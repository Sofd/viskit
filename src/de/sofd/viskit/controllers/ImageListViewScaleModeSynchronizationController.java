package de.sofd.viskit.controllers;

import de.sofd.viskit.ui.imagelist.JImageListView;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller that maintains a list of references to {@link JImageListView} objects
 * and a boolean "enabled" flag. If the flag is set to true, the scale modes of
 * all the lists are kept synchronized.
 *
 * @author Sofd GmbH
 */
public class ImageListViewScaleModeSynchronizationController {

    private final List<JImageListView> lists = new ArrayList<JImageListView>();
    private boolean enabled;
    public static final String PROP_ENABLED = "enabled";

    public ImageListViewScaleModeSynchronizationController() {
    }

    public ImageListViewScaleModeSynchronizationController(JImageListView... lists) {
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
        // TODO: deal with selection model changes on the lists?
        for (JImageListView lv : this.lists) {
            lv.removePropertyChangeListener(scaleModeChangeHandler);
        }
        this.lists.clear();
        for (JImageListView lv : lists) {
            this.lists.add(lv);
            lv.addPropertyChangeListener(scaleModeChangeHandler);
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


    private PropertyChangeListener scaleModeChangeHandler = new PropertyChangeListener() {
        private boolean inProgrammedChange = false;
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!isEnabled()) {
                return;
            }
            if (! JImageListView.PROP_SCALEMODE.equals(evt.getPropertyName())) {
                return;
            }
            if (inProgrammedChange) {
                return;
            }
            JImageListView source = (JImageListView) evt.getSource();
            for (JImageListView l : getLists()) {
                if (l != source) {
                    inProgrammedChange = true;
                    try {
                        l.setScaleMode(source.getScaleMode());
                    } finally {
                        inProgrammedChange = false;
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