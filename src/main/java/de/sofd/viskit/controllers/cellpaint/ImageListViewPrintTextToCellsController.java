package de.sofd.viskit.controllers.cellpaint;

import java.awt.Color;
import java.awt.geom.Point2D;

import de.sofd.viskit.ui.imagelist.ImageListView;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintEvent;

/**
 * Controller that references a ImageListView and an "enabled" flag. When
 * enabled, the controller prints text into every cell as it is drawn. The
 * cell-relative x/y position of the text as well as the text color can be set,
 * the text to print is obtained through the callback method
 * {@link #getTextToPrint(ImageListViewCell)}, which subclasses must override to
 * print anything useful.
 * <p>
 * Normally you'd write a (possibly anonymous) subclass of this controller and
 * override the {@link #getTextToPrint(ImageListViewCell)} method there.
 * 
 * @author olaf
 */
public class ImageListViewPrintTextToCellsController extends CellPaintControllerBase {
    
    private Color textColor = Color.green;
    private Point2D textPosition = new Point2D.Double(5, 15);
    public static final String PROP_TEXTCOLOR = "textColor";
    public static final String PROP_TEXTPOSITION = "textPosition";

    public ImageListViewPrintTextToCellsController() {
        this(null, ImageListView.PAINT_ZORDER_LABELS);
    }

    public ImageListViewPrintTextToCellsController(ImageListView controlledImageListView) {
        super(controlledImageListView, ImageListView.PAINT_ZORDER_LABELS);
    }

    public ImageListViewPrintTextToCellsController(ImageListView controlledImageListView, int zOrder) {
        super(controlledImageListView, zOrder);
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        Color oldValue = this.textColor;
        this.textColor = textColor;
        propertyChangeSupport.firePropertyChange(PROP_TEXTCOLOR, oldValue , textColor);
    }

    public Point2D getTextPosition() {
        return textPosition;
    }

    public void setTextPosition(int x, int y) {
        setTextPosition(new Point2D.Double(x, y));
    }
    
    public void setTextPosition(Point2D textPosition) {
        Point2D oldValue = this.textPosition;
        this.textPosition = textPosition;
        propertyChangeSupport.firePropertyChange(PROP_TEXTPOSITION, oldValue , textPosition);
    }

    @Override
    protected void paint(ImageListViewCellPaintEvent e) {
        getControlledImageListView().getBackend().printTextIntoCell(
                e,
                getTextToPrint(e.getSource()),
                (int) getTextPosition().getX(),
                (int) getTextPosition().getY(),
                textColor);
    }

    /**
     * Called to obtain the text to print.
     * 
     * @param cell cell to be drawn
     * @return the text, as a String array (one String per line)
     */
    protected String[] getTextToPrint(ImageListViewCell cell) {
        return new String[]{"Hello World"};
    }
    
}
