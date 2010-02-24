package de.sofd.viskit.ui.imagelist.cellviewers.java2d;

import de.sofd.draw2d.viewer.gc.GC;
import de.sofd.viskit.draw2d.gc.ViskitGC;
import de.sofd.viskit.model.DicomImageListViewModelElement;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;
import de.sofd.viskit.ui.imagelist.cellviewers.BaseImageListViewCellViewer;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintEvent;

import java.awt.Dimension;
import java.awt.Graphics;
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
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 * Swing component for displaying a {@link ImageListViewCell}. For use in cell renderers
 * or elsewhere.
 *
 * @author olaf
 */
public class ImageListViewCellViewer extends BaseImageListViewCellViewer {

    public ImageListViewCellViewer(ImageListViewCell cell) {
        super(cell);
    }

    @Override
    public void setDisplayedCell(ImageListViewCell displayedCell) {
        super.setDisplayedCell(displayedCell);
        repaint();
    }

    protected BufferedImage getWindowedImage() {
        // TODO: caching of windowed images, probably using
        //       displayedCell.getDisplayedModelElement().getImageKey() and the windowing parameters as the cache key
        BufferedImage srcImg = displayedCell.getDisplayedModelElement().getImage();
        BufferedImage windowedImage;
        // TODO: use the model element's RawImage instead of the BufferedImage when possible
        ///*
        if (srcImg.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_GRAY) {
            windowedImage = windowMonochrome(srcImg, displayedCell.getWindowLocation(), displayedCell.getWindowWidth());
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


    private BufferedImage windowMonochrome(BufferedImage srcImg, float windowLocation, float windowWidth) {
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

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g2d);

        displayedCell.setLatestSize(getSize());

        // Call all CellPaintListeners below the image in the z-order.
        // Eventually all painting, including the image and ROIs, should happen in PaintListeners.
        getDisplayedCell().getOwner().fireCellPaintEvent(new ImageListViewCellPaintEvent(getDisplayedCell(), new ViskitGC(g2d), null, null),
                                                         Integer.MIN_VALUE, JImageListView.PAINT_ZORDER_IMAGE);
        
        //give the render* methods a Graphics2D whose coordinate system
        //(and eventually, clipping) is already relative to the area in
        //which the image should be drawn
        Graphics2D userGraphics = (Graphics2D)g2d.create();
        Point2D imageOffset = getImageOffset();
        userGraphics.transform(AffineTransform.getTranslateInstance(imageOffset.getX(), imageOffset.getY()));

        // render the image
        BufferedImageOp scaleImageOp = new AffineTransformOp(getDicomToUiTransform(), AffineTransformOp.TYPE_BILINEAR);
        userGraphics.drawImage(getWindowedImage(), scaleImageOp, 0, 0);

        // render the ROI DrawingViewer
        displayedCell.getRoiDrawingViewer().paint(new ViskitGC(userGraphics));

        // image and ROIs have been drawn. Now call all CellPaintListeners above the ROIs in the z-order.
        // Eventually all painting, including the image and ROIs, should happen in PaintListeners.
        getDisplayedCell().getOwner().fireCellPaintEvent(new ImageListViewCellPaintEvent(getDisplayedCell(), new ViskitGC(g2d), null, null),
                                                         JImageListView.PAINT_ZORDER_ROI + 1, Integer.MAX_VALUE);
    }

    protected void renderImage(Graphics2D g2d) {
        BufferedImageOp scaleImageOp = new AffineTransformOp(getDicomToUiTransform(), AffineTransformOp.TYPE_BILINEAR);
        g2d.drawImage(getWindowedImage(), scaleImageOp, 0, 0);
        //g2d.drawImage(getObjectImage(), scaleImageOp, 0, 0);
    }

    protected void renderOverlays(Graphics2D g2d) {
        displayedCell.getRoiDrawingViewer().paint(new ViskitGC(g2d));
    }

}
