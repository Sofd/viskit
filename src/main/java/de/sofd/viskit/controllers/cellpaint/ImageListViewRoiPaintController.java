package de.sofd.viskit.controllers.cellpaint;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import de.sofd.viskit.image.ViskitImage;
import de.sofd.viskit.ui.imagelist.ImageListView;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintEvent;

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
    protected void paint(ImageListViewCellPaintEvent e) {
        getControlledImageListView().getBackend().paintCellROIs(e);
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