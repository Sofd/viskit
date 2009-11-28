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
        // TODO: caching of windowed images
        // TODO: unique identification of images would provide for better caching possibility along
        //       the lines of n2g JDicomObjectImageViewer.
        //       Maybe expose the DicomObject (containing the image UID) in the cell class after all.
        BufferedImage srcImg = displayedCell.getDisplayedModelElement().getImage();
        BufferedImage windowedImage = new BufferedImage(srcImg.getWidth(), srcImg.getHeight(),
                                                        BufferedImage.TYPE_INT_RGB);
        float scale = 256.0F / displayedCell.getWindowWidth();
        float offset = scale * (displayedCell.getWindowWidth() / 2 - displayedCell.getWindowLocation());

        /* windowing using RescaleOp disabled for now -- image too dark (TODO: investigate)
        RescaleOp rescaleOp = new RescaleOp(scale, offset, null);
        // TODO: windowedImage after the following filter call is not identical to the
        // return value, contrary to what the documentation seems to imply. If we get
        // rid of windowedImage (and supply null instead), the result looks visually
        // identical *most* of the times, but for some images, e.g. cd846__center4001__39.dcm,
        // JAI causes a JVM segfault on Linux... With the call as done here, it seems to
        // work all the time, albeit maybe with unnecessary performance penalties.
        return rescaleOp.filter(srcImg, windowedImage);
         */

        if (srcImg.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_GRAY) {
            windowMonochrome(srcImg, windowedImage, scale, offset);
        } else if (srcImg.getColorModel().getColorSpace().isCS_sRGB()) {
            windowRGB(srcImg, windowedImage, scale, offset);
        } else {
            throw new IllegalStateException("don't know how to window image with color space " + srcImg.getColorModel().getColorSpace());
            // TODO: do something cleverer here? Like, create windowedImage
            //    with a color space that's "compatible" to srcImg (using
            //    some createCompatibleImage() method in BufferedImage or elsewhere),
            //    window all bands of that, and let the JRE figure out how to draw the result?
        }
        return windowedImage;
    }

    /**
     * @pre destImg is of type BufferedImage.TYPE_INT_RGB
     */
    private BufferedImage windowMonochrome(BufferedImage srcImg, BufferedImage destImg, float scale, float offset) {
        final int windowedImageGrayscalesCount = 256;  // for BufferedImage.TYPE_INT_RGB
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
    private BufferedImage windowRGB(BufferedImage srcImg, BufferedImage destImg, float scale, float offset) {
        final int windowedImageBandValuesCount = 256;  // for BufferedImage.TYPE_INT_RGB
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
