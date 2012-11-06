package de.sofd.viskit.controllers;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Set;

import de.sofd.util.IdentityHashSet;
import de.sofd.viskit.ui.imagelist.ImageListView;

/**
 * Simple implementation of {@link MultiImageListViewController} that keeps all
 * the lists in an internal set. Also provides an enabled flag and change event
 * support.
 * <p>
 * This class may be subclassed by actual MultiImageListViewControllers to
 * inherit that functionality. Alternatively, controllers may aggregate an
 * instance of this class (and possibly implement
 * {@link MultiImageListViewController} and delegate all its methods on to the
 * aggregated controller).
 * 
 * @author olaf
 */
public class DefaultMultiImageListViewController implements MultiImageListViewController {

    private final Set<ImageListView> lists = new IdentityHashSet<ImageListView>();
    private boolean enabled;
    public static final String PROP_ENABLED = "enabled";

    public DefaultMultiImageListViewController() {
    }

    public DefaultMultiImageListViewController(ImageListView... lists) {
        setLists(lists);
    }

    @Override
    public ImageListView[] getLists() {
        return (ImageListView[]) lists.toArray(new ImageListView[0]);
    }

    @Override
    public boolean addList(ImageListView l) {
        if (! this.lists.contains(l)) {
            this.lists.add(l);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean removeList(ImageListView l) {
        if (this.lists.contains(l)) {
            this.lists.remove(l);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean containsList(ImageListView l) {
        return this.lists.contains(l);
    }

    /**
     * Set the set of lists directly.
     */
    @Override
    public void setLists(ImageListView[] lists) {
        for (ImageListView l : this.lists.toArray(new ImageListView[0])) {
            removeList(l);
        }
        for (ImageListView l : lists) {
            addList(l);
        }
    }

    @Override
    public void setLists(Collection<ImageListView> lists) {
        for (ImageListView l : this.lists.toArray(new ImageListView[0])) {
            removeList(l);
        }
        for (ImageListView l : lists) {
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
    
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

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

    /**
     * Return the {@link PropertyChangeSupport} that may be used for firing
     * property change events to this list's property change listeners.
     * 
     * @return
     */
    protected PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }
    
}
