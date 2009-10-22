package de.sofd.viskit.controllers;

import de.sofd.viskit.ui.RoiToolPanel;
import de.sofd.viskit.ui.imagelist.JImageListView;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller that maintains a list of references to {@link JImageListView} objects,
 * a reference to a {@link RoiToolPanel}, and
 * and a boolean "enabled" flag. If the flag is set to true, a DrawingViewer tool class
 * being selected in the panel leads to an instance of that tool being activated on
 * all cells of all the JImageListViews. Also, immediately after a drawing object has
 * been created in any of the cells, a SelectorTool is re-activated on all the cells.
 *
 * @author Sofd GmbH
 */
public class ImageListViewRoiToolApplicationController {

    private final List<JImageListView> lists = new ArrayList<JImageListView>();
    private RoiToolPanel roiToolPanel;
    private boolean enabled;
    public static final String PROP_ENABLED = "enabled";

    public ImageListViewRoiToolApplicationController() {
    }

    public ImageListViewRoiToolApplicationController(JImageListView... lists) {
        setLists(lists);
    }

    /**
     * Get the value of lists
     *
     * @return the value of lists
     */
    public JImageListView[] getLists() {
        return (JImageListView[]) lists.toArray(new JImageListView[0]);
    }

    /**
     * Set the value of lists
     *
     * @param lists new value of lists
     */
    public void setLists(JImageListView[] lists) {
        for (JImageListView lv : this.lists) {
            //lv.removePropertyChangeListener(scaleModeChangeHandler);
        }
        this.lists.clear();
        for (JImageListView lv : lists) {
            this.lists.add(lv);
            //lv.addPropertyChangeListener(scaleModeChangeHandler);
        }
    }

    /**
     * Get the value of lists at specified index
     *
     * @param index
     * @return the value of lists at specified index
     */
    public JImageListView getLists(int index) {
        return this.lists.get(index);
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
     * Get the value of roiToolPanel
     *
     * @return the value of roiToolPanel
     */
    public RoiToolPanel getRoiToolPanel() {
        return roiToolPanel;
    }

    /**
     * Set the value of roiToolPanel
     *
     * @param roiToolPanel new value of roiToolPanel
     */
    public void setRoiToolPanel(RoiToolPanel roiToolPanel) {
        this.roiToolPanel = roiToolPanel;
    }

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
