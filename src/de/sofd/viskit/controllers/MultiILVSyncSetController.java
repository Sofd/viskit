package de.sofd.viskit.controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonModel;
import javax.swing.JToggleButton;

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
 * If a sync set is modified (i.e., if {@link JImageListView}s are
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

    public static interface SyncControllerFactory {
        MultiImageListViewController createController();
    }
    
    protected final Map<Object, SyncControllerFactory> syncControllerFactoriesByKey = new HashMap<Object, SyncControllerFactory>();
    
    protected final Map<Object, SyncSetImpl> syncSetsByKey = new IdentityHashMap<Object, SyncSetImpl>();

    /**
     * Register a new {@link MultiImageListViewController} subclass. The
     * implementation will ensure that an instance of this class is associated
     * with each sync set. The class must have a public no-args constructor.
     * <p>
     * You can only have one controller per class when using this method. If you
     * want to have multiple controllers of the same class, use the more general
     * method {@link #addSyncControllerType(Object, SyncControllerFactory)}.
     * <p>
     * Internally, this method will delegate to
     * {@link #addSyncControllerType(Object, SyncControllerFactory)} with the
     * class parameter as the key and an internal factory that just invokes the
     * no-args constructor.
     * 
     * @param <C>
     * @param clazz
     */
    public <C extends MultiImageListViewController> void addSyncControllerType(final Class<C> clazz) {
        addSyncControllerType(clazz, new SyncControllerFactory() {
            @Override
            public C createController() {
                try {
                    return clazz.newInstance();
                } catch (InstantiationException e) {
                    throw new IllegalArgumentException(e);
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
    }

    /**
     * More generalized version of {@link #addSyncControllerType(Class)}:
     * Instead of passing a class, you pass a special
     * {@link SyncControllerFactory} that will be called when instances of the
     * controller need to be created.
     * 
     * @param <C>
     * @param key
     * @param fac
     */
    public <C extends MultiImageListViewController> void addSyncControllerType(Object key, SyncControllerFactory fac) {
        if (syncControllerFactoriesByKey.containsKey(key)) {
            throw new IllegalArgumentException("duplicate sync controller factory key: " + fac);
        }
        syncControllerFactoriesByKey.put(key, fac);
        for (SyncSetImpl ss : syncSetsByKey.values()) {
            ss.addSyncController(key);
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
         *            the class of the controller (must be one of the classes
         *            registered via
         *            {@link MultiILVSyncSetController#addSyncControllerType(Class)}
         *            )
         * @return
         */
        <C extends MultiImageListViewController> C getSyncController(Class<C> clazz);

        /**
         * More general variant of {@link #getSyncController(Class)}: Takes the
         * factory key as passed to
         * {@link MultiILVSyncSetController#addSyncControllerType(Object, SyncControllerFactory)}
         * rather than the class.
         * 
         * @param factoryKey
         * @return
         */
        MultiImageListViewController getSyncController(Object factoryKey);

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
         * More general variant of {@link #syncController(Class, boolean)}:
         * Takes the factory key as passed to
         * {@link MultiILVSyncSetController#addSyncControllerType(Object, SyncControllerFactory)}
         * rather than the class.
         * 
         * @param factoryKey
         * @param synced
         */
        void syncController(Object factoryKey, boolean synced);

        /**
         * Getter operation for syncController(). Tells whether a given
         * controller in this sync set (specified by its class or factory key)
         * currently has its list of {@link JImageListView}s synced to this sync
         * set.
         * 
         * @param <C>
         * @param factoryKey
         *            factory key or class
         * @return
         */
        boolean isControllerSynced(Object factoryKey);

        /**
         * UI convenience method: ButtonModel wrapper for
         * syncController/isControllerSynced. Returns a Swing
         * {@link ButtonModel} (more specifically, a toggle button model
         * suitable for use with check boxes in Swing) whose
         * {@link ButtonModel#isSelected() selected} flag tracks the "synced"
         * flag for a controller in this sync set (specified by its class or
         * factory key). The tracking is fully bidirectional, i.e. setting the
         * flag by calling a syncController() method changes the "selected" flag
         * in the model, and actively changing the "selected" flag in the model
         * (by calling {@link ButtonModel#setSelected(boolean) setSelected}, or
         * by clicking a check box connected to the model) changes the "synced"
         * flag for the controller accordingly.
         * 
         * @param factoryKey
         *            factory key or class
         * @return
         */
        public ButtonModel getIsControllerSyncedModel(Object factoryKey);
        
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
        for (Object factoryKey : syncControllerFactoriesByKey.keySet()) {
            syncSet.addSyncController(factoryKey);
        }
        syncSetsByKey.put(key, syncSet);
        return syncSet;
    }
    
    public SyncSet getSyncSet(Object key) {
        return syncSetsByKey.get(key);
    }
    
    public Collection<SyncSet> getAllSyncSets() {
        return new ArrayList<SyncSet>(syncSetsByKey.values());
    }

    protected class SyncSetImpl implements SyncSet {

        protected final Object key;
        protected final Set<JImageListView> lists = new IdentityHashSet<JImageListView>();
        protected final Map<Object, MultiImageListViewController> syncControllersByFactoryKey
            = new HashMap<Object, MultiImageListViewController>();
        protected final Map<Object, ButtonModel> isSyncedModelByFactoryKey = new HashMap<Object, ButtonModel>();
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

        protected ActionListener controllerUpdater = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSyncControllers();
            }
        };

        public <C extends MultiImageListViewController> void addSyncController(Object factoryKey) {
            if (null != syncControllersByFactoryKey.get(factoryKey)) {
                throw new IllegalStateException("trying to add more than one sync controller " + factoryKey +
                                                " to the same MultiILVSyncSetController");
            }
            SyncControllerFactory fac = syncControllerFactoriesByKey.get(factoryKey);
            MultiImageListViewController controller = fac.createController();
            syncControllersByFactoryKey.put(factoryKey, controller);
            ButtonModel model = new JToggleButton.ToggleButtonModel();
            model.addActionListener(controllerUpdater);
            isSyncedModelByFactoryKey.put(factoryKey, model);
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <C extends MultiImageListViewController> C getSyncController(Class<C> clazz) {
            return (C) syncControllersByFactoryKey.get(clazz);
        }

        @Override
        public MultiImageListViewController getSyncController(Object factoryKey) {
            return syncControllersByFactoryKey.get(factoryKey);
        }

        @Override
        public <C extends MultiImageListViewController> void syncController(Class<C> clazz, boolean synced) {
            syncController(clazz, synced);
        }
        
        @Override
        public void syncController(Object factoryKey, boolean synced) {
            if (!syncControllersByFactoryKey.containsKey(factoryKey)) {
                throw new IllegalArgumentException("unregistered MultiImageListViewController factory: " + factoryKey);
            }
            isSyncedModelByFactoryKey.get(factoryKey).setSelected(synced);
        }
        
        @Override
        public boolean isControllerSynced(Object factoryKey) {
            ButtonModel result = isSyncedModelByFactoryKey.get(factoryKey);
            if (null == result) {
                throw new IllegalArgumentException("unregistered MultiImageListViewController factory: " + factoryKey);
            }
            return result.isSelected();
        }

        @Override
        public ButtonModel getIsControllerSyncedModel(Object factoryKey) {
            return isSyncedModelByFactoryKey.get(factoryKey);
        }
        
        protected void disassociateControllers() {
            for (MultiImageListViewController c : syncControllersByFactoryKey.values()) {
                c.setLists(new JGLImageListView[0]);
            }
        }
    }
    
    protected void updateSyncControllers() {
        for (SyncSetImpl ss : syncSetsByKey.values()) {
            for (Object factoryKey : syncControllerFactoriesByKey.keySet()) {
                if (ss.isControllerSynced(factoryKey)) {
                    ss.getSyncController(factoryKey).setLists(ss.lists);
                } else {
                    ss.getSyncController(factoryKey).setLists(new JImageListView[0]);
                }
            }
        }
    }
    

}

// TODO: is the idea of having this class a case of over-engineering?
