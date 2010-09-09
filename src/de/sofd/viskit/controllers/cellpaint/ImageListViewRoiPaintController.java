package de.sofd.viskit.controllers.cellpaint;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Map;

import javax.media.opengl.GL2;

import org.dcm4che2.data.Tag;

import de.sofd.viskit.draw2d.gc.ViskitGC;
import de.sofd.viskit.model.DicomImageListViewModelElement;
import de.sofd.viskit.model.ImageListViewModelElement;
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
            float w2 = (float) getOriginalImageWidth(cell) * scale / 2;
            float h2 = (float) getOriginalImageHeight(cell) * scale / 2;
            Dimension cellSize = cell.getLatestSize();
            gl.glTranslated(cellSize.getWidth() / 2, cellSize.getHeight() / 2, 0);
            gl.glTranslated(centerOffset.getX(), centerOffset.getY(), 0);
            gl.glTranslated(-w2, -h2, 0);

            cell.getRoiDrawingViewer().paint(gc);
        } finally {
            gl.glPopMatrix();
        }
    }
    
    protected int getOriginalImageWidth(ImageListViewCell cell) {
        ImageListViewModelElement elt = cell.getDisplayedModelElement();
        if (elt instanceof DicomImageListViewModelElement) {
            // performance optimization for this case -- read the value from DICOM metadata instead of getting the image
            DicomImageListViewModelElement dicomElt = (DicomImageListViewModelElement) elt;
            return dicomElt.getDicomImageMetaData().getInt(Tag.Columns);
        } else if (elt.hasRawImage() && elt.isRawImagePreferable()){
            return elt.getRawImage().getWidth();
        } else {
            return elt.getImage().getWidth();
        }
    }

    protected int getOriginalImageHeight(ImageListViewCell cell) {
        ImageListViewModelElement elt = cell.getDisplayedModelElement();
        if (elt instanceof DicomImageListViewModelElement) {
            // performance optimization for this case -- read the value from DICOM metadata instead of getting the image
            DicomImageListViewModelElement dicomElt = (DicomImageListViewModelElement) elt;
            return dicomElt.getDicomImageMetaData().getInt(Tag.Rows);
        } else if (elt.hasRawImage() && elt.isRawImagePreferable()){
            return elt.getRawImage().getHeight();
        } else {
            return elt.getImage().getHeight();
        }
    }
    
    private AffineTransform getDicomToUiTransform(ImageListViewCell cell) {
        double z = cell.getScale();
        return AffineTransform.getScaleInstance(z, z);
    }

    private Point2D getScaledImageSize(ImageListViewCell cell) {
        return getDicomToUiTransform(cell).transform(new Point2D.Double(getOriginalImageWidth(cell), getOriginalImageHeight(cell)), null);
    }

    protected Point2D getImageOffset(ImageListViewCell cell) {
        Point2D imgSize = getScaledImageSize(cell);
        Dimension latestSize = cell.getLatestSize();
        return new Point2D.Double((latestSize.width + 2 * cell.getCenterOffset().getX() - (int) imgSize.getX()) / 2,
                                  (latestSize.height + 2 * cell.getCenterOffset().getY() - (int) imgSize.getY()) / 2);
    }
}