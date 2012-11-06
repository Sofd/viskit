package de.sofd.viskit.controllers.cellpaint;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Map;

import javax.media.opengl.GL2;


import org.dcm4che2.data.Tag;
import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.sofd.viskit.draw2d.gc.ViskitGC;
import de.sofd.viskit.image.ViskitImage;
import de.sofd.viskit.ui.imagelist.ImageListView;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;

/**
 * Controller that paints the ROIs of the cell's model element
 * 
 * @author honglinh
 *
 */
public class ImageListViewRoiPaintController extends CellPaintControllerBase {
    
    public ImageListViewRoiPaintController() {
        this(null, ImageListView.PAINT_ZORDER_ROI);
    }

    public ImageListViewRoiPaintController(ImageListView controlledImageListView) {
        super(controlledImageListView, ImageListView.PAINT_ZORDER_ROI);
    }

    public ImageListViewRoiPaintController(ImageListView controlledImageListView, int zOrder) {
        super(controlledImageListView, zOrder);
    }
    
    @Override
    protected void paintJ2D(ImageListViewCell cell, Graphics2D g2d) {
        Graphics2D userGraphics = (Graphics2D)g2d.create();
        Point2D imageOffset = getImageOffset(cell);
     
        userGraphics.transform(AffineTransform.getTranslateInstance(imageOffset.getX(), imageOffset.getY()));
        cell.getRoiDrawingViewer().paint(new ViskitGC(userGraphics));
    }
    
    
    @Override
    protected void paintGL(ImageListViewCell cell, GL2 gl,
            Map<String, Object> sharedContextData) {
        cell.getDisplayedModelElement();
        ViskitGC gc = new ViskitGC(gl);
        gl.glPushMatrix();
        try {
            Point2D centerOffset = cell.getCenterOffset();
            float scale = (float) cell.getScale();
            float w2 = (float) cell.getDisplayedModelElement().getImage().getWidth() * scale / 2;
            float h2 = (float) cell.getDisplayedModelElement().getImage().getHeight() * scale / 2;
            Dimension cellSize = cell.getLatestSize();
            gl.glTranslated(cellSize.getWidth() / 2, cellSize.getHeight() / 2, 0);
            gl.glTranslated(centerOffset.getX(), centerOffset.getY(), 0);
            gl.glTranslated(-w2, -h2, 0);

            cell.getRoiDrawingViewer().paint(gc);
        } finally {
            gl.glPopMatrix();
        }
    }
    

    @Override
    protected void paintLWJGL(ImageListViewCell cell, LWJGLRenderer renderer, Map<String,Object> sharedContextData) {
        cell.getDisplayedModelElement();
        ViskitGC gc = new ViskitGC(renderer);
        GL11.glPushMatrix();
        try {
            Point2D centerOffset = cell.getCenterOffset();
            float scale = (float) cell.getScale();
            float w2 = (float) cell.getDisplayedModelElement().getImage().getWidth() * scale / 2;
            float h2 = (float) cell.getDisplayedModelElement().getImage().getHeight() * scale / 2;
            Dimension cellSize = cell.getLatestSize();
            GL11.glTranslated(cellSize.getWidth() / 2, cellSize.getHeight() / 2, 0);
            GL11.glTranslated(centerOffset.getX(), centerOffset.getY(), 0);
            GL11.glTranslated(-w2, -h2, 0);

            cell.getRoiDrawingViewer().paint(gc);
        } finally {
            GL11.glPopMatrix();
        }
    }
    
    private AffineTransform getDicomToUiTransform(ImageListViewCell cell) {
        double z = cell.getScale();
        return AffineTransform.getScaleInstance(z, z);
    }

    private Point2D getScaledImageSize(ImageListViewCell cell) {
        ViskitImage img = cell.getDisplayedModelElement().getImage();
        return getDicomToUiTransform(cell).transform(new Point2D.Double(img.getWidth(), img.getHeight()), null);
    }

    protected Point2D getImageOffset(ImageListViewCell cell) {
        Point2D imgSize = getScaledImageSize(cell);
        Dimension latestSize = cell.getLatestSize();
        return new Point2D.Double((latestSize.width + 2 * cell.getCenterOffset().getX() - (int) imgSize.getX()) / 2,
                                  (latestSize.height + 2 * cell.getCenterOffset().getY() - (int) imgSize.getY()) / 2);
    }
}