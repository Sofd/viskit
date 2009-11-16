package de.sofd.viskit.controllers;

import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Controller that references a JImageListView and an "enabled" flag.
 * When enabled, the controller tracks any outside changes to the windowing
 * parameters of any of the list's cells and copies those changes to all
 * other cells of the list.
 *
 * @author olaf
 */
public class ImageListViewWindowingApplyToAllController {

    protected JImageListView controlledImageListView;
    public static final String PROP_CONTROLLEDIMAGELISTVIEW = "controlledImageListView";
    private boolean enabled;
    public static final String PROP_ENABLED = "enabled";

    public ImageListViewWindowingApplyToAllController() {
    }

    public ImageListViewWindowingApplyToAllController(JImageListView controlledImageListView) {
        setControlledImageListView(controlledImageListView);
    }

    /**
     * Get the value of enabled
     *
     * @return the value of enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set the value of enabled
     *
     * @param enabled new value of enabled
     */
    public void setEnabled(boolean enabled) {
        boolean oldEnabled = this.enabled;
        this.enabled = enabled;
        propertyChangeSupport.firePropertyChange(PROP_ENABLED, oldEnabled, enabled);
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
            oldControlledImageListView.removeCellPropertyChangeListener(cellWindowingChangeHandler);
        }
        if (null != controlledImageListView) {
            controlledImageListView.addCellPropertyChangeListener(cellWindowingChangeHandler);
        }
        propertyChangeSupport.firePropertyChange(PROP_CONTROLLEDIMAGELISTVIEW, oldControlledImageListView, controlledImageListView);
    }

    private PropertyChangeListener cellWindowingChangeHandler = new PropertyChangeListener() {
        private boolean inProgrammedChange = false;
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!isEnabled()) {
                return;
            }
            if (inProgrammedChange) {
                return;
            }
            if (!evt.getPropertyName().equals(ImageListViewCell.PROP_WINDOWLOCATION) &&
                !evt.getPropertyName().equals(ImageListViewCell.PROP_WINDOWWIDTH)) {
                return;
            }
            ImageListViewCell sourceCell = (ImageListViewCell) evt.getSource();
            for (int i = 0; i < controlledImageListView.getLength(); i++) {
                ImageListViewCell targetCell = controlledImageListView.getCell(i);
                if (targetCell != sourceCell) {
                    inProgrammedChange = true;
                    try {
                        targetCell.setWindowLocation(sourceCell.getWindowLocation());
                        targetCell.setWindowWidth(sourceCell.getWindowWidth());
                    } finally {
                        inProgrammedChange = false;
                    }
                }
            }
        }
    };

    // TODO: what about cells being added to the list?

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
