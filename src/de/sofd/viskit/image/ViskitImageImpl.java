package de.sofd.viskit.image;

import java.awt.image.BufferedImage;

/**
 * Simple implementation of ViskitImage that just provides member fields for all
 * the properties. May be used directly or serve as a base class for other
 * implementations.
 * 
 * @author olaf
 */
public class ViskitImageImpl implements ViskitImage {

    protected final Object key;

    /*
     * use an explicit flag rather than (rawImage != null) b/c there may be
     * cases where one wants rawImage to be null and still record the fact the
     * the ViskitImage "has" a RawImage.
     */
    protected boolean hasRawImage;

    protected RawImage rawImage;
    protected BufferedImage bufferedImage;
    
    public ViskitImageImpl(Object key, BufferedImage bimg) {
        this.key = key;
        this.bufferedImage = bimg;
        this.hasRawImage = false;
    }

    public ViskitImageImpl(Object key, RawImage rimg) {
        this.key = key;
        this.rawImage = rimg;
        this.hasRawImage = false;
    }

    @Override
    public int getHeight() {
        if (hasRawImage()) {
            return getRawImage().getHeight();
        } else {
            return getImage().getHeight();
        }
    }

    @Override
    public int getWidth() {
        if (hasRawImage()) {
            return getRawImage().getWidth();
        } else {
            return getImage().getWidth();
        }
    }

    @Override
    public BufferedImage getImage() {
        if (hasRawImage()) {
            //TODO: create it from getRawImage()? What about caching the result then?
            throw new IllegalStateException("no BufferedImage");
        }
        return bufferedImage;
    }

    @Override
    public Object getImageKey() {
        return key;
    }

    @Override
    public RawImage getRawImage() {
        return rawImage;
    }

    @Override
    public boolean hasBufferedImage() {
        return !hasRawImage();
    }

    @Override
    public boolean hasRawImage() {
        return hasRawImage;
    }

    @Override
    public boolean isRawImagePreferable() {
        return hasRawImage();
    }

}
