package de.sofd.viskit.image;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import de.sofd.util.FloatRange;
import de.sofd.util.Histogram;
import de.sofd.util.IntRange;

/**
 * Base class for ViskitImage implementations. Subclasses must implement the
 * basic image getters like {@link #getRawImage()}, {@link #getBufferedImage()},
 * {@link #hasRawImage()} etc., this class implements the other getters on top
 * of that.
 * 
 * @author olaf
 */
public abstract class ViskitImageBase implements ViskitImage {

    protected Histogram histogram;
    
    @Override
    public int getHeight() {
        if (hasRawImage()) {
            return getRawImage().getHeight();
        } else {
            return getBufferedImage().getHeight();
        }
    }

    @Override
    public int getWidth() {
        if (hasRawImage()) {
            return getRawImage().getWidth();
        } else {
            return getBufferedImage().getWidth();
        }
    }

    @Override
    public boolean isBufferedImageSigned() {
        return true;
    }

    @Override
    public RawImage getProxyRawImage() {
        return getRawImage();
    }

    @Override
    public boolean isRawImagePreferable() {
        return hasRawImage();
    }

    @Override
    public FloatRange getMaximumPixelValuesRange() {
        //TODO: if hasRawImage()...
        return new FloatRange(-32768, 32767);
    }
    
    @Override
    public FloatRange getUsedPixelValuesRange() {
        //init max range
        FloatRange range = getMaximumPixelValuesRange();
        float min = range.getMin();
        float max = range.getMax();

        if (hasRawImage() && isRawImagePreferable()) {
            min = range.getMax();
            max = range.getMin();
            
            RawImage img = getRawImage();
            int pxCount = (img.getPixelFormat() == RawImage.PIXEL_FORMAT_RGB ? 3 : 1) * img.getWidth() * img.getHeight();

            if (img.getPixelType() != RawImage.PIXEL_TYPE_UNSIGNED_16BIT) {
                //unsigned 8 bit or 12 bit or signed
                ShortBuffer buf = (ShortBuffer) img.getPixelData();
                for (int i = 0; i < pxCount; i++) {
                    short val = buf.get(i);
                    if (val < min) { min = val; }
                    if (val > max) { max = val; }
                }
            } else {
                min = range.getMax();
                max = range.getMin();
                
                //unsigned 16 bit
                IntBuffer buf = (IntBuffer) img.getPixelData();
                for (int i = 0; i < pxCount; i++) {
                    int val = buf.get(i);
                    if (val < min) { min = val; }
                    if (val > max) { max = val; }
                }
            }
        } else {
            BufferedImage bimg = getBufferedImage();

            if (bimg.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_GRAY) {

                min = range.getMax();
                max = range.getMin();
                FloatRange range2 = getFloatRange(bimg, min, max, isBufferedImageSigned());
                min = range2.getMin();
                max = range2.getMax();
            }
            /*min = 200;
            max = 800;*/
        }

        //TODO: the following needs to be done in DICOM-based subclasses
        /*
        if (metadata.contains(Tag.RescaleSlope) && metadata.contains(Tag.RescaleIntercept)) {
            float rscSlope = metadata.getFloat(Tag.RescaleSlope);
            float rscIntercept = metadata.getFloat(Tag.RescaleIntercept);
            min = (int) (rscSlope * min + rscIntercept);
            max = (int) (rscSlope * max + rscIntercept);
        }
        */
        
        return new FloatRange(min, max);
    }

    protected static FloatRange getFloatRange(BufferedImage bimg, float min, float max, boolean isSigned) {
        Raster raster = bimg.getRaster();
        if (raster.getNumBands() != 1) {
            throw new IllegalArgumentException("source image must be grayscales");
        }

        for (int x = 0; x < bimg.getWidth(); x++) {
            for (int y = 0; y < bimg.getHeight(); y++) {
                int val = raster.getSample(x, y, 0);
                if (isSigned) {
                    val = (int)(short)val;  // will only work for 16-bit signed...
                }

                if (val < min) { min = val; }
                if (val > max) { max = val; }
            }
        }

        return new FloatRange(min, max);
    }

    @Override
    public Histogram getHistogram() {
        if (this.histogram != null)
            return this.histogram;
        
        //init max range
        FloatRange range = getMaximumPixelValuesRange();
        int min = (int)range.getMin();
        int max = (int)range.getMax();

        if (!hasRawImage() || !isRawImagePreferable()) {
            BufferedImage bimg = getBufferedImage();
            return new Histogram(bimg.getRaster(), new IntRange(min, max));
        }
            
        RawImage img = getRawImage();

        if (img.getPixelType() != RawImage.PIXEL_TYPE_UNSIGNED_16BIT) {
            //unsigned 8 bit or 12 bit or signed
            ShortBuffer buf = (ShortBuffer) img.getPixelData();
            return new Histogram(buf, new IntRange(min, max));
        }

        //unsigned 16 bit
        IntBuffer buf = (IntBuffer) img.getPixelData();
        this.histogram = new Histogram(buf, new IntRange(min, max));
        
        return this.histogram;
    }

    @Override
    public int hashCode() {
        return getImageKey().hashCode();
    }
}
