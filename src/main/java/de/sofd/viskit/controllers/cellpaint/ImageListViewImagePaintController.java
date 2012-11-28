package de.sofd.viskit.controllers.cellpaint;

import org.apache.log4j.Logger;

import de.sofd.viskit.ui.imagelist.ImageListView;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintEvent;

/**
 * Cell-painting controller that paints the image of the cell's model element
 * (cell.getDisplayedModelElement().getImage()) into the cell.
 * 
 * @author olaf
 */
public class ImageListViewImagePaintController extends CellPaintControllerBase {

    static final Logger logger = Logger.getLogger(ImageListViewImagePaintController.class);
    
    public ImageListViewImagePaintController() {
        this(null, ImageListView.PAINT_ZORDER_IMAGE);
    }

    public ImageListViewImagePaintController(ImageListView controlledImageListView) {
        super(controlledImageListView, ImageListView.PAINT_ZORDER_IMAGE);
    }

    public ImageListViewImagePaintController(ImageListView controlledImageListView, int zOrder) {
        super(controlledImageListView, zOrder);
    }
    
    @Override
    protected void paint(ImageListViewCellPaintEvent e) {
        getControlledImageListView().getBackend().paintCellImage(e);
    }
    
}
