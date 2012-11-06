package de.sofd.viskit.controllers;

import de.sofd.viskit.ui.imagelist.ImageListView;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller that maintains a list of references to {@link ImageListView} objects
 * and a boolean "enabled" flag. If the flag is set to true, the scale modes of
 * all the lists are kept synchronized.
 *
 * @author Sofd GmbH
 */
public class ImageListViewScaleModeSynchronizationController {

    private final List<ImageListView> lists = new ArrayList<ImageListView>();
    private boolean enabled;
    public static final String PROP_ENABLED = "enabled";

    public ImageListViewScaleModeSynchronizationController() {
    }

    public ImageListViewScaleModeSynchronizationController(ImageListView... lists) {
        setLists(lists);
    }

    /**
     * Get the value of lists
     *
     * @return the value of lists
     */
    public ImageListView[] getLists() {
        return (ImageListView[]) lists.toArray(new ImageListView[0]);
    }

    /**
     * Set the value of lists
     *
     * @param lists new value of lists
     */
    public void setLists(ImageListView[] lists) {
        // TODO: deal with selection model changes on the lists?
        for (ImageListView lv : this.lists) {
            lv.removePropertyChangeListener(scaleModeChangeHandler);
        }
        this.lists.clear();
        for (ImageListView lv : lists) {
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
    public ImageListView getLists(int index) {
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
            if (! ImageListView.PROP_SCALEMODE.equals(evt.getPropertyName())) {
                return;
            }
            if (inProgrammedChange) {
                return;
            }
            ImageListView source = (ImageListView) evt.getSource();
            for (ImageListView l : getLists()) {
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
