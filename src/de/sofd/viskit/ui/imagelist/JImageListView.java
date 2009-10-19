package de.sofd.viskit.ui.imagelist;

import de.sofd.util.BiIdentityHashMap;
import de.sofd.util.BiMap;
import de.sofd.util.IdentityHashSet;
import de.sofd.util.Misc;
import de.sofd.viskit.ui.imagelist.event.ImageListViewCellAddEvent;
import de.sofd.viskit.ui.imagelist.event.ImageListViewCellRemoveEvent;
import de.sofd.viskit.ui.imagelist.event.ImageListViewEvent;
import de.sofd.viskit.ui.imagelist.event.ImageListViewListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Base class for GUI components displaying a list of elements, which are objects implementing {@link ImageListViewModelElement}.
 * <p>
 * The elements must be fed to the JImageListView via a {@link ListModel} that contains them.
 * <p>
 * A selection of elements is maintained via a {@link ListSelectionModel}.
 * <p>
 * The list automatically maintains a list of "cell" objects, implementing {@link ImageListViewCell},
 * that will always be associated 1:1 to the current elements (i.e. each element is associated
 * to exactly one cell, and vice versa). A cell is used to hold data that should be associated
 * with a model element in this list, but not elsewhere, i.e. if a model element is displayed
 * simultaneously in more than one JImageListView, each one has separate cell data associated with
 * the element.
 *
 * @author olaf
 */
public abstract class JImageListView extends JPanel {

    private ListModel model;
    public static final String PROP_MODEL = "model";
    private ListSelectionModel selectionModel;
    public static final String PROP_SELECTIONMODEL = "selectionModel";
    private final List<ListSelectionListener> listSelectionListeners = new ArrayList<ListSelectionListener>();
    private String displayName = "";
    public static final String PROP_SCALEMODE = "scaleMode";
    private ScaleMode scaleMode;

    public static interface ScaleMode {
        String getDisplayName();
    }


    // TODO: it's probably better to use a map that uses normal (equals()/hashCode()-based)
    //       mapping for the modelElement => cell direction
    // TODO: maybe this should really be list of cell objects that tracks the model
    //       (a ListModel may contain more than one identical or equal elements, in which case the
    //       cell => element mapping would fail). Or, explicitly forbid having the same element
    //       in the model more than once (we kind of do that for now; see the modelChangeListener below)
    private BiMap<ImageListViewModelElement, ImageListViewCell> cellsByElementMap
            = new BiIdentityHashMap<ImageListViewModelElement, ImageListViewCell>();

    public JImageListView() {
        ensureUiStateIsCopiedForAddedComponents();
    }


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
            JImageListView.this.modelIntervalRemoved(e);
            garbageCollectStaleCells();
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
     * Get the value of scaleMode
     *
     * @return the value of scaleMode
     */
    public ScaleMode getScaleMode() {
        return scaleMode;
    }

    /**
     * Set the scaleMode ({@link #getScaleMode()}) of this viewer to one of the {@link #getSupportedScaleModes() }
     * (calling this with an unsupported scale mode will raise an exception). Sets the bean property,
     * fires the corresponding PropertyChangeEvent, and (before firing, but after setting the property) calls
     * {@link #doSetScaleMode(de.sofd.viskit.ui.imagelist.JImageListView.ScaleMode, de.sofd.viskit.ui.imagelist.JImageListView.ScaleMode) },
     * which subclasses should override normally (rather than overriding this method).
     *
     * @param scaleMode new value of scaleMode
     */
    public void setScaleMode(ScaleMode scaleMode) {
        if (! getSupportedScaleModes().contains(scaleMode)) {
            throw new IllegalArgumentException("Unsupported scale mode: " + scaleMode);
        }
        if (Misc.equal(scaleMode, getScaleMode())) {
            return;
        }
        ScaleMode oldScaleMode = this.scaleMode;
        this.scaleMode = scaleMode;
        doSetScaleMode(oldScaleMode, this.scaleMode);
        propertyChangeSupport.firePropertyChange(PROP_SCALEMODE, oldScaleMode, scaleMode);
    }

    /**
     * Actual setter for the {@link #getScaleMode() }. oldScaleMode is the previous scaleMode,
     * newScale mode is the new one (which will already be in {@link #getScaleMode() } by the time
     * this method is called, so this parameter is just a convenience). Default impl. ist empty,
     * which is pretty useless unless the subclass doesn't support any ScaleModes or doesn't want
     * to do anything when the scaleMode is set (which would be kind of pointless). Thus, subclasses
     * should normally override this method. Alternatively, they could override
     * {@link #setScaleMode(de.sofd.viskit.ui.imagelist.JImageListView.ScaleMode) } and
     * {@link #getScaleMode() } in concert to implement some completely different way of setting the
     * scale mode; however, in that case, the general contract for the bound bean property getter/setter
     * (which is adhered to by the default implementations of those methods) must be re-implemented.
     *
     * @param oldScaleMode oldScaleMode
     * @param newScaleMode newScaleMode
     */
    protected void doSetScaleMode(ScaleMode oldScaleMode, ScaleMode newScaleMode) {
        //
    }

    /**
     *
     * @return list off ScaleModes supported by this JImageListView implementation. Subclasses must implement.
     */
    public abstract Collection<ScaleMode> getSupportedScaleModes();

    /**
     * Creates a new ImageListViewCell (using doCreateCell()), ensures the cell's
     * PropertyChangeEvents are exposed to {@link #addCellPropertyChangeListener(java.beans.PropertyChangeListener) }.
     * Not normally called by subclasses (and never called by users). Never overridden;
     * override {@link #doCreateCell(de.sofd.viskit.ui.imagelist.ImageListViewModelElement) } instead.
     *
     * @param modelElement
     * @return
     */
    protected final ImageListViewCell createCell(ImageListViewModelElement modelElement) {
        ImageListViewCell cell = doCreateCell(modelElement);
        cell.addPropertyChangeListener(cellPropertyChangeEventForwarder);
        fireImageListViewEvent(new ImageListViewCellAddEvent(this, cell));
        return cell;
    }

    /**
     * Factory method for creating the {@link ImageListViewCell} instances
     * to associate with model elements. Called whenever a new cell must
     * be created to be associated with a model element (either during initialization,
     * or whenever a new element is added to this list later).
     * <p>
     * Default implementation instantiates
     * a {@link DefaultImageListViewCell}. Subclasses may override. Don't call
     * this method; call createCell() instead (if you really think you must).
     *
     * @param modelElement the model element to create the cell for
     * @return the newly created cell
     */
    protected ImageListViewCell doCreateCell(ImageListViewModelElement modelElement) {
        return new DefaultImageListViewCell(this, modelElement);
    }

    /**
     * Callback method called immediately before a cell is disassociated from its model element.
     * Override to implement special handling in this situation. Call super() in the overridden
     * version. Never call this method yourself in subclasses.
     *
     * @param cell
     * @param modelElement
     */
    protected void beforeCellRemoval(ImageListViewCell cell, ImageListViewModelElement modelElement) {
        cell.removePropertyChangeListener(cellPropertyChangeEventForwarder);
    }

    private PropertyChangeListener cellPropertyChangeEventForwarder = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            JImageListView.this.fireCellPropertyChangeEvent(evt);
        }
    };

    public ImageListViewCell getCellFor(ImageListViewModelElement modelElement) {
        return cellsByElementMap.get(modelElement);
    }

    private void clearCellsByElementMap() {
        for (ImageListViewModelElement elt: cellsByElementMap.keySet()) {
            ImageListViewCell cell = cellsByElementMap.get(elt);
            fireImageListViewEvent(new ImageListViewCellRemoveEvent(this, cell));
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

    /**
     * Adds a listener that gets notified if this list's selection changes.
     * The source of the selection change events will be this list (rather
     * than the selection model, as would be the case for listeners registered
     * via getSelectionModel().addListSelectionListener())
     * 
     * @param listener
     */
    public void addListSelectionListener(ListSelectionListener listener) {
        listSelectionListeners.add(listener);
    }

    public void removeListSelectionListener(ListSelectionListener listener) {
        listSelectionListeners.remove(listener);
    }

    protected void fireListSelectionEvent(int firstIndex, int lastIndex, boolean isAdjusting) {
        for (ListSelectionListener l : listSelectionListeners) {
            l.valueChanged(new ListSelectionEvent(this, firstIndex, lastIndex, isAdjusting));
        }
    }

    private ListSelectionListener listSelectionListener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            JImageListView.this.selectionChanged(e);
            fireListSelectionEvent(e.getFirstIndex(), e.getLastIndex(), e.getValueIsAdjusting());
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

    @Override
    public void setForeground(Color fg) {
        super.setForeground(fg);
        copyUiStateToSubComponents();
    }


    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        copyUiStateToSubComponents();
    }


    protected void copyUiStateToSubComponents() {
        for (Component c : this.getComponents()) {
            copyUiStateToSubComponent(c);
        }
    }

    /**
     * The base class (JImageListView) calls this whenever a UI state like
     * foreground/background colors should to be copied from this component
     * to a child component (c). This happens when (a) such a UI state
     * was changed or (b) when a new child component was added. JImageListView
     * ensures that this is done correctly ((b) is ensured by the constructor
     * calling {@link #ensureUiStateIsCopiedForAddedComponents() }). The default
     * implementation copies the foreground and background color. Subclasses
     * may override to copy additional properties.
     *
     * @param c
     */
    protected void copyUiStateToSubComponent(Component c) {
        c.setForeground(this.getForeground());
        c.setBackground(this.getBackground());
    }

    /**
     * Ensures {@link #copyUiStateToSubComponent(java.awt.Component) } will
     * be called for any child component added to this component in the
     * future. Called once by the default constructor of JImageListView.
     * Subclasses may (rarely) override, e.g. with an empty implementation
     * if they want to inhibit this behaviour for some (strange) reason.
     */
    protected void ensureUiStateIsCopiedForAddedComponents() {
        this.addContainerListener(new ContainerAdapter() {
            @Override
            public void componentAdded(ContainerEvent e) {
                JImageListView.this.copyUiStateToSubComponent(e.getChild());
            }
        });
    }

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    // TODO: the following methods (generated by Netbeans/"insert code"/Add Property../[x] Bound)
    // don't call the overridden versions inherited from Container, thereby breaking the latter's
    // bound property support? Investigate. Add super calls if deemed necessary.

    /**
     * Add a PropertyChangeListener that receives property change events for this
     * list's properties.
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


    private Collection<ImageListViewListener> imageListViewListeners =
            new ArrayList<ImageListViewListener>();

    public void addImageListViewListener(ImageListViewListener listener) {
        imageListViewListeners.add(listener);
    }

    public void removeImageListViewListener(ImageListViewListener listener) {
        imageListViewListeners.remove(listener);
    }

    protected void fireImageListViewEvent(ImageListViewEvent e) {
        for (ImageListViewListener l : imageListViewListeners) {
            l.onImageListViewEvent(e);
        }
    }


    private Collection<PropertyChangeListener> cellPropertyChangeListeners =
            new ArrayList<PropertyChangeListener>();

    /**
     * Add a PropertyChangeListener that receives property change events for all
     * properties of all cells of this list. This is a convenient way to be
     * informed of all such property changes without having to manually add
     * listeners to all cells (and track elements and corresponding cells being
     * added/removed to the list, and correctly add/remove the listeners to them).
     * This method takes care of all that internally.
     *
     * @param listener
     */
    public void addCellPropertyChangeListener(PropertyChangeListener listener) {
        cellPropertyChangeListeners.add(listener);
    }

    public void removeCellPropertyChangeListener(PropertyChangeListener listener) {
        cellPropertyChangeListeners.remove(listener);
    }

    protected void fireCellPropertyChangeEvent(PropertyChangeEvent e) {
        for (PropertyChangeListener l : cellPropertyChangeListeners) {
            l.propertyChange(e);
        }
    }



    private Collection<MouseListener> cellMouseListeners =
            new ArrayList<MouseListener>();

    /**
     * Add a MouseListener that receives mouse events for all cells of this list.
     * This is a convenient way to be
     * informed of all such mouse events without having to manually add
     * listeners to all cells (and track elements and corresponding cells being
     * added/removed to the list, and correctly add/remove the listeners to them).
     * This method takes care of all that internally.
     *
     * @param listener
     */
    public void addCellMouseListener(MouseListener listener) {
        cellMouseListeners.add(listener);
    }

    public void removeCellMouseListener(MouseListener listener) {
        cellMouseListeners.remove(listener);
    }

    protected void fireCellMouseEvent(MouseEvent e) {
        if (e.getID() == MouseEvent.MOUSE_MOVED || e.getID() == MouseEvent.MOUSE_DRAGGED) {
            fireCellMouseMotionEvent(e);
            return;
        }
        for (MouseListener l : cellMouseListeners) {
            switch (e.getID()) {
                case MouseEvent.MOUSE_CLICKED:
                    l.mouseClicked(e);
                    break;
                case MouseEvent.MOUSE_PRESSED:
                    l.mousePressed(e);
                    break;
                case MouseEvent.MOUSE_RELEASED:
                    l.mouseReleased(e);
                    break;
                case MouseEvent.MOUSE_ENTERED:
                    l.mouseEntered(e);
                    break;
                case MouseEvent.MOUSE_EXITED:
                    l.mouseExited(e);
                    break;
            }
        }
    }


    private Collection<MouseMotionListener> cellMouseMotionListeners =
            new ArrayList<MouseMotionListener>();

    /**
     * Like {@link #addCellMouseListener(java.awt.event.MouseListener) }, but for
     * MouseMotionListeners.
     *
     * @param listener
     */
    public void addCellMouseMotionListener(MouseMotionListener listener) {
        cellMouseMotionListeners.add(listener);
    }

    public void removeCellMouseMotionListener(MouseMotionListener listener) {
        cellMouseMotionListeners.remove(listener);
    }

    protected void fireCellMouseMotionEvent(MouseEvent e) {
        for (MouseMotionListener l : cellMouseMotionListeners) {
            switch (e.getID()) {
                case MouseEvent.MOUSE_MOVED:
                    l.mouseMoved(e);
                    break;
                case MouseEvent.MOUSE_DRAGGED:
                    l.mouseDragged(e);
                    break;
            }
        }
    }


    private Collection<MouseWheelListener> cellMouseWheelListeners =
            new ArrayList<MouseWheelListener>();

    /**
     * Like {@link #addCellMouseListener(java.awt.event.MouseListener) }, but for
     * MouseWheelListener.
     *
     * @param listener
     */
    public void addCellMouseWheelListener(MouseWheelListener listener) {
        cellMouseWheelListeners.add(listener);
    }

    public void removeCellMouseWheelListener(MouseWheelListener listener) {
        cellMouseWheelListeners.remove(listener);
    }

    protected void fireCellMouseWheelEvent(MouseWheelEvent e) {
        for (MouseWheelListener l : cellMouseWheelListeners) {
            l.mouseWheelMoved(e);
        }
    }


}
