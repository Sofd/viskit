package de.sofd.viskit.ui.imagelist.cellviewers.java2d;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import de.sofd.viskit.draw2d.gc.ViskitGC;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;
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

        // Call all CellPaintListeners below the image in the z-order.
        // Eventually all painting, including the image and ROIs, should happen in PaintListeners.
        getDisplayedCell().getOwner().fireCellPaintEvent(new ImageListViewCellPaintEvent(getDisplayedCell(), new ViskitGC(g2d), null, null),
                                                         Integer.MIN_VALUE, JImageListView.PAINT_ZORDER_IMAGE);
        
        // render the ROI DrawingViewer
        Graphics2D userGraphics = (Graphics2D)g2d.create();
        Point2D imageOffset = getImageOffset();
        userGraphics.transform(AffineTransform.getTranslateInstance(imageOffset.getX(), imageOffset.getY()));
        displayedCell.getRoiDrawingViewer().paint(new ViskitGC(userGraphics));

        // image and ROIs have been drawn. Now call all CellPaintListeners above the ROIs in the z-order.
        // Eventually all painting, including the image and ROIs, should happen in PaintListeners.
        getDisplayedCell().getOwner().fireCellPaintEvent(new ImageListViewCellPaintEvent(getDisplayedCell(), new ViskitGC(g2d), null, null),
                                                         JImageListView.PAINT_ZORDER_ROI + 1, Integer.MAX_VALUE);
    }

}
