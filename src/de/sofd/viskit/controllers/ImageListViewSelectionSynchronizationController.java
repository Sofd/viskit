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
    private boolean keepRelativeSelectionIndices;
    public static final String PROP_KEEPRELATIVESELECTIONINDICES = "keepRelativeSelectionIndices";
    private int[] lastSelectionIndices;

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
        recordLastSelectionIndices();
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

    private void recordLastSelectionIndices() {
        int size = lists.size();
        lastSelectionIndices = new int[size];
        for (int i = 0; i < size; i++) {
            lastSelectionIndices[i] = lists.get(i).getLeadSelectionIndex();
        }
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
        recordLastSelectionIndices();
        propertyChangeSupport.firePropertyChange(PROP_ENABLED, oldEnabled, enabled);
    }

    /**
     * Get the value of keepRelativeSelectionIndices
     *
     * @return the value of keepRelativeSelectionIndices
     */
    public boolean isKeepRelativeSelectionIndices() {
        return keepRelativeSelectionIndices;
    }

    /**
     * Set the value of keepRelativeSelectionIndices
     *
     * @param keepRelativeSelectionIndices new value of keepRelativeSelectionIndices
     */
    public void setKeepRelativeSelectionIndices(boolean keepRelativeSelectionIndices) {
        boolean oldKeepRelativeSelectionIndices = this.keepRelativeSelectionIndices;
        this.keepRelativeSelectionIndices = keepRelativeSelectionIndices;
        recordLastSelectionIndices();
        propertyChangeSupport.firePropertyChange(PROP_KEEPRELATIVESELECTIONINDICES, oldKeepRelativeSelectionIndices, keepRelativeSelectionIndices);
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
            inProgrammedSelectionChange = true;
            try {
                JImageListView source = (JImageListView) e.getSource();
                int selIndex = source.getSelectedIndex();
                if (selIndex >= 0) {
                    if (keepRelativeSelectionIndices) {
                        int sourceListIdx = lists.indexOf(source);
                        assert(sourceListIdx >= 0);
                        int lastSourceSelIdx = lastSelectionIndices[sourceListIdx];
                        if (lastSourceSelIdx >= 0) {
                            int change = selIndex - lastSourceSelIdx;
                            int nLists = lists.size();
                            for (int i = 0; i < nLists; i++) {
                                if (i != sourceListIdx) {
                                    JImageListView l = lists.get(i);
                                    int si = l.getLeadSelectionIndex();
                                    if (si >= 0) {
                                        int newsi = si + change;
                                        if (newsi >= l.getLength()) {
                                            newsi = l.getLength() - 1;
                                        }
                                        if (newsi < 0) {
                                            newsi = 0;
                                        }
                                        l.getSelectionModel().setSelectionInterval(newsi, newsi);
                                    }
                                }
                            }
                        }
                    } else {
                        for (JImageListView l : getLists()) {
                            if (l != source) {
                                l.getSelectionModel().setSelectionInterval(selIndex, selIndex);
                            }
                        }
                    }
                }
            } finally {
                inProgrammedSelectionChange = false;
                recordLastSelectionIndices();
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
