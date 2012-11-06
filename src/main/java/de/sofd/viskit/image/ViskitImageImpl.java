package de.sofd.viskit.image;

import java.awt.image.BufferedImage;

/**
 * Simple implementation of ViskitImage that directly wraps a {@link RawImage}
 * or {@link BufferedImage}.
 * 
 * @author olaf
 */
public class ViskitImageImpl extends ViskitImageBase {

    protected final Object imageKey;

    /*
     * use an explicit flag rather than (rawImage != null) b/c there may be
     * cases where one wants rawImage to be null and still record the fact the
     * the ViskitImage "has" a RawImage.
     */
    protected boolean hasRawImage;

    protected RawImage rawImage;
    protected BufferedImage bufferedImage;
    
    public ViskitImageImpl(BufferedImage bimg) {
        this(null, bimg);
    }

    public ViskitImageImpl(RawImage rimg) {
        this(null, rimg);
    }
    
    public ViskitImageImpl(Object imageKey, BufferedImage bimg) {
        this.imageKey = imageKey;
        this.bufferedImage = bimg;
        this.hasRawImage = false;
    }

    public ViskitImageImpl(Object key, RawImage rimg) {
        this.imageKey = key;
        this.rawImage = rimg;
        this.hasRawImage = false;
    }

    @Override
    public BufferedImage getBufferedImage() {
        if (hasRawImage()) {
            //TODO: create it from getRawImage()? What about caching the result then?
            throw new IllegalStateException("no BufferedImage");
        }
        return bufferedImage;
    }
    
    @Override
    public Object getImageKey() {
        if (imageKey == null) {
            return System.identityHashCode(this);
        } else {
            return imageKey;
        }
    }

    @Override
    public RawImage getRawImage() {
        return rawImage;
    }
    
    @Override
    public RawImage getProxyRawImage() {
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

    @Override
    public int hashCode() {
        return getImageKey().hashCode();
    }
}
