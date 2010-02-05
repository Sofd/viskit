package de.sofd.viskit.controllers;

import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 *
 * @author olaf
 */
public class ImageListViewMouseZoomPanController {

    // TODO: make these parameterizable
    private static final int MOUSE_BUTTON = MouseEvent.BUTTON2;
    private static final int MOUSE_MASK = MouseEvent.BUTTON2_MASK;

    protected JImageListView controlledImageListView;
    public static final String PROP_CONTROLLEDIMAGELISTVIEW = "controlledImageListView";

    public ImageListViewMouseZoomPanController() {
    }

    public ImageListViewMouseZoomPanController(JImageListView controlledImageListView) {
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
            oldControlledImageListView.removeCellMouseListener(mouseHandler);
            oldControlledImageListView.removeCellMouseMotionListener(mouseHandler);
            oldControlledImageListView.removeCellMouseWheelListener(mouseHandler);
        }
        if (null != controlledImageListView) {
            controlledImageListView.addCellMouseListener(mouseHandler);
            controlledImageListView.addCellMouseMotionListener(mouseHandler);
            controlledImageListView.addCellMouseWheelListener(mouseHandler);
        }
        propertyChangeSupport.firePropertyChange(PROP_CONTROLLEDIMAGELISTVIEW, oldControlledImageListView, controlledImageListView);
    }

    private MouseAdapter mouseHandler = new MouseAdapter() {
        //TODO: this is a MouseCellEventHandler, i.e. the event source is the cell, not the list.
        //   and the coordinates are relative to the cell. Modify code to take that into account.

        private int translateLastX, translateLastY;

        public void changeScaleAndTranslationOfCell(ImageListViewCell cell, double scaleChange, Point translationChange) {
            double newScale = cell.getScale() * scaleChange;
            if (newScale > 0.1 && newScale < 10) {
                cell.setScale(newScale);
            }
            Point2D centerOffset = cell.getCenterOffset();
            cell.setCenterOffset(centerOffset.getX() + translationChange.x,
                                 centerOffset.getY() + translationChange.y);
            cell.refresh();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            translateLastX = -1;
            translateLastY = -1;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (controlledImageListView.getModel().getSize() > 0) {
                if (e.isShiftDown() && (e.getButton() == MOUSE_BUTTON || (e.getModifiers() & MOUSE_MASK) != 0)) {
                    if (translateLastX == -1 || translateLastY == -1) {
                        translateLastX = e.getX();
                        translateLastY = e.getY();
                        return;
                    }
                    ImageListViewCell cell = (ImageListViewCell) e.getSource();
                    changeScaleAndTranslationOfCell(cell, 1.0, new Point(e.getX() - translateLastX, e.getY() - translateLastY));
                    translateLastX = e.getX();
                    translateLastY = e.getY();
                    e.consume();
                }
            }
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (e.isShiftDown()) {
                double scaleChange = (e.getWheelRotation() < 0 ? 110.0/100.0 : 100.0/110.0);
                ImageListViewCell cell = (ImageListViewCell) e.getSource();
                changeScaleAndTranslationOfCell(cell, scaleChange, new Point(0, 0));
                e.consume();
            }
        }

        @Override
        public void mouseClicked(MouseEvent evt) {
            if (controlledImageListView.getModel().getSize() > 0) {
                if (evt.getClickCount() == 2 && evt.isShiftDown() && (evt.getButton() == MOUSE_BUTTON || (evt.getModifiers() & MOUSE_MASK) != 0)) {
                    ImageListViewCell cell = (ImageListViewCell) evt.getSource();
                    if (cell != null) {
                        cell.setCenterOffset(new Point2D.Double(0, 0));
                        cell.refresh();
                        evt.consume();
                    }
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
