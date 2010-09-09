package de.sofd.viskit.controllers;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import de.sofd.util.FloatRange;
import de.sofd.viskit.ui.imagelist.ImageListView;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;

/**
 *
 * @author olaf
 */
public class ImageListViewMouseWindowingController {

    // TODO: make these parameterizable
    private static final int WINDOWING_MOUSE_BUTTON = MouseEvent.BUTTON3;
    private static final int WINDOWING_MOUSE_MASK = MouseEvent.BUTTON3_MASK;

    protected ImageListView controlledImageListView;
    public static final String PROP_CONTROLLEDIMAGELISTVIEW = "controlledImageListView";
    private int zOrder;
    public static final String PROP_ZORDER = "zOrder";
    protected static final int DEFAULT_ZORDER = ImageListView.PAINT_ZORDER_LABELS + 20;

    public ImageListViewMouseWindowingController() {
        this(null, DEFAULT_ZORDER);
    }

    public ImageListViewMouseWindowingController(ImageListView controlledImageListView) {
        this(controlledImageListView, DEFAULT_ZORDER);
    }

    public ImageListViewMouseWindowingController(ImageListView controlledImageListView, int zOrder) {
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
    public ImageListView getControlledImageListView() {
        return controlledImageListView;
    }

    /**
     * Set the value of controlledImageListView
     *
     * @param controlledImageListView new value of controlledImageListView
     */
    public void setControlledImageListView(ImageListView controlledImageListView) {
        ImageListView oldControlledImageListView = this.controlledImageListView;
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
                final ImageListViewCell sourceCell = (ImageListViewCell) e.getSource();
                if (sourceCell != null && sourceCell == currentCell) {
                    FloatRange usedPxRange = sourceCell.getDisplayedModelElement().getUsedPixelValuesRange();
                    final int mouseIncrement = 1 + (int)(usedPxRange.getDelta() / 300);
                    // TODO: floating-point mouseIncrements would be better...
                    // TODO: account for cell size as well maybe? (bigger mouse increment when the cell is small)
                    final Point sourcePosition = e.getPoint();
                    sourceCell.runWithPropChangingInteractively(ImageListViewCell.PROP_WINDOWLOCATION, new Runnable() {
                        @Override
                        public void run() {
                            sourceCell.setWindowLocation(sourceCell.getWindowLocation() + mouseIncrement * (sourcePosition.x - lastPosition.x));
                        }
                    });
                    int tmpNewWW = sourceCell.getWindowWidth() + mouseIncrement * (sourcePosition.y - lastPosition.y);
                    final int newWW = tmpNewWW < 1 ? 1 : tmpNewWW;
                    sourceCell.runWithPropChangingInteractively(ImageListViewCell.PROP_WINDOWWIDTH, new Runnable() {
                        @Override
                        public void run() {
                            sourceCell.setWindowWidth(newWW);
                        }
                    });
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
