package de.sofd.viskit.controllers.cellpaint;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import de.sofd.viskit.ui.imagelist.ImageListView;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintEvent;

/**
 * Controller that draws a lookup table legend to the cell elements. The size of
 * the legend can be changed. The position of the legend is adjusted to the cell
 * size. If the cell size is smaller than a minimum cell size, the legend will
 * not be drawn. The scale type (absolute windowing values or fixed percentage values)
 * and the intervals can be set.
 * 
 * @author honglinh
 * 
 */
public class ImageListViewPrintLUTController extends CellPaintControllerBase {

    public static enum ScaleType {
        ABSOLUTE, PERCENTAGE
    };
    
    
    private ScaleType type = ScaleType.ABSOLUTE;
    private int intervals = 3;
    private Color textColor = Color.green;
    private int lutWidth = 30;
    private int lutHeight = 200;

    private int cellDistance = 10;

    public ImageListViewPrintLUTController() {
        this(null, ImageListView.PAINT_ZORDER_LABELS);
    }

    public ImageListViewPrintLUTController(int intervals, ScaleType type) {
        this(null, ImageListView.PAINT_ZORDER_LABELS);
        checkInterval(intervals);
        this.type = type;
        this.intervals = intervals;
    }

    public ImageListViewPrintLUTController(ImageListView controlledImageListView) {
        super(controlledImageListView, ImageListView.PAINT_ZORDER_LABELS);
    }

    public ImageListViewPrintLUTController(ImageListView controlledImageListView, int intervals, ScaleType type) {
        this(controlledImageListView, ImageListView.PAINT_ZORDER_LABELS);
        checkInterval(intervals);
        this.type = type;
        this.intervals = intervals;
    }

    public ImageListViewPrintLUTController(ImageListView controlledImageListView, int zOrder) {
        super(controlledImageListView, zOrder);
    }

    public ImageListViewPrintLUTController(ImageListView controlledImageListView, int zOrder, int intervals,
            ScaleType type) {
        this(controlledImageListView, zOrder);
        checkInterval(intervals);
        this.type = type;
        this.intervals = intervals;
    }
    

    @Override
    protected void paint(ImageListViewCellPaintEvent evt) {
        ImageListViewCell cell = evt.getSource();

        Dimension cellSize = cell.getLatestSize();
        double cellHeight = cellSize.getHeight();
        double cellWidth = cellSize.getWidth();

        // no lookup table assigned
        if (cell.getLookupTable() == null) {
            return;
        }
        // cell size is too small, do not display the lut
        if (cellHeight < lutHeight + 50 || cellWidth < lutHeight + 50) {
            return;
        }
        // get scale value list
        List<String> scaleList = getScaleList(cell);

        // calculate lut and text position
        Point2D lutPosition = calculateLutPosition(cellSize);
        Point2D textPosition = calculateTextPosition(lutPosition);
        
        getControlledImageListView().getBackend().printLUTIntoCell(evt, lutWidth, lutHeight, intervals, lutPosition, textPosition, textColor, scaleList);
    }

    /**
     * depending on scale type calculate the scale values
     * 
     * @param cell
     * @return
     */
    private List<String> getScaleList(ImageListViewCell cell) {
        List<String> scaleList = new ArrayList<String>();

        float upperBoundary = 0.0f;
        float lowerBoundary = 0.0f;

        switch (type) {
        case ABSOLUTE:
            int wl = cell.getWindowLocation();
            int ww = cell.getWindowWidth();
            upperBoundary = (float) (wl + ww / 2);
            lowerBoundary = (float) (wl - ww / 2);
            break;
        case PERCENTAGE:
            upperBoundary = 100.0f;
            lowerBoundary = 0.0f;
            break;
        }

        float unit = (Math.abs(upperBoundary) + Math.abs(lowerBoundary)) / intervals;

        scaleList.add(String.valueOf((int) upperBoundary));
        for (int i = 1; i <= intervals - 1; i++) {
            scaleList.add(String.valueOf((int) (upperBoundary - unit * i)));
        }
        scaleList.add(String.valueOf((int) lowerBoundary));
        return scaleList;
    }

    /**
     * calculate the top left corner of the lut legend
     * 
     * @param cellSize
     * @return
     */
    private Point2D calculateLutPosition(Dimension cellSize) {
        return new Point2D.Double(cellSize.getWidth() - cellDistance - lutWidth, cellSize.getHeight() / 2 - lutHeight
                / 2);
    }

    /**
     * 
     * calculate the the position of the top scale value
     * 
     * @param lutPosition
     * @return
     */
    private Point2D calculateTextPosition(Point2D lutPosition) {
        return new Point2D.Double(lutPosition.getX(), lutPosition.getY() + 10);
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    public int getLutWidth() {
        return lutWidth;
    }

    public void setLutWidth(int lutWidth) {
        this.lutWidth = lutWidth;
    }

    public int getLutHeight() {
        return lutHeight;
    }

    public void setLutHeight(int lutHeight) {
        this.lutHeight = lutHeight;
    }
    
    public ScaleType getType() {
        return type;
    }

    public void setType(ScaleType type) {
        this.type = type;
    }

    public int getIntervals() {
        return intervals;
    }

    /**
     * set the number of intervals of the scale. The number of intervals must be
     * at least 1
     * 
     * @param intervals
     */
    public void setIntervals(int intervals) {
        checkInterval(intervals);
        this.intervals = intervals;
    }
    
    private void checkInterval(int interval) {
        if (intervals < 1) {
            throw new IllegalArgumentException("the number of intervals must be at least one!");
        }
    }
}