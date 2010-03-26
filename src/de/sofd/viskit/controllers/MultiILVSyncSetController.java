package de.sofd.viskit.controllers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import de.sofd.util.IdentityHashSet;
import de.sofd.viskit.ui.imagelist.JImageListView;
import de.sofd.viskit.ui.imagelist.glimpl.JGLImageListView;

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
    
    protected final Map<Object, SyncSetImpl> syncSetsByKey = new IdentityHashMap<Object, SyncSetImpl>();

    /**
     * Register a new MultiImageListViewController subclass. The implementation
     * will ensure that an instance of this class is associated with each sync
     * set. The class must have a public no-args constructor.
     * 
     * @param <C>
     * @param clazz
     */
    public <C extends MultiImageListViewController> void addSyncControllerType(Class<C> clazz) {
        for (SyncSetImpl ss : syncSetsByKey.values()) {
            ss.addSyncController(clazz);
        }
        updateSyncControllers();
    }

    /**
     * A single sync set.
     * 
     * @author olaf
     */
    public static interface SyncSet {
        /**
         * set/add/removeList -- dynamically modify the list of JImageListViews
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
         * Keep the list of JImageListViews of one of this sync set's
         * {@link MultiImageListViewController}s in sync with this sync set, or
         * empty it.
         * 
         * @param <C>
         * @param clazz
         *            the class of the controller (must be one of the classes
         *            registered via
         *            {@link MultiILVSyncSetController#addSyncControllerType(Class)}
         *            )
         * @param synced
         *            flag indicating whether to keep the controller's list of
         *            JImageListViews in sync with the sync set (synced=true),
         *            or clear it
         */
        <C extends MultiImageListViewController> void syncController(Class<C> clazz, boolean synced);

        /**
         * 
         * @return the key that was used when adding this sync set to this
         *         controller (see
         *         {@link MultiILVSyncSetController#addSyncSet(Object)}).
         */
        Object getKey();
    }

    /**
     * Add a new sync set. The sync set is created internally and returned.
     * 
     * @param key arbitrary key used to retrieve the set later
     * @return the newly created sync set
     */
    public SyncSet addSyncSet(Object key) {
        SyncSetImpl syncSet = new SyncSetImpl(key);
        for (Class<? extends MultiImageListViewController> syncControllerClass : syncControllerClasses) {
            syncSet.addSyncController(syncControllerClass);
        }
        syncSetsByKey.put(key, syncSet);
        return syncSet;
    }
    
    public SyncSet getSyncSet(Object key) {
        return syncSetsByKey.get(key);
    }

    protected class SyncSetImpl implements SyncSet {

        protected final Object key;
        protected final Set<JImageListView> lists = new IdentityHashSet<JImageListView>();
        protected final Map<Class<? extends MultiImageListViewController>, MultiImageListViewController> syncControllersByClass
            = new HashMap<Class<? extends MultiImageListViewController>, MultiImageListViewController>();
        protected final Map<MultiImageListViewController, Boolean> isSyncedFlagByController = new HashMap<MultiImageListViewController, Boolean>();
        protected final Map<String, Object> attrs = new HashMap<String, Object>();
        
        public SyncSetImpl(Object key) {
            this.key = key;
        }

        @Override
        public Object getKey() {
            return key;
        }

        @Override
        public void addList(JImageListView list) {
            lists.add(list);
            updateSyncControllers();
        }
        
        @Override
        public void removeList(JImageListView list) {
            disassociateControllers();
            lists.remove(list);
            updateSyncControllers();
        }

        @Override
        public void setLists(Collection<JImageListView> lists) {
            disassociateControllers();
            this.lists.clear();
            this.lists.addAll(lists);
            updateSyncControllers();
        }

        @Override
        public void setLists(JImageListView[] lists) {
            disassociateControllers();
            this.lists.clear();
            for (JImageListView l : lists) {
                this.lists.add(l);
            }
            updateSyncControllers();
        }

        @Override
        public Object getAttribute(String name) {
            return attrs.get(name);
        }

        @Override
        public void setAttribute(String name, Object value) {
            attrs.put(name, value);
        }

        public <C extends MultiImageListViewController> void addSyncController(Class<C> clazz) {
            try {
                if (null != syncControllersByClass.get(clazz)) {
                    throw new IllegalStateException("trying to add more than one " + clazz + " to " + MultiILVSyncSetController.class);
                }
                C controller = clazz.newInstance();
                syncControllersByClass.put(clazz, controller);
                isSyncedFlagByController.put(controller, false);
            } catch (InstantiationException e) {
                throw new IllegalArgumentException(e);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e);
            }
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <C extends MultiImageListViewController> C getSyncController(Class<C> clazz) {
            return (C) syncControllersByClass.get(clazz);
        }

        @Override
        public <C extends MultiImageListViewController> void syncController(Class<C> clazz, boolean synced) {
            C controller = getSyncController(clazz);
            if (null == controller) {
                throw new IllegalArgumentException("unregistered MultiImageListViewController class: " + clazz);
            }
            isSyncedFlagByController.put(controller, synced);
            updateSyncControllers();
        }
        
        public <C extends MultiImageListViewController> boolean isControllerSynced(Class<C> clazz) {
            C controller = getSyncController(clazz);
            if (null == controller) {
                throw new IllegalArgumentException("unregistered MultiImageListViewController class: " + clazz);
            }
            return isSyncedFlagByController.get(controller);
        }
        
        protected void disassociateControllers() {
            for (MultiImageListViewController c : syncControllersByClass.values()) {
                c.setLists(new JGLImageListView[0]);
            }
        }
    }
    
    protected void updateSyncControllers() {
        for (SyncSetImpl ss : syncSetsByKey.values()) {
            for (Class<? extends MultiImageListViewController> scc : syncControllerClasses) {
                if (ss.isControllerSynced(scc)) {
                    ss.getSyncController(scc).setLists(ss.lists);
                } else {
                    ss.getSyncController(scc).setLists(new JImageListView[0]);
                }
            }
        }
    }
    

}

// TODO: is the idea of having this class a case of over-engineering?
