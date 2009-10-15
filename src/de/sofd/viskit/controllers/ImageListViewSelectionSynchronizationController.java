package de.sofd.viskit.controllers;

import de.sofd.viskit.ui.imagelist.JImageListView;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Controller that maintains a list of references to {@link JImageListView} objects
 * and a boolean "enabled" flag. If the flag is set to true, the selections of
 * all the lists are kept synchronized.
 *
 * @author Sofd GmbH
 */
public class ImageListViewSelectionSynchronizationController {

    private final List<JImageListView> lists = new ArrayList<JImageListView>();
    private boolean enabled;
    public static final String PROP_ENABLED = "enabled";

    public ImageListViewSelectionSynchronizationController() {
    }

    public ImageListViewSelectionSynchronizationController(JImageListView... lists) {
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
            lv.removeListSelectionListener(selectionHandler);
        }
        this.lists.clear();
        for (JImageListView lv : lists) {
            this.lists.add(lv);
            lv.addListSelectionListener(selectionHandler);
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


    private ListSelectionListener selectionHandler = new ListSelectionListener() {
        private boolean inProgrammedSelectionChange = false;
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!isEnabled()) {
                return;
            }
            if (inProgrammedSelectionChange) {
                return;
            }
            JImageListView source = (JImageListView) e.getSource();
            int selIndex = source.getSelectedIndex();
            if (selIndex != -1) {
                for (JImageListView l : getLists()) {
                    if (l != source) {
                        inProgrammedSelectionChange = true;
                        try {
                            l.getSelectionModel().setSelectionInterval(selIndex, selIndex);
                        } finally {
                            inProgrammedSelectionChange = false;
                        }
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
