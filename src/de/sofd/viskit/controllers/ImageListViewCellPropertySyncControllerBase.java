package de.sofd.viskit.controllers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.sofd.viskit.ui.imagelist.JImageListView;

/**
 * Base class for controllers that synchronize a per-cell property (e.g.
 * windowing parameters, zoom/pan setting) between multiple
 * {@link JImageListView}s. Subclasses must implement
 * {@link #onCellPropertyChange(PropertyChangeEvent)}.
 * 
 * @author Olaf Klischat
 */
public abstract class ImageListViewCellPropertySyncControllerBase extends DefaultMultiImageListViewController {

    public ImageListViewCellPropertySyncControllerBase() {
    }

    public ImageListViewCellPropertySyncControllerBase(JImageListView... lists) {
        setLists(lists);
    }

    @Override
    public boolean addList(JImageListView l) {
        boolean retval = super.addList(l);
        if (retval) {
            l.addCellPropertyChangeListener(listsCellPropertyChangeListener);
        }
        return retval;
    }

    @Override
    public boolean removeList(JImageListView l) {
        boolean retval = super.removeList(l);
        if (retval) {
            l.removeCellPropertyChangeListener(listsCellPropertyChangeListener);
        }
        return retval;
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

}
