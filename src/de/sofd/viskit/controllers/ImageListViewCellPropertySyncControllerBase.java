package de.sofd.viskit.controllers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Set;

import de.sofd.util.IdentityHashSet;
import de.sofd.viskit.ui.imagelist.JImageListView;

/**
 * Base class for controllers that synchronize a per-cell property (e.g.
 * windowing parameters, zoom/pan setting) between multiple
 * {@link JImageListView}s. Subclasses must implement
 * {@link #onCellPropertyChange(PropertyChangeEvent)}.
 * 
 * @author Olaf Klischat
 */
public abstract class ImageListViewCellPropertySyncControllerBase {

    private final Set<JImageListView> lists = new IdentityHashSet<JImageListView>();
    private boolean enabled;
    public static final String PROP_ENABLED = "enabled";

    public ImageListViewCellPropertySyncControllerBase() {
    }

    public ImageListViewCellPropertySyncControllerBase(JImageListView... lists) {
        setLists(lists);
    }

    /**
     * The set of {@link JImageListView}s that this controller currently
     * synchronizes.
     * 
     * @return the value of lists
     */
    public JImageListView[] getLists() {
        return (JImageListView[]) lists.toArray(new JImageListView[0]);
    }
    
    public void addList(JImageListView l) {
        if (! this.lists.contains(l)) {
            this.lists.add(l);
            l.addCellPropertyChangeListener(listsCellPropertyChangeListener);
        }
    }

    public void removeList(JImageListView l) {
        if (this.lists.contains(l)) {
            this.lists.remove(l);
            l.removeCellPropertyChangeListener(listsCellPropertyChangeListener);
        }
    }

    /**
     * Set set of {@link JImageListView}s that this controller currently
     * synchronizes.
     * 
     * @param lists
     */
    public void setLists(JImageListView[] lists) {
        for (JImageListView l : this.lists.toArray(new JImageListView[0])) {
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
        for (JImageListView l : this.lists.toArray(new JImageListView[0])) {
            removeList(l);
        }
        for (JImageListView l : lists) {
            addList(l);
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
        propertyChangeSupport.firePropertyChange(PROP_ENABLED, oldEnabled, enabled);
    }


    private PropertyChangeListener listsCellPropertyChangeListener = new PropertyChangeListener() {

        private boolean inChange = false;
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (! isEnabled()) {
                return;
            }
            if (inChange) {
                return;
            }
            inChange = true;
            try {
                onCellPropertyChange(evt);
            } finally {
                inChange = false;
            }
        }
    };

    /**
     * A property of a cell of any of the JImageListViews {@link #getLists()}
     * has changed. Subclasses must decide here what to do with this.
     * 
     * @param e
     */
    protected abstract void onCellPropertyChange(PropertyChangeEvent e);
    
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
