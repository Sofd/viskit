package de.sofd.viskit.controllers;

import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import de.sofd.viskit.ui.imagelist.JImageListView;

/**
 * Controller that holds multiple, mutable sets of {@link JImageListView}s
 * called "sync sets" and, for each sync set, a number of
 * {@link MultiImageListViewController}s. Each such MultiImageListViewController
 * can have its list of {@link JImageListView}s be kept in sync with the sync
 * set, or emptied, dynamically at any time.
 * 
 * If the sync set is modified (i.e., if {@link JImageListView}s are
 * added/removed), which may be done at any time dynamically, all
 * MultiImageListViewControllers of the sync set that currently have their set
 * of JImageListViews kept in sync with the sync set, will have that set updated
 * accordingly.
 * 
 * The sync sets don't have to be disjoint (i.e. they may overlap).
 * 
 * @author olaf
 */
public class MultiILVSyncSetController {

    protected final Set<Class<? extends MultiImageListViewController>> syncControllerClasses =
        new HashSet<Class<? extends MultiImageListViewController>>();
    
    protected final Map<Object, SyncSet> syncSetsByKey = new IdentityHashMap<Object, SyncSet>();

    /**
     * Register a new MultiImageListViewController subclass. The implementation will ensure that
     * an instance of this class is associated with each sync set.
     * 
     * @param <C>
     * @param clazz
     */
    public <C extends MultiImageListViewController> void addSyncControllerType(Class<C> clazz) {
    }

    /**
     * A single sync set.
     * 
     * @author olaf
     */
    public static interface SyncSet {
        Object getKey();

        /**
         * set/add/removeList -- dynamically mutate the list of JImageListViews
         * in the sync set.
         * 
         * @param lists
         */
        void setLists(Collection<JImageListView> lists);
        
        void setLists(JImageListView[] lists);

        void addList(JImageListView list);
        
        void removeList(JImageListView list);

        /**
         * Retrieve one of the {@link MultiImageListViewController}s associated
         * with this sync set, given the class of the controller
         * 
         * @param <C>
         * @param clazz
         *            the class of the controller (must one of the classes
         *            registered via
         *            {@link MultiILVSyncSetController#addSyncControllerType(Class)}
         *            )
         * @return
         */
        <C extends MultiImageListViewController> C getSyncController(Class<C> clazz);

        /**
         * Arbitrary attributes may be associated with the sync set for
         * application-specific purposes
         * 
         * @param name
         * @param value
         */
        void setAttribute(String name, Object value);
        
        Object getAttribute(String name);

        /**
         * Set the list of JImageListViews of one of this sync set's {@link MultiImageListViewController}s
         * to this sync set, or to an empty list
         * 
         * @param <C>
         * @param clazz
         *            the class of the controller (must be one of the classes
         *            registered via
         *            {@link MultiILVSyncSetController#addSyncControllerType(Class)}
         *            )
         * @param synced flag indicating whether to set the controller's list of JImageListViews to the
         *        sync set (synced=true), or clear it
         */
        <C extends MultiImageListViewController> void syncController(Class<C> clazz, boolean synced);
    }

    /**
     * add a new sync set.
     * 
     * @param key arbitrary key used to retrieve the set later
     * @return the newly created sync set
     */
    public SyncSet addSyncSet(Object key) {
        SyncSetImpl syncSet = new SyncSetImpl();
        
        syncSetsByKey.put(key, syncSet);
        return syncSet;
    }
    
    public SyncSet getSyncSet(Object key) {
        return syncSetsByKey.get(key);
    }

    protected static class SyncSetImpl implements SyncSet {

        @Override
        public Object getKey() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addList(JImageListView list) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void removeList(JImageListView list) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLists(Collection<JImageListView> lists) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLists(JImageListView[] lists) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getAttribute(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <C extends MultiImageListViewController> C getSyncController(Class<C> clazz) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setAttribute(String name, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <C extends MultiImageListViewController> void syncController(
                Class<C> clazz, boolean synced) {
            throw new UnsupportedOperationException();
        }
        
    }
    
}
