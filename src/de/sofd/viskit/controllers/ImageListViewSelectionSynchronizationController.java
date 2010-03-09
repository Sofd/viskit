package de.sofd.viskit.controllers;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.sofd.viskit.ui.imagelist.JImageListView;

/**
 * Controller that maintains a list of references to {@link JImageListView} objects
 * and a boolean "enabled" flag. If the flag is set to true, the selections of
 * all the lists are kept synchronized.
 *
 * @author Sofd GmbH
 */
public class ImageListViewSelectionSynchronizationController {

    private boolean enabled;
    public static final String PROP_ENABLED = "enabled";
    private boolean keepRelativeSelectionIndices;
    public static final String PROP_KEEPRELATIVESELECTIONINDICES = "keepRelativeSelectionIndices";
    private Map<JImageListView, Integer> listsAndSelectionIndices = new IdentityHashMap<JImageListView, Integer>();

    public ImageListViewSelectionSynchronizationController() {
    }

    public ImageListViewSelectionSynchronizationController(JImageListView... lists) {
        setLists(lists);
    }

    /**
     * The set of {@link JImageListView}s that this controller currently
     * synchronizes.
     * 
     * @return the value of lists
     */
    public JImageListView[] getLists() {
        return (JImageListView[]) listsAndSelectionIndices.keySet().toArray(new JImageListView[0]);
    }
    
    public boolean containsList(JImageListView l) {
        return listsAndSelectionIndices.containsKey(l);
    }

    public void addList(JImageListView l) {
        if (! listsAndSelectionIndices.containsKey(l)) {
            putSelectionIndex(l, l.getLeadSelectionIndex());
            l.addListSelectionListener(selectionHandler);
        }
    }

    public void removeList(JImageListView l) {
        if (null != listsAndSelectionIndices.remove(l)) {
            l.removeListSelectionListener(selectionHandler);
        }
    }

    private void putSelectionIndex(JImageListView l, int idx) {
        if (idx == -1) {
            listsAndSelectionIndices.put(l, null);
        } else {
            listsAndSelectionIndices.put(l, idx);
        }
    }
    
    /**
     * Set set of {@link JImageListView}s that this controller currently
     * synchronizes.
     * 
     * @param lists
     */
    public void setLists(JImageListView[] lists) {
        for (JImageListView l : listsAndSelectionIndices.keySet()) {
            removeList(l);
        }
        for (JImageListView l : lists) {
            addList(l);
        }
    }

    /**
     * Set set of {@link JImageListView}s that this controller currently
     * synchronizes.
     * 
     * @param lists
     */
    public void setLists(Collection<JImageListView> lists) {
        for (JImageListView l : listsAndSelectionIndices.keySet()) {
            removeList(l);
        }
        for (JImageListView l : lists) {
            addList(l);
        }
    }
    
    // TODO: deal with selection model changes on the lists?
    
    private void recordSelectionIndices() {
        for (JImageListView l : getLists()) {
            putSelectionIndex(l, l.getLeadSelectionIndex());
        }
    }
    
    private void dumpSelectionIndices() {
        for (JImageListView l : listsAndSelectionIndices.keySet()) {
            System.out.print("" + listsAndSelectionIndices.get(l) + " ");
        }
        System.out.println();
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
        recordSelectionIndices();
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
        recordSelectionIndices();
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
                JImageListView sourceList = (JImageListView) e.getSource();
                int selIndex = sourceList.getLeadSelectionIndex();
                if (selIndex >= 0) {
                    if (keepRelativeSelectionIndices) {
                        Integer lastSourceSelIdx = listsAndSelectionIndices.get(sourceList);
                        if (lastSourceSelIdx == null) {
                            // nothing was selected in sourceList previously. Just remember the newly selected index
                            putSelectionIndex(sourceList, selIndex);
                        } else {
                            putSelectionIndex(sourceList, selIndex);
                            int change = selIndex - lastSourceSelIdx;
                            for (JImageListView l : getLists()) {
                                if (l != sourceList) {
                                    Integer si = listsAndSelectionIndices.get(l);
                                    if (si != null) {
                                        int newsi = si + change;
                                        listsAndSelectionIndices.put(l, newsi);  // newsi==-1 is a valid value here, so DON'T call putSelectionIndex
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
                        putSelectionIndex(sourceList, selIndex);
                        for (JImageListView l : getLists()) {
                            if (l != sourceList) {
                                l.getSelectionModel().setSelectionInterval(selIndex, selIndex);
                                putSelectionIndex(l, selIndex);
                            }
                        }
                    }
                } else {
                    putSelectionIndex(sourceList, -1);
                }
            } finally {
                inProgrammedSelectionChange = false;
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
