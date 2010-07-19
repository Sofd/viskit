package de.sofd.viskit.ui.imagelist.cellviewers.java2d;

import java.awt.Graphics;
import java.awt.Graphics2D;

import de.sofd.viskit.draw2d.gc.ViskitGC;
import de.sofd.viskit.model.NotInitializedException;
import de.sofd.viskit.model.ImageListViewModelElement.InitializationState;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.cellviewers.BaseImageListViewCellViewer;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintEvent;

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
            getDisplayedCell().getDisplayedModelElement().setInitializationState(InitializationState.UNINITIALIZED);
            getDisplayedCell().getOwner().fireCellPaintEvent(new ImageListViewCellPaintEvent(getDisplayedCell(), new ViskitGC(g2d), null, null));
        }
    }

}
