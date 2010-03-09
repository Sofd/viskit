package de.sofd.viskit.controllers;

import de.sofd.util.FloatRange;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 *
 * @author olaf
 */
public class ImageListViewMouseWindowingController {

    // TODO: make these parameterizable
    private static final int WINDOWING_MOUSE_BUTTON = MouseEvent.BUTTON3;
    private static final int WINDOWING_MOUSE_MASK = MouseEvent.BUTTON3_MASK;

    protected JImageListView controlledImageListView;
    public static final String PROP_CONTROLLEDIMAGELISTVIEW = "controlledImageListView";
    private int zOrder;
    public static final String PROP_ZORDER = "zOrder";
    protected static final int DEFAULT_ZORDER = JImageListView.PAINT_ZORDER_LABELS + 20;

    public ImageListViewMouseWindowingController() {
        this(null, DEFAULT_ZORDER);
    }

    public ImageListViewMouseWindowingController(JImageListView controlledImageListView) {
        this(controlledImageListView, DEFAULT_ZORDER);
    }

    public ImageListViewMouseWindowingController(JImageListView controlledImageListView, int zOrder) {
        if (controlledImageListView != null) {
            setControlledImageListView(controlledImageListView);
        }
        setZOrder(zOrder);
    }

    /**
     * Get the value of controlledImageListView
     *
     * @return the value of controlledImageListView
     */
    public JImageListView getControlledImageListView() {
        return controlledImageListView;
    }

    /**
     * Set the value of controlledImageListView
     *
     * @param controlledImageListView new value of controlledImageListView
     */
    public void setControlledImageListView(JImageListView controlledImageListView) {
        JImageListView oldControlledImageListView = this.controlledImageListView;
        this.controlledImageListView = controlledImageListView;
        if (null != oldControlledImageListView) {
            oldControlledImageListView.removeCellMouseListener(windowingCellMouseListener);
            oldControlledImageListView.removeCellMouseMotionListener(windowingCellMouseListener);
        }
        if (null != controlledImageListView) {
            controlledImageListView.addCellMouseListener(getZOrder(), windowingCellMouseListener);
            controlledImageListView.addCellMouseMotionListener(getZOrder(), windowingCellMouseListener);
        }
        propertyChangeSupport.firePropertyChange(PROP_CONTROLLEDIMAGELISTVIEW, oldControlledImageListView, controlledImageListView);
    }

    /**
     * Get the value of zOrder
     *
     * @return the value of zOrder
     */
    public int getZOrder() {
        return zOrder;
    }
    
    /**
     * Set the value of zOrder
     *
     * @param enabled new value of zOrder
     */
    public void setZOrder(int zOrder) {
        int oldZOrder = this.zOrder;
        this.zOrder = zOrder;
        propertyChangeSupport.firePropertyChange(PROP_ZORDER, oldZOrder, zOrder);
        if (controlledImageListView != null) {
            controlledImageListView.removeCellMouseListener(windowingCellMouseListener);
            controlledImageListView.removeCellMouseMotionListener(windowingCellMouseListener);
            controlledImageListView.addCellMouseListener(zOrder, windowingCellMouseListener);
            controlledImageListView.addCellMouseMotionListener(zOrder, windowingCellMouseListener);
            controlledImageListView.refreshCells();
        }
    }

    private MouseAdapter windowingCellMouseListener = new MouseAdapter() {
        private ImageListViewCell currentCell;
        private Point lastPosition;

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == WINDOWING_MOUSE_BUTTON || (e.getModifiers() & WINDOWING_MOUSE_MASK) != 0) {
                currentCell = (ImageListViewCell) e.getSource();
                lastPosition = e.getPoint();
                e.consume();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (e.getButton() == WINDOWING_MOUSE_BUTTON || (e.getModifiers() & WINDOWING_MOUSE_MASK) != 0) {
                ImageListViewCell sourceCell = (ImageListViewCell) e.getSource();
                if (sourceCell != null && sourceCell == currentCell) {
                    FloatRange usedPxRange = sourceCell.getDisplayedModelElement().getUsedPixelValuesRange();
                    int mouseIncrement = 1 + (int)(usedPxRange.getDelta() / 300);
                    // TODO: floating-point mouseIncrements would be better...
                    // TODO: account for cell size as well maybe? (bigger mouse increment when the cell is small)
                    Point sourcePosition = e.getPoint();
                    sourceCell.setWindowLocation(sourceCell.getWindowLocation() + mouseIncrement * (sourcePosition.x - lastPosition.x));
                    int newWW = sourceCell.getWindowWidth() + mouseIncrement * (sourcePosition.y - lastPosition.y);
                    if (newWW < 1) {
                        newWW = 1;
                    }
                    sourceCell.setWindowWidth(newWW);
                    lastPosition = sourcePosition;
                    e.consume();
                }
            }
        }
    };

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

}
