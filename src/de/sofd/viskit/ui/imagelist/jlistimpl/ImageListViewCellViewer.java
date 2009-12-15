/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.sofd.viskit.ui.imagelist.jlistimpl;

import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.Raster;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;
import javax.swing.JPanel;

/**
 * Swing component for displaying a {@link ImageListViewCell}. For use in cell renderers
 * or elsewhere.
 *
 * @author olaf
 */
public class ImageListViewCellViewer extends JPanel {

    private final ImageListViewCell displayedCell;

    public ImageListViewCellViewer(ImageListViewCell cell) {
        this.displayedCell = cell;
    }

    public ImageListViewCell getDisplayedCell() {
        return displayedCell;
    }

    public int getOriginalImageWidth() {
        return displayedCell.getDisplayedModelElement().getImage().getWidth();
    }

    public int getOriginalImageHeight() {
        return displayedCell.getDisplayedModelElement().getImage().getHeight();
    }

    public double getZoomFactor() {
        return displayedCell.getScale();
        // TODO: when it changes, we'd want to recomputeImageOrigin()...
    }

    protected AffineTransform getDicomToUiTransform() {
        double z = getZoomFactor();
        return AffineTransform.getScaleInstance(z, z);
    }

    public Point2D getScaledImageSize() {
        return getDicomToUiTransform().transform(new Point2D.Double(getOriginalImageWidth(), getOriginalImageHeight()), null);
    }

    @Override
    public Dimension getPreferredSize() {
        Point2D scaledImageSize = getScaledImageSize();
        Insets insets = getInsets();  // insets imposed by our getBorder()
        return new Dimension((int)(scaledImageSize.getX()+insets.left+insets.right),
                             (int)(scaledImageSize.getY()+insets.top+insets.bottom));
    }

    public Point2D getImageOffset() {
        Point2D imgSize = getScaledImageSize();
        Dimension latestSize = displayedCell.getLatestSize();
        return new Point2D.Double((latestSize.width + 2 * displayedCell.getCenterOffset().getX() - (int) imgSize.getX()) / 2,
                                  (latestSize.height + 2 * displayedCell.getCenterOffset().getY() - (int) imgSize.getY()) / 2);
    }

    protected BufferedImage getWindowedImage() {
        // TODO: caching of windowed images, probably using
        //       displayedCell.getDisplayedModelElement().getImageKey() and the windowing parameters as the cache key
        BufferedImage srcImg = displayedCell.getDisplayedModelElement().getImage();
        BufferedImage windowedImage;
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


    /**
     * @pre destImg is of type BufferedImage.TYPE_INT_RGB
     */
    private BufferedImage windowMonochrome(BufferedImage srcImg, float windowLocation, float windowWidth) {
        BufferedImage destImg = new BufferedImage(srcImg.getWidth(), srcImg.getHeight(), BufferedImage.TYPE_INT_RGB);
        final int windowedImageGrayscalesCount = 256;  // for BufferedImage.TYPE_INT_RGB
        float scale = windowedImageGrayscalesCount/windowWidth;
        float offset = (windowWidth/2-windowLocation)*scale;
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

    /**
     * @pre destImg is of type BufferedImage.TYPE_INT_RGB
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

        //give the render* methods a Graphics2D whose coordinate system
        //(and eventually, clipping) is already relative to the area in
        //which the image should be drawn
        Graphics2D userGraphics = (Graphics2D)g2d.create();
        Point2D imageOffset = getImageOffset();
        userGraphics.transform(AffineTransform.getTranslateInstance(imageOffset.getX(), imageOffset.getY()));
        renderImage(userGraphics);
        renderOverlays(userGraphics);
    }

    protected void renderImage(Graphics2D g2d) {
        BufferedImageOp scaleImageOp = new AffineTransformOp(getDicomToUiTransform(), AffineTransformOp.TYPE_BILINEAR);
        // TODO: windowing
        g2d.drawImage(getWindowedImage(), scaleImageOp, 0, 0);
        //g2d.drawImage(getObjectImage(), scaleImageOp, 0, 0);
    }

    protected void renderOverlays(Graphics2D g2d) {
        displayedCell.getRoiDrawingViewer().paint(g2d);
    }

}
