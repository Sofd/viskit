package de.sofd.viskit.controllers;

import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
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
    private static final int GREYSCALE_RANGE = 4096; // 12 bits. But may depend on image...

    protected JImageListView controlledImageListView;
    public static final String PROP_CONTROLLEDIMAGELISTVIEW = "controlledImageListView";

    public ImageListViewMouseWindowingController() {
    }

    public ImageListViewMouseWindowingController(JImageListView controlledImageListView) {
        setControlledImageListView(controlledImageListView);
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
            oldControlledImageListView.removeCellMouseMotionListener(windowingCellMouseListener);
        }
        if (null != controlledImageListView) {
            controlledImageListView.addCellMouseMotionListener(windowingCellMouseListener);
        }
        propertyChangeSupport.firePropertyChange(PROP_CONTROLLEDIMAGELISTVIEW, oldControlledImageListView, controlledImageListView);
    }

    // TODO: only change windowing if mouseDown is received first on the same cell
    
    private MouseMotionListener windowingCellMouseListener = new MouseMotionAdapter() {
        private ImageListViewCell lastCell;
        private Point lastPosition;

        @Override
        public void mouseDragged(MouseEvent e) {
            if (e.getButton() == WINDOWING_MOUSE_BUTTON || (e.getModifiers() & WINDOWING_MOUSE_MASK) != 0) {
                ImageListViewCell sourceCell = (ImageListViewCell) e.getSource();
                Point sourcePosition = e.getPoint();
                if (sourceCell != null && sourceCell == lastCell) {
                    sourceCell.setWindowLocation(sourceCell.getWindowLocation() + sourcePosition.x - lastPosition.x);
                    sourceCell.setWindowWidth(sourceCell.getWindowWidth() + sourcePosition.y - lastPosition.y);
                }
                lastCell = sourceCell;
                lastPosition = sourcePosition;
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
