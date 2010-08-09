package de.sofd.viskit.ui.imagelist.cellviewers.java2d;

import java.awt.Graphics;
import java.awt.Graphics2D;

import org.apache.log4j.Logger;

import de.sofd.viskit.draw2d.gc.ViskitGC;
import de.sofd.viskit.model.NotInitializedException;
import de.sofd.viskit.model.ImageListViewModelElement.InitializationState;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.cellviewers.BaseImageListViewCellViewer;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintEvent;
import de.sofd.viskit.ui.imagelist.gridlistimpl.JGridImageListView;

/**
 * Swing component for displaying a {@link ImageListViewCell}. For use in cell renderers
 * or elsewhere.
 *
 * @author olaf
 */
public class ImageListViewCellViewer extends BaseImageListViewCellViewer {

    static final Logger logger = Logger.getLogger(JGridImageListView.class);

    public ImageListViewCellViewer(ImageListViewCell cell) {
        super(cell);
    }

    @Override
    public void setDisplayedCell(ImageListViewCell displayedCell) {
        super.setDisplayedCell(displayedCell);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g2d);

        displayedCell.setLatestSize(getSize());

        try {
            // call all CellPaintListeners in the z-order
            getDisplayedCell().getOwner().fireCellPaintEvent(new ImageListViewCellPaintEvent(getDisplayedCell(), new ViskitGC(g2d), null, null));
        } catch (NotInitializedException e) {
            // a paint listener indicated that the cell's model element is uninitialized.
            // set the element's initializationState accordingly, repaint everything to let paint listeners to draw the right thing
            // TODO: clear out the cell before?
            logger.debug("NotInitializedException drawing " + getDisplayedCell().getDisplayedModelElement(), e);
            getDisplayedCell().getDisplayedModelElement().setInitializationState(InitializationState.UNINITIALIZED);
            getDisplayedCell().getOwner().fireCellPaintEvent(new ImageListViewCellPaintEvent(getDisplayedCell(), new ViskitGC(g2d), null, null));
        } catch (Exception e) {
            logger.error("Exception drawing " + getDisplayedCell().getDisplayedModelElement() + ". Setting the model elt to permanent ERROR state.", e);
            //TODO: support the notion of "temporary" errors, for which we would not change the initializationState?
            getDisplayedCell().getDisplayedModelElement().setInitializationState(InitializationState.ERROR);
            getDisplayedCell().getDisplayedModelElement().setErrorInfo(e);
        }
    }

}
