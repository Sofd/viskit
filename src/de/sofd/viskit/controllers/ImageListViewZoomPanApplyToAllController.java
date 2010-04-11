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
 * TODO: This is 95% copy&paste from
 * {@link ImageListViewWindowingApplyToAllController}. Generalize to
 * ImageListViewCellPropChangeApplyToAllController, and/or a common superclass
 * 
 * @author olaf
 */
public class ImageListViewZoomPanApplyToAllController {

    protected JImageListView controlledImageListView;
    public static final String PROP_CONTROLLEDIMAGELISTVIEW = "controlledImageListView";
    private boolean enabled;
    public static final String PROP_ENABLED = "enabled";
    protected boolean ignoreNonInteractiveChanges = true;
    protected boolean isInhibited = false;

    /**
     * Constant to be passed to the {@link DynScope}facility to implement
     * {@link #runWithAllControllersInhibited(java.lang.Runnable) }. Won't use
     * a static boolean variable for that because we want this to be thread-local.
     */
    protected static final String DSK_GLOBAL_INHIBIT = ImageListViewZoomPanApplyToAllController.class.getName() + ".dynscope_global_inhibit";

    public ImageListViewZoomPanApplyToAllController() {
    }

    public ImageListViewZoomPanApplyToAllController(JImageListView controlledImageListView) {
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
     * Get the value of ignoreNonInteractiveChanges
     *
     * @return the value of ignoreNonInteractiveChanges
     */
    public boolean isIgnoreNonInteractiveChanges() {
        return ignoreNonInteractiveChanges;
    }

    /**
     * Set the value of ignoreNonInteractiveChanges
     *
     * @param ignoreNonInteractiveChanges new value of ignoreNonInteractiveChanges
     */
    public void setIgnoreNonInteractiveChanges(boolean ignoreNonInteractiveChanges) {
        this.ignoreNonInteractiveChanges = ignoreNonInteractiveChanges;
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
            oldControlledImageListView.removeCellPropertyChangeListener(cellPropChangeHandler);
        }
        if (null != controlledImageListView) {
            controlledImageListView.addCellPropertyChangeListener(cellPropChangeHandler);
        }
        propertyChangeSupport.firePropertyChange(PROP_CONTROLLEDIMAGELISTVIEW, oldControlledImageListView, controlledImageListView);
    }

    /**
     * Disable this controller, run the Runnable, and enable the controller
     * again (if it was enabled before).
     * 
     * @param r
     */
    public void runWithControllerInhibited(Runnable r) {
        boolean wasInhibited = isInhibited;
        isInhibited = true;
        try {
            r.run();
        } finally {
            isInhibited = wasInhibited;
        }
    }

    /**
     * Disable all instances of this class, run the Runnable, and enable the
     * instances again (those that were enabled before).
     * 
     * @param r
     */
    public static void runWithAllControllersInhibited(Runnable r) {
        DynScope.runWith(DSK_GLOBAL_INHIBIT, r);
    }

    protected boolean isInhibited() {
        return isInhibited || DynScope.contains(DSK_GLOBAL_INHIBIT);
    }

    private PropertyChangeListener cellPropChangeHandler = new PropertyChangeListener() {
        private boolean inProgrammedChange = false;
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!isEnabled()) {
                return;
            }
            if (inProgrammedChange) {
                return;
            }
            if (!evt.getPropertyName().equals(ImageListViewCell.PROP_SCALE) &&
                !evt.getPropertyName().equals(ImageListViewCell.PROP_CENTEROFFSET)) {
                return;
            }
            if (isInhibited()) {
                return;
            }
            if (isIgnoreNonInteractiveChanges()) {
                ImageListViewCell sourceCell = (ImageListViewCell) evt.getSource();
                if (! sourceCell.isInteractivelyChangingProp(evt.getPropertyName())) {
                    return;
                }
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
                        targetCell.setScale(sourceCell.getScale());
                        targetCell.setCenterOffset(sourceCell.getCenterOffset());
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
