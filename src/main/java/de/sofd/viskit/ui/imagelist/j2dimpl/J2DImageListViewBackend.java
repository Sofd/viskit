package de.sofd.viskit.ui.imagelist.j2dimpl;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.Raster;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Map;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import de.sofd.draw2d.viewer.DrawingViewer;
import de.sofd.draw2d.viewer.adapters.DefaultObjectViewerAdapterFactory;
import de.sofd.math.LinAlg;
import de.sofd.util.FloatRange;
import de.sofd.viskit.image.RawImage;
import de.sofd.viskit.image.ViskitImage;
import de.sofd.viskit.model.DicomImageListViewModelElement;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.model.LookupTable;
import de.sofd.viskit.ui.imagelist.ImageListViewBackendBase;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintEvent;

public class J2DImageListViewBackend extends ImageListViewBackendBase {

    @Override
    public void glSharedContextDataInitialization(Object gl,
            Map<String, Object> sharedData) {
        
    }
    
    @Override
    public DrawingViewer createRoiDrawingViewer(ImageListViewModelElement elt, ImageListViewCell cell) {
        return new DrawingViewer(elt.getRoiDrawing(), new DefaultObjectViewerAdapterFactory());
    }

    @Override
    public void paintCellImage(ImageListViewCellPaintEvent e) {
        ImageListViewCell cell = e.getSource();
        Graphics2D g2d = (Graphics2D) e.getGc().getGraphics2D().create();

        //give the render* methods a Graphics2D whose coordinate system
        //(and eventually, clipping) is already relative to the area in
        //which the image should be drawn
        Graphics2D userGraphics = (Graphics2D)g2d.create();
        Point2D imageOffset = getImageOffset(cell);
        userGraphics.transform(AffineTransform.getTranslateInstance(imageOffset.getX(), imageOffset.getY()));

        // render the image
        BufferedImageOp scaleImageOp = new AffineTransformOp(getDicomToUiTransform(cell), AffineTransformOp.TYPE_BILINEAR);
        userGraphics.drawImage(getWindowedImage(cell), scaleImageOp, 0, 0);
    }
    
    private Point2D getImageOffset(ImageListViewCell cell) {
        Point2D imgSize = getScaledImageSize(cell);
        Dimension latestSize = cell.getLatestSize();
        return new Point2D.Double((latestSize.width + 2 * cell.getCenterOffset().getX() - (int) imgSize.getX()) / 2,
                                  (latestSize.height + 2 * cell.getCenterOffset().getY() - (int) imgSize.getY()) / 2);
    }

    private Point2D getScaledImageSize(ImageListViewCell cell) {
        ViskitImage img = cell.getDisplayedModelElement().getImage();
        return getDicomToUiTransform(cell).transform(new Point2D.Double(img.getWidth(), img.getHeight()), null);
    }
    
    protected AffineTransform getDicomToUiTransform(ImageListViewCell cell) {
        double z = cell.getScale();
        return AffineTransform.getScaleInstance(z, z);
    }

    protected BufferedImage getWindowedImage(ImageListViewCell displayedCell) {
        BufferedImage windowedImage = tryWindowRawImage(displayedCell);
        if (windowedImage != null) {
            return windowedImage;
        } else {
            // TODO: caching of windowed images, probably using
            //       displayedCell.getDisplayedModelElement().getImageKey() and the windowing parameters as the cache key
            BufferedImage srcImg = displayedCell.getDisplayedModelElement().getImage().getBufferedImage();
            // TODO: use the model element's RawImage instead of the BufferedImage when possible
            ///*
            if (srcImg.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_GRAY) {
                windowedImage = windowMonochrome(displayedCell, srcImg, displayedCell.getWindowLocation(), displayedCell.getWindowWidth());
            } else if (srcImg.getColorModel().getColorSpace().isCS_sRGB()) {
                windowedImage = windowRGB(srcImg, displayedCell.getWindowLocation(), displayedCell.getWindowWidth());
            } else {
                throw new IllegalStateException("don't know how to window image with color space " + srcImg.getColorModel().getColorSpace());
                // TODO: do something cleverer here? Like, create windowedImage
                //    with a color space that's "compatible" to srcImg (using
                //    some createCompatibleImage() method in BufferedImage or elsewhere),
                //    window all bands of that, and let the JRE figure out how to draw the result?
            }
            //*/
            //windowedImage = windowWithRasterOp(srcImg, windowLocation, windowWidth);
            //windowedImage = srcImg;
    
            return windowedImage;
        }
    }

    private BufferedImage tryWindowRawImage(ImageListViewCell displayedCell) {
        ImageListViewModelElement elt = displayedCell.getDisplayedModelElement();
        ViskitImage img = elt.getImage();
        if (!(img.hasRawImage() && img.isRawImagePreferable())) {
            return null;
        }
        
        //logger.debug("trying to create windowed BufferedImage for: " + elt.getImageKey());

        float[] pixelTransform = new float[]{1,0}; // compute the complete raw input => output pixel transformation in here
        
        // rescale/slope tags
        if (elt instanceof DicomImageListViewModelElement) {
            try {
                DicomImageListViewModelElement delt = (DicomImageListViewModelElement) elt;
                if (delt.getDicomImageMetaData().contains(Tag.RescaleSlope) && delt.getDicomImageMetaData().contains(Tag.RescaleIntercept)) {
                    float rscSlope = delt.getDicomImageMetaData().getFloat(Tag.RescaleSlope);
                    float rscIntercept = delt.getDicomImageMetaData().getFloat(Tag.RescaleIntercept);
                    LinAlg.matrMult1D(new float[]{rscSlope, rscIntercept}, pixelTransform, pixelTransform);
                }
            } catch (Exception e) {
                //ignore -- no error
            }
        }
        
        // normalization to [0,1]
        FloatRange pxValuesRange = img.getMaximumPixelValuesRange();
        float minGrayvalue = pxValuesRange.getMin();
        float nGrayvalues = pxValuesRange.getDelta();
        LinAlg.matrMult1D(new float[]{1.0F/nGrayvalues, -minGrayvalue/nGrayvalues}, pixelTransform, pixelTransform);

        // window level/width
        float wl = (displayedCell.getWindowLocation() - minGrayvalue) / nGrayvalues;
        float ww = displayedCell.getWindowWidth() / nGrayvalues;
        float scale = 1F/ww;
        float offset = (ww/2-wl)*scale;
        LinAlg.matrMult1D(new float[]{scale, offset}, pixelTransform, pixelTransform);


        LookupTable lut = displayedCell.getLookupTable();
        /*if (displayedCell.isOutputGrayscaleRGBs()) {
            //TODO: perform 12-bit grayscale output in J2D as well
        } else */if (lut != null) {
            // transformation to output LUT index
            int lutLength = lut.getRGBAValues().limit() / 4;
            LinAlg.matrMult1D(new float[]{lutLength, 0}, pixelTransform, pixelTransform);
        } else {
            // transformation to output grayscale range ( [0,256) )
            LinAlg.matrMult1D(new float[]{256, 0}, pixelTransform, pixelTransform);
        }
        
        RawImage rimg = img.getRawImage();
        if (rimg.getPixelFormat() != RawImage.PIXEL_FORMAT_LUMINANCE) {
            return null;  // can't window raw RGB images for now
        } else {
            switch (rimg.getPixelType()) {
            // will only window RawImages whose pixelData buffer is a ShortBuffer for now
            case RawImage.PIXEL_TYPE_UNSIGNED_BYTE:
            case RawImage.PIXEL_TYPE_SIGNED_12BIT:
            case RawImage.PIXEL_TYPE_SIGNED_16BIT:
            case RawImage.PIXEL_TYPE_UNSIGNED_12BIT: {
                //TODO: maybe reuse BufferedImages of the same size to relieve the GC
                float txscale = pixelTransform[0];
                float txoffset = pixelTransform[1];
                ShortBuffer srcBuffer = (ShortBuffer) rimg.getPixelData();
                int w = rimg.getWidth();
                int h = rimg.getHeight();
                BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                WritableRaster resultRaster = result.getRaster();
                if (lut != null) {
                    windowLUTShort(srcBuffer, w, h, txscale, txoffset, lut, resultRaster);
                } else {
                    windowGrayShort(srcBuffer, w, h, txscale, txoffset, resultRaster);
                }
                return result;
            }
            //...and IntBuffer
            case RawImage.PIXEL_TYPE_UNSIGNED_16BIT: {
                //TODO: maybe reuse BufferedImages of the same size to relieve the GC
                float txscale = pixelTransform[0];
                float txoffset = pixelTransform[1];
                IntBuffer srcBuffer = (IntBuffer) rimg.getPixelData();
                int w = rimg.getWidth();
                int h = rimg.getHeight();
                BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                WritableRaster resultRaster = result.getRaster();
                if (lut != null) {
                    windowLUTInt(srcBuffer, w, h, txscale, txoffset, lut, resultRaster);
                } else {
                    windowGrayInt(srcBuffer, w, h, txscale, txoffset, resultRaster);
                }
                return result;
            }
            default:
                return null;
            }
        }
    }

    //most performance-sensitive part of the J2D pipeline --
    // separate window* methods to ensure Hotspot compiles them (when we had all this code
    // directly in tryWindowRawImage, that method became so long that Hotspot did not
    // compile it completely -- some of the inner loops ran five times slower than other,
    // virtually identical inner loops)

    private void windowGrayShort(ShortBuffer srcBuffer, int w, int h, float txscale, float txoffset, WritableRaster resultRaster) {
        int index = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float destGrayValue = txscale * srcBuffer.get(index++) + txoffset;
                //clamp
                if (destGrayValue < 0) {
                    destGrayValue = 0;
                } else if (destGrayValue >= 256) {
                    destGrayValue = 255;
                }
                resultRaster.setSample(x, y, 0, destGrayValue);
                resultRaster.setSample(x, y, 1, destGrayValue);
                resultRaster.setSample(x, y, 2, destGrayValue);
            }
        }
    }

    private void windowLUTShort(ShortBuffer srcBuffer, int w, int h, float txscale, float txoffset, LookupTable lut, WritableRaster resultRaster) {
        int[][] lutRGBAs = lut.getRGBA256intArrays();
        int lutLength = lutRGBAs.length;
        int lutLengthMinus1 = lutLength - 1;
        int index = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int destLutIndex = (int)(txscale * srcBuffer.get(index++) + txoffset);
                //clamp
                if (destLutIndex < 0) {
                    destLutIndex = 0;
                } else if (destLutIndex >= lutLength) {
                    destLutIndex = lutLengthMinus1;
                }
                resultRaster.setPixel(x, y, lutRGBAs[destLutIndex]);
            }
        }
    }

    //*Int method identical to *Short ones, except they take IntBuffers
    //   (we don't want to have an additional if-then-else in the innermost loop,
    //   and there's no proper generics support in Java)

    private void windowGrayInt(IntBuffer srcBuffer, int w, int h, float txscale, float txoffset, WritableRaster resultRaster) {
        int index = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float destGrayValue = txscale * srcBuffer.get(index++) + txoffset;
                //clamp
                if (destGrayValue < 0) {
                    destGrayValue = 0;
                } else if (destGrayValue >= 256) {
                    destGrayValue = 255;
                }
                resultRaster.setSample(x, y, 0, destGrayValue);
                resultRaster.setSample(x, y, 1, destGrayValue);
                resultRaster.setSample(x, y, 2, destGrayValue);
            }
        }
    }

    private void windowLUTInt(IntBuffer srcBuffer, int w, int h, float txscale, float txoffset, LookupTable lut, WritableRaster resultRaster) {
        int[][] lutRGBAs = lut.getRGBA256intArrays();
        int lutLength = lutRGBAs.length;
        int lutLengthMinus1 = lutLength - 1;
        int index = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int destLutIndex = (int)(txscale * srcBuffer.get(index++) + txoffset);
                //clamp
                if (destLutIndex < 0) {
                    destLutIndex = 0;
                } else if (destLutIndex >= lutLength) {
                    destLutIndex = lutLengthMinus1;
                }
                resultRaster.setPixel(x, y, lutRGBAs[destLutIndex]);
            }
        }
    }

    private BufferedImage windowMonochrome(ImageListViewCell displayedCell, BufferedImage srcImg, float windowLocation, float windowWidth) {
        BufferedImage destImg = new BufferedImage(srcImg.getWidth(), srcImg.getHeight(), BufferedImage.TYPE_INT_RGB);

        boolean isSigned = false;
        int minValue = 0;
        {
            // hack: try to determine signedness and minValue from DICOM metadata if available --
            // the BufferedImage's metadata don't contain that information reliably.
            // Only works for some special cases
            ImageListViewModelElement elt = displayedCell.getDisplayedModelElement();
            if (elt instanceof DicomImageListViewModelElement) {
                DicomImageListViewModelElement delt = (DicomImageListViewModelElement) elt;
                DicomObject imgMetadata = delt.getDicomImageMetaData();
                int bitsAllocated = imgMetadata.getInt(Tag.BitsAllocated);
                isSigned = (1 == imgMetadata.getInt(Tag.PixelRepresentation));
                if (isSigned && (bitsAllocated > 0)) {
                    minValue = -(1<<(bitsAllocated-1));
                }
            }
        }


        final int windowedImageGrayscalesCount = 256;  // for BufferedImage.TYPE_INT_RGB
        float scale = windowedImageGrayscalesCount/windowWidth;
        float offset = (windowWidth/2 - windowLocation)*scale;
        if (! (srcImg.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_GRAY)) {
            throw new IllegalArgumentException("source image must be grayscales");
        }
        Raster srcRaster = srcImg.getRaster();
        if (srcRaster.getNumBands() != 1) {
            throw new IllegalArgumentException("source image must be grayscales");
        }
        WritableRaster resultRaster = destImg.getRaster();
        for (int x = 0; x < srcImg.getWidth(); x++) {
            for (int y = 0; y < srcImg.getHeight(); y++) {
                int srcGrayValue = srcRaster.getSample(x, y, 0);
                if (isSigned) {
                    srcGrayValue = (int)(short)srcGrayValue;  // will only work for 16-bit signed...
                }
                float destGrayValue = scale * srcGrayValue + offset;
                // clamp
                if (destGrayValue < 0) {
                    destGrayValue = 0;
                } else if (destGrayValue >= windowedImageGrayscalesCount) {
                    destGrayValue = windowedImageGrayscalesCount - 1;
                }
                resultRaster.setSample(x, y, 0, destGrayValue);
                resultRaster.setSample(x, y, 1, destGrayValue);
                resultRaster.setSample(x, y, 2, destGrayValue);
            }
        }
        return destImg;
    }


    /*
    private void getImageMetadata() {
        DicomObject imgMetadata = displayedCell.getDisplayedModelElement().getDicomImageMetaData();
        int bitsAllocated = imgMetadata.getInt(Tag.BitsAllocated);
        if (bitsAllocated <= 0) {
        }
        int bitsStored = imgMetadata.getInt(Tag.BitsStored);
        if (bitsStored <= 0) {
        }
        boolean isSigned = (1 == imgMetadata.getInt(Tag.PixelRepresentation));
    }
     */

    private BufferedImage windowRGB(BufferedImage srcImg, float windowLocation, float windowWidth) {
        BufferedImage destImg = new BufferedImage(srcImg.getWidth(), srcImg.getHeight(), BufferedImage.TYPE_INT_RGB);
        final int windowedImageBandValuesCount = 256;  // for BufferedImage.TYPE_INT_RGB
        float scale = windowedImageBandValuesCount/windowWidth;
        float offset = (windowWidth/2-windowLocation)*scale;
        if (! srcImg.getColorModel().getColorSpace().isCS_sRGB()) {
            throw new IllegalArgumentException("source image must be RGB");
        }
        Raster srcRaster = srcImg.getRaster();
        if (srcRaster.getNumBands() != 3) {
            throw new IllegalArgumentException("source image must be RGB");
        }
        WritableRaster resultRaster = destImg.getRaster();
        for (int x = 0; x < srcImg.getWidth(); x++) {
            for (int y = 0; y < srcImg.getHeight(); y++) {
                for (int band = 0; band < 3; band++) {
                    int srcGrayValue = srcRaster.getSample(x, y, band);
                    float destGrayValue = scale * srcGrayValue + offset;
                    // clamp
                    if (destGrayValue < 0) {
                        destGrayValue = 0;
                    } else if (destGrayValue >= windowedImageBandValuesCount) {
                        destGrayValue = windowedImageBandValuesCount - 1;
                    }
                    resultRaster.setSample(x, y, band, destGrayValue);
                }
            }
        }
        return destImg;
    }

    private BufferedImage windowWithRasterOp(BufferedImage srcImg, float windowLocation, float windowWidth) {
        //final int windowedImageBandValuesCount = 256;  // for BufferedImage.TYPE_INT_RGB
        //final float windowedImageBandValuesCount = 1.0F;
        //final float windowedImageBandValuesCount = 65535F;
        //final float windowedImageBandValuesCount = 4095F;
        final float windowedImageBandValuesCount = (1 << srcImg.getColorModel().getComponentSize(0)) - 1;
        float scale = windowedImageBandValuesCount/windowWidth;
        float offset = (windowWidth/2-windowLocation)*scale;
        RescaleOp rescaleOp = new RescaleOp(scale, offset, null);
        return rescaleOp.filter(srcImg, null);
    }

}
