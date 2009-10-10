package de.sofd.viskit.ui.imagelist;

import de.sofd.util.BiIdentityHashMap;
import de.sofd.util.BiMap;
import de.sofd.util.IdentityHashSet;
import de.sofd.util.Misc;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import javax.swing.AbstractListModel;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author olaf
 */
public abstract class JImageListView extends JPanel {

    private ListModel model;
    public static final String PROP_MODEL = "model";
    private ListSelectionModel selectionModel;
    public static final String PROP_SELECTIONMODEL = "selectionModel";
    private String displayName = "";

    // TODO: it's probably better to use a map that uses normal (equals()/hashCode()-based)
    //       mapping for the modelElement => cell direction
    // TODO: maybe this should really be list of cell objects that tracks the model
    //       (a ListModel may contain more than one identical or equal elements, in which case the
    //       cell => element mapping would fail). Or, explicitly forbid having the same element
    //       in the model more than once (we kind of do that for now; see the modelChangeListener below)
    private BiMap<ImageListViewModelElement, ImageListViewCell> cellsByElementMap
            = new BiIdentityHashMap<ImageListViewModelElement, ImageListViewCell>();

    /**
     * Get the value of model
     *
     * @return the value of model
     */
    public ListModel getModel() {
        return model;
    }

    /**
     * Set the value of model
     *
     * @param model new value of model
     */
    public void setModel(ListModel model) {
        ListModel oldModel = this.model;
        if (oldModel != null) {
            oldModel.removeListDataListener(modelChangeListener);
            clearCellsByElementMap();
        }
        this.model = model;
        if (this.model != null) {
            fillCellsByElementMap();
            this.model.addListDataListener(modelChangeListener);
        }
        propertyChangeSupport.firePropertyChange(PROP_MODEL, oldModel, model);
    }

    private ListDataListener modelChangeListener = new ListDataListener() {
        // we only keep the cellsByElementMap in sync with the model here;
        // we don't initiate any cell refreshs b/c we assume the subclass
        // will do that (probably automatically by passing the model through
        // to an internal, wrapped list component)
        @Override
        public void contentsChanged(ListDataEvent e) {
            boolean newElements = false;
            for (int i = e.getIndex0(); i <= e.getIndex1(); i++) {
                ImageListViewModelElement elt = (ImageListViewModelElement) getModel().getElementAt(i);
                if (! cellsByElementMap.containsKey(elt)) {
                    ImageListViewCell cell = createCell(elt);
                    cellsByElementMap.put(elt, cell);
                    newElements = true;
                }
            }
            if (newElements) {
                garbageCollectStaleCells();
            }
            JImageListView.this.modelContentsChanged(e);
        }
        @Override
        public void intervalAdded(ListDataEvent e) {
            for (int i = e.getIndex0(); i <= e.getIndex1(); i++) {
                ImageListViewModelElement elt = (ImageListViewModelElement) getModel().getElementAt(i);
                if (cellsByElementMap.containsKey(elt)) {
                    throw new IllegalStateException("JImageListView doesn't support adding the same model element (" + elt + ") more than once");
                }
                ImageListViewCell cell = createCell(elt);
                cellsByElementMap.put(elt, cell);
            }
            JImageListView.this.modelIntervalAdded(e);
        }
        @Override
        public void intervalRemoved(ListDataEvent e) {
            // TODO: remove corresponding entries from cellsByElementMap...how?
            //  (e.index[01] no longer valid here)
            JImageListView.this.modelIntervalRemoved(e);
        }
    };

    public ImageListViewCell getCellForElement(ImageListViewModelElement elt) {
        return cellsByElementMap.get(elt);
    }

    public ImageListViewModelElement getElementForCell(ImageListViewCell cell) {
        return cellsByElementMap.reverseGet(cell);
    }

    public ImageListViewCell getCell(int index) {
        if (null != getModel()) {
            return cellsByElementMap.get((ImageListViewModelElement) (getModel().getElementAt(index)));
        } else {
            return null;
        }
    }

    /**
     * Remove cells that are no longer in use (i.e. no longer correspond to an existing model element)
     */
    protected void garbageCollectStaleCells() {
        Collection<ImageListViewModelElement> staleElts = new IdentityHashSet<ImageListViewModelElement>(cellsByElementMap.keySet());
        int size = getModel().getSize();
        for (int i = 0; i < size; i++) {
            ImageListViewModelElement elt = (ImageListViewModelElement) getModel().getElementAt(i);
            staleElts.remove(elt);
        }
        for (ImageListViewModelElement elt : staleElts) {
            ImageListViewCell cell = cellsByElementMap.get(elt);
            beforeCellRemoval(cell, elt);
            cellsByElementMap.remove(elt);
        }
    }

    /**
     * Called if the contents of the {@link #getModel() } have changed in some way.
     * Default impl. does nothing, subclasses may override
     * @param e
     */
    protected void modelContentsChanged(ListDataEvent e) {
    }

    /**
     * Called if an interval was added to the {@link #getModel() }.
     * Default impl. does nothing, subclasses may override
     * @param e
     */
    protected void modelIntervalAdded(ListDataEvent e) {
    }

    /**
     * Called if an interval was removed from the {@link #getModel() }.
     * Default impl. does nothing, subclasses may override
     * @param e
     */
    protected void modelIntervalRemoved(ListDataEvent e) {
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        if (null == displayName) {
            this.displayName = "";
        } else {
            this.displayName = displayName.trim();
        }
    }

    /**
     * Factory method for creating the {@link ImageListViewCell} instances
     * to associate with model elements. Default implementation instantiates
     * a {@link DefaultImageListViewCell}. Subclasses may override.
     *
     * @param modelElement
     * @return
     */
    protected ImageListViewCell createCell(ImageListViewModelElement modelElement) {
        return new DefaultImageListViewCell(this, modelElement);
    }

    protected void beforeCellRemoval(ImageListViewCell cell, ImageListViewModelElement modelElement) {
        // 
    }

    public ImageListViewCell getCellFor(ImageListViewModelElement modelElement) {
        return cellsByElementMap.get(modelElement);
    }

    private void clearCellsByElementMap() {
        for (ImageListViewModelElement elt: cellsByElementMap.keySet()) {
            ImageListViewCell cell = cellsByElementMap.get(elt);
            beforeCellRemoval(cell, elt);
        }
        cellsByElementMap.clear();
    }

    private void fillCellsByElementMap() {
        for (int i = 0; i < getModel().getSize(); i++) {
            ImageListViewModelElement elt = (ImageListViewModelElement) getModel().getElementAt(i);
            ImageListViewCell cell = createCell(elt);
            cellsByElementMap.put(elt, cell);
        }
    }

    /**
     * Get the value of selectionModel
     *
     * @return the value of selectionModel
     */
    public ListSelectionModel getSelectionModel() {
        return selectionModel;
    }

    /**
     * Set the value of selectionModel
     *
     * @param selectionModel new value of selectionModel
     */
    public void setSelectionModel(ListSelectionModel selectionModel) {
        ListSelectionModel oldSelectionModel = this.selectionModel;
        if (oldSelectionModel != null) {
            oldSelectionModel.removeListSelectionListener(listSelectionListener);
        }
        this.selectionModel = selectionModel;
        if (this.selectionModel != null) {
            this.selectionModel.addListSelectionListener(listSelectionListener);
        }
        propertyChangeSupport.firePropertyChange(PROP_SELECTIONMODEL, oldSelectionModel, selectionModel);
    }

    public int getSelectedIndex() {
        return getMinSelectionIndex();
    }

    public ImageListViewModelElement getSelectedValue() {
        int i = getMinSelectionIndex();
        return (i == -1) ? null : (ImageListViewModelElement) getModel().getElementAt(i);
    }

    public int getMinSelectionIndex() {
        return getSelectionModel().getMinSelectionIndex();
    }

    public int getMaxSelectionIndex() {
        return getSelectionModel().getMaxSelectionIndex();
    }

    public int getLeadSelectionIndex() {
        return getSelectionModel().getLeadSelectionIndex();
    }

    private ListSelectionListener listSelectionListener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            JImageListView.this.selectionChanged(e);
        }
    };

    /**
     * Called if the selection of this list has changed.
     * Default implementation does nothing, subclasses may override.
     * @param e
     */
    protected void selectionChanged(ListSelectionEvent e) {
    }

    /**
     * Called if a the cells of this viewer need to be refreshed. Normally
     * called internally by subclasses if they determine the need to refresh the cells,
     * but may also be called explicitly from the outside.
     * <p>
     * Uses the same special casing hack as {@link #refreshCellForIndex(int) }.
     */
    public void refreshCells() {
        if (getModel() instanceof AbstractListModel) {
            AbstractListModel alm = (AbstractListModel) getModel();
            Misc.callMethod(alm, "fireContentsChanged", alm, 0, alm.getSize() - 1);
        } else {
            System.err.println("JImageListView#refreshCells(): don't know how to refresh. Override me!");
        }
    }

    /**
     * Called if a specific cell of this viewer needs to be refreshed. Normally
     * called internally by subclasses if they determine the need to refresh a cell,
     * but may also be called explicitly from the outside.
     * <p>
     * Default implementation implements a special casing hack if the #getModel()
     * is an instance of {@link AbstractListModel}: In that case, it fires a
     * corresponding contentsChanged event through the model, which should cause
     * compliant viewers to refresh themselves accordingly. This might be terribly
     * inefficient (all viewers of the model will refresh even if only cell data
     * in this viewer has changed, and there may be more efficient ways to refresh
     * the cell). Subclasses must override (not calling the super implementation)
     * if they need/want a better strategy.
     *
     * @param idx index of the cell (corresponds to index in the getModel())
     */
    public void refreshCellForIndex(int idx) {
        if (getModel() instanceof AbstractListModel) {
            AbstractListModel alm = (AbstractListModel) getModel();
            Misc.callMethod(alm, "fireContentsChanged", alm, idx, idx);
        } else {
            System.err.println("JImageListView#refreshCellForIndex(): don't know how to refresh. Override me!");
        }
    }

    /**
     * Called if a specific cell of this viewer needs to be refreshed. Normally
     * called internally by subclasses if they determine the need to refresh a cell,
     * but may also be called explicitly from the outside.
     * <p>
     * Uses the same special casing hack as {@link #refreshCellForIndex(int) }.
     *
     * @param elt model element corresponding to the cell
     */
    public void refreshCellForElement(ImageListViewModelElement elt) {
        if (null == getModel()) { return; }
        int eltCount = getModel().getSize();
        for (int i = 0; i < eltCount; i++) {
            if (Misc.equal(elt, getModel().getElementAt(i))) {
                refreshCellForIndex(i);
                return;
            }
        }
    }


    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

}
