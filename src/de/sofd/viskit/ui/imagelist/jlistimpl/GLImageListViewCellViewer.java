package de.sofd.viskit.ui.imagelist.jlistimpl;

import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImageOp;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLJPanel;

/**
 * Swing component for displaying a {@link ImageListViewCell} using an OpenGL-based
 * renderer. For use in cell renderers or elsewhere.
 *
 * @author olaf
 */
public class GLImageListViewCellViewer extends GLJPanel {

    private final ImageListViewCell displayedCell;

    public GLImageListViewCellViewer(ImageListViewCell cell) {
        this.displayedCell = cell;
        this.addGLEventListener(new GLEventHandler());
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


    // TODO: implement painting (with an internal GLEventListener rather than overriding paintComponent)

    protected void paintComponent_ren(Graphics g) {
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
        //g2d.drawImage(getWindowedImage(), scaleImageOp, 0, 0);
    }

    protected void renderOverlays(Graphics2D g2d) {
        displayedCell.getRoiDrawingViewer().paint(g2d);
    }


    protected class GLEventHandler implements GLEventListener {

        @Override
        public void init(GLAutoDrawable glAutoDrawable) {
            System.out.println("INIT GLAutoDrawable");
            GL2 gl = (GL2) glAutoDrawable.getGL();
        }

        @Override
        public void dispose(GLAutoDrawable glAutoDrawable) {
        }

        @Override
        public void display(GLAutoDrawable glAutoDrawable) {
            GL2 gl = (GL2) glAutoDrawable.getGL();
            //glAutoDrawable.swapBuffers();
        }

        @Override
        public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
            GL2 gl = (GL2) glAutoDrawable.getGL();
        }
    };

}
