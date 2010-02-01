package de.sofd.viskit.controllers;

import de.sofd.util.DynScope;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Controller that references a JImageListView and an "enabled" flag. When
 * enabled, the controller tracks any outside changes to the windowing
 * parameters of any of the list's cells and copies those changes to all other
 * cells of the list.
 * <p>
 * The special {@link DynScope} constant {@link #DSK_INHIBIT} is provided for
 * external parties to temporarily disable this controller in any invocations
 * down the callstack from a specific point in an external control flow.
 * 
 * @author olaf
 */
public class ImageListViewWindowingApplyToAllController {

    protected JImageListView controlledImageListView;
    public static final String PROP_CONTROLLEDIMAGELISTVIEW = "controlledImageListView";
    private boolean enabled;
    public static final String PROP_ENABLED = "enabled";

    /**
     * Constant to be passed to the {@link DynScope}facility to inhibit any
     * activity of instances of this controller down the callstack from a
     * specific point. Sample usage:
     * 
     * <code>
     * DynScope.runWith(ImageListViewWindowingApplyToAllController.DSK_INHIBIT, new Runnable() {
     *    @Override
     *    public void run() {
     *        // inside this method and in any piece of code called from here,
     *        // any ImageListViewWindowingApplyToAllController invocation code
     *        // will be disabled
     *        ...
     *        // e.g.:
     *        JImageListView someList = ...;
     *        someList.getCell(xx).setWindowWidth(500); // this change WON'T be copied to other cells of someList,
     *                                                  // even if a ImageListViewWindowingApplyToAllController is
     *                                                  // set and enabled for it.
     *    }
     * }
     * </code>
     */
    public static final String DSK_INHIBIT = ImageListViewWindowingApplyToAllController.class.getName() + ".dynscope_inhibit";

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
            if (DynScope.contains(DSK_INHIBIT)) {
                return;
            }
            ImageListViewCell sourceCell = (ImageListViewCell) evt.getSource();
            for (int i = 0; i < controlledImageListView.getLength(); i++) {
                ImageListViewCell targetCell = controlledImageListView.getCell(i);
                if (targetCell != null && targetCell != sourceCell) {
                    // targetCell != null test because targetCell may be null null under some circumstances,
                    // e.g. if the windowing is set in a cellCreated handler for a newly created cell, which
                    // may have been created b/c of a dynamic JImageListView#setModel call, which ends up
                    // firing the cellCreared event before adding the cell to
                    // the internal cell list that getCell(int) uses
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
