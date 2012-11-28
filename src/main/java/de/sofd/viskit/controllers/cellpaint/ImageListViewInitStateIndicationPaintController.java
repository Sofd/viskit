package de.sofd.viskit.controllers.cellpaint;

import java.awt.Color;
import java.awt.Dimension;

import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.model.ImageListViewModelElement.InitializationState;
import de.sofd.viskit.ui.imagelist.ImageListView;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintEvent;

/**
 * Cell paint controller that draws indications into a cell if the cell's model
 * element's initialization state (
 * {@link ImageListViewModelElement#getInitializationState()} is UNINITIALIZED
 * or ERROR. Draws nothing if the state is INITIALIZED.
 * 
 * @author olaf
 */
public class ImageListViewInitStateIndicationPaintController extends CellPaintControllerBase {
    
    public ImageListViewInitStateIndicationPaintController() {
        this(null, ImageListView.PAINT_ZORDER_LABELS);
    }

    public ImageListViewInitStateIndicationPaintController(ImageListView controlledImageListView) {
        super(controlledImageListView, ImageListView.PAINT_ZORDER_LABELS);
        setEnabled(true);
    }

    public ImageListViewInitStateIndicationPaintController(ImageListView controlledImageListView, int zOrder) {
        super(controlledImageListView, zOrder);
        setEnabled(true);
    }

    @Override
    protected boolean canPaintInitializationState(InitializationState initState) {
        return initState == InitializationState.UNINITIALIZED || initState == InitializationState.ERROR;
    }
    
    @Override
    protected void paint(ImageListViewCellPaintEvent evt) {
        ImageListViewCell cell = evt.getSource();
        ImageListViewModelElement.InitializationState initState = cell.getDisplayedModelElement().getInitializationState();
        Color textColor;
        String text;
        int textX, textY;
        switch (initState) {
        case UNINITIALIZED:
            textColor = Color.green;
            text = getUninitializedString(cell);
            Dimension sz = cell.getLatestSize();
            textX = sz.width / 2;
            textY = sz.height / 2;
            break;
        case ERROR:
            textColor = Color.red;
            text = getErrorString(cell);
            textX = 0;
            textY = cell.getLatestSize().height / 2;
            break;
        default:
            throw new IllegalStateException("SHOULD NEVER HAPPEN");
        }
        getControlledImageListView().getBackend().paintCellInitStateIndication(evt.getGc(), text, textX, textY, textColor);
    }
    
    protected String getUninitializedString(ImageListViewCell cell) {
        return "...";
    }

    protected String getErrorString(ImageListViewCell cell) {
        ImageListViewModelElement elt = cell.getDisplayedModelElement();
        Object ei = elt.getErrorInfo();
        if (ei == null) {
            return "Error";
        } else {
            if (ei instanceof Throwable) {
                Throwable t = (Throwable) ei;
                if (t.getLocalizedMessage() != null) {
                    return t.getLocalizedMessage();
                } else {
                    return t.toString();
                }
            } else {
                return ei.toString();
            }
        }
    }

}
