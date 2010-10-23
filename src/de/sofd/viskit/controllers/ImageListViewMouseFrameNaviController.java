package de.sofd.viskit.controllers;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import de.sofd.viskit.model.DicomImageListViewModelElement;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.ImageListView;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;

/**
 *
 * @author olaf
 */
public class ImageListViewMouseFrameNaviController {

    private static final int MODIFIER_MASK = MouseEvent.CTRL_MASK;

    /**
     * Setting an attribute with this name in a model element or (NYI) a cell
     * to a non-null value inhibits this controller on that element.
     */
    public static final String PN_INHIBIT = ImageListViewMouseFrameNaviController.class.getName() + ".inhibit";

    protected ImageListView controlledImageListView;
    public static final String PROP_CONTROLLEDIMAGELISTVIEW = "controlledImageListView";

    public ImageListViewMouseFrameNaviController() {
    }

    public ImageListViewMouseFrameNaviController(ImageListView controlledImageListView) {
        setControlledImageListView(controlledImageListView);
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

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if ((e.getModifiers() & MODIFIER_MASK) != 0) {
                ImageListViewCell cell = (ImageListViewCell) e.getSource();
                ImageListViewModelElement elt = cell.getDisplayedModelElement();
                if (elt.getAttribute(PN_INHIBIT) != null) {
                    return;
                }
                if (elt instanceof DicomImageListViewModelElement) {
                    DicomImageListViewModelElement delt = (DicomImageListViewModelElement) elt;
                    int fnum = delt.getFrameNumber();
                    int fcount = delt.getTotalFrameNumber();
                    fnum += (e.getWheelRotation() < 0 ? -1 : 1);
                    fnum = (fnum + fcount) % fcount;
                    delt.setFrameNumber(fnum);
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
