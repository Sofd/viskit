package de.sofd.viskit.ui.imagelist/*<$!subPackage>*/;

import java.awt.Dimension;
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
import java.util.EventListener;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import de.sofd.draw2d.viewer.DrawingViewer;
import de.sofd.draw2d.viewer.backend.DrawingViewerBackend;
import de.sofd.lang.Runnable1;
import de.sofd.util.BiIdentityHashMap;
import de.sofd.util.BiMap;
import de.sofd.util.IdentityHashSet;
import de.sofd.util.Misc;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.event.ImageListViewCellAddEvent;
import de.sofd.viskit.ui.imagelist.event.ImageListViewCellRemoveEvent;
import de.sofd.viskit.ui.imagelist.event.ImageListViewEvent;
import de.sofd.viskit.ui.imagelist.event.ImageListViewListener;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintEvent;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintListener;

/*<import de.sofd.viskit.ui.imagelist.*;>*/

/**
 * Basic implementation of the {@link ImageListView} interface. Meant to be
 * extended by, aggregated by, or (mostly) <em>mixed into</em> actual
 * implementations. We need mixins here because actual (instantiatable)
 * ImageListView implementations would normally have to be derived from some
 * toolkit/technology-specific base class, e.g. javax.swing.JPanel, which means
 * that they could no longer be derived from this class to get all the base
 * functionality. What we want is to "mix" this class's fields and methods into
 * an actual implementation. To do this, we use a <em>preprocessor</em> which
 * runs at build time and copies this Java file into a target Java file that
 * defines a class that's identical to this one except that it has a different
 * name and is derived from a the needed toolkit-specific base class like
 * JPanel. From this preprocessor-generated class actual implementations would
 * then be derived. The preprocessor is a small Java program that lives in the
 * build-dep/ subdirectory of the project folder and is called by its
 * accompanying Ant build file, which in turn is called by the IDE-specific
 * build process.
 * <p>
 * At the moment, this class in only used in this way (as a mixin source for the
 * preprocessor build step). So it wouldn't actually need to be compiled because
 * it is not directly needed at runtime.
 * 
 * @author olaf
 */
public abstract class ImageListViewBaseImpl /*< extends $baseClass >*/ implements ImageListView {

    static final Logger logger = Logger.getLogger(ImageListViewBaseImpl.class);

    private ListModel model;
    private ListSelectionModel selectionModel;
    private int firstVisibleIndex = 0;
    private Integer lowerVisibilityLimit, upperVisibilityLimit;
    private final List<ListSelectionListener> listSelectionListeners = new ArrayList<ListSelectionListener>();
    private String displayName = "";
    private ScaleMode scaleMode;
    private final Map<ImageListViewCell, Integer> cellToIndexMap = new IdentityHashMap<ImageListViewCell, Integer>();
    private final List<CompListener> compListeners = new ArrayList<CompListener>();
    
    private final ImageListViewBackend backend;
    
    
    // TODO: it's probably better to use a map that uses normal (equals()/hashCode()-based)
    //       mapping for the modelElement => cell direction
    // TODO: maybe this should really be list of cell objects that tracks the model
    //       (a ListModel may contain more than one identical or equal elements, in which case the
    //       cell => element mapping would fail). Or, explicitly forbid having the same element
    //       in the model more than once (we kind of do that for now; see the modelChangeListener below)
    private BiMap<ImageListViewModelElement, ImageListViewCell> cellsByElementMap
            = new BiIdentityHashMap<ImageListViewModelElement, ImageListViewCell>();

    public ImageListViewBaseImpl(ImageListViewBackend backend) {
        this.backend = backend;
    }

    /**
     * Get the value of model
     *
     * @return the value of model
     */
    @Override
    public ListModel getModel() {
        return model;
    }

//</  #if ($baseClass != "de.matthiasmann.twl.Widget")  ## Widget already defines the property event methods as final, thus redefining them wouldn't work
    
    private PropertyChangeSupport propertyChangeSupport; //initialized lazily

    protected PropertyChangeSupport getPropertyChangeSupport() {
        if (null == propertyChangeSupport) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        return propertyChangeSupport;
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        getPropertyChangeSupport().addPropertyChangeListener(listener);
    }

    @Override
    public void addPropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        getPropertyChangeSupport().addPropertyChangeListener(propertyName, listener);
    }

    public void firePropertyChange(PropertyChangeEvent evt) {
        getPropertyChangeSupport().firePropertyChange(evt);
    }

    public void firePropertyChange(String propertyName, boolean oldValue,
            boolean newValue) {
        getPropertyChangeSupport().firePropertyChange(propertyName, oldValue,
                newValue);
    }

    public void firePropertyChange(String propertyName, int oldValue,
            int newValue) {
        getPropertyChangeSupport().firePropertyChange(propertyName, oldValue,
                newValue);
    }

    public void firePropertyChange(String propertyName, Object oldValue,
            Object newValue) {
        getPropertyChangeSupport().firePropertyChange(propertyName, oldValue,
                newValue);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        getPropertyChangeSupport().removePropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        getPropertyChangeSupport().removePropertyChangeListener(propertyName,
                listener);
    }

//</  #end

    @Override
    public void addCompListener(CompListener listener) {
        compListeners.add(listener);
    }
    
    @Override
    public void removeCompListener(CompListener listener) {
        compListeners.remove(listener);
    }
    
    public void fireCompSizeChange(Dimension oldSize, Dimension newSize) {
        for(CompListener listener : compListeners) {
            listener.compResized(oldSize, newSize);
        }
    }
    
    /**
     * Set the value of model
     *
     * @param model new value of model
     */
    @Override
    public void setModel(ListModel model) {
        ListModel oldModel = this.model;
        if (oldModel != null) {
            oldModel.removeListDataListener(modelChangeListener);
            clearCellsByElementMap();
            cellToIndexMap.clear();
        }
        this.model = model;
        if (this.model != null) {
            fillCellsByElementMap();
            this.model.addListDataListener(modelChangeListener);
        }
        firePropertyChange(PROP_MODEL, oldModel, model);
        refreshCells();
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
                if (i < 0 || i >= getModel().getSize()) {
                    continue;
                }
                ImageListViewModelElement elt = (ImageListViewModelElement) getModel().getElementAt(i);
                if (! cellsByElementMap.containsKey(elt)) {
                    elt.addPropertyChangeListener(modelElementPropertyChangeEventForwarder);
                    ImageListViewCell cell = createCell(elt, -1);
                    cellsByElementMap.put(elt, cell);
                    newElements = true;
                }
            }
            if (newElements) {
                garbageCollectStaleCells();
            }
            ImageListViewBaseImpl.this.modelContentsChanged(e);
        }
        @Override
        public void intervalAdded(ListDataEvent e) {
            for (int i = e.getIndex0(); i <= e.getIndex1(); i++) {
                ImageListViewModelElement elt = (ImageListViewModelElement) getModel().getElementAt(i);
                if (cellsByElementMap.containsKey(elt)) {
                    throw new IllegalStateException("JImageListView doesn't support adding the same model element (" + elt + ") more than once");
                }
                elt.addPropertyChangeListener(modelElementPropertyChangeEventForwarder);
                ImageListViewCell cell = createCell(elt, -1);
                cellsByElementMap.put(elt, cell);
            }
            ImageListViewBaseImpl.this.modelIntervalAdded(e);
        }
        @Override
        public void intervalRemoved(ListDataEvent e) {
            ImageListViewBaseImpl.this.modelIntervalRemoved(e);
            garbageCollectStaleCells();
        }
    };

    @Override
    public int getLength() {
        if (null == getModel()) {
            return 0;
        } else {
            return getModel().getSize();
        }
    }

    @Override
    public ImageListViewModelElement getElementAt(int index) {
        return (ImageListViewModelElement) (getModel().getElementAt(index));
    }

    @Override
    public ImageListViewCell getCellForElement(ImageListViewModelElement elt) {
        return cellsByElementMap.get(elt);
    }

    @Override
    public ImageListViewModelElement getElementForCell(ImageListViewCell cell) {
        return cellsByElementMap.reverseGet(cell);
    }

    @Override
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
            cellToIndexMap.remove(cell);
            elt.removePropertyChangeListener(modelElementPropertyChangeEventForwarder);
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
    @Override
    public ScaleMode getScaleMode() {
        return scaleMode;
    }

    /**
     * Set the scaleMode ({@link #getScaleMode()}) of this viewer to one of the {@link #getSupportedScaleModes() }
     * (calling this with an unsupported scale mode will raise an exception). Sets the bean property,
     * fires the corresponding PropertyChangeEvent, and (before firing, but after setting the property) calls
     * {@link \#doSetScaleMode(de.sofd.viskit.ui.imagelist.ImageListViewBaseImpl.ScaleMode, de.sofd.viskit.ui.imagelist.ImageListViewBaseImpl.ScaleMode) },
     * which subclasses should override normally (rather than overriding this method).
     *
     * @param scaleMode new value of scaleMode
     */
    @Override
    public void setScaleMode(ScaleMode scaleMode) {
        //if (! getSupportedScaleModes().contains(scaleMode)) {
        //    throw new IllegalArgumentException("Unsupported scale mode: " + scaleMode);
        //}
        if (Misc.equal(scaleMode, getScaleMode())) {
            return;
        }
        ScaleMode oldScaleMode = this.scaleMode;
        this.scaleMode = scaleMode;
        try {
            doSetScaleMode(oldScaleMode, this.scaleMode);
        } catch (Exception e) {
            //TODO: test this out...
            logger.error("Exception in doSetScaleMode. Resetting to previous scaleMode.", e);
            setScaleMode(oldScaleMode);
            throw new IllegalStateException("Exception in doSetScaleMode. Previous scaleMode was successfully restored.", e);
        }
        firePropertyChange(PROP_SCALEMODE, oldScaleMode, scaleMode);
    }

    /**
     * Actual setter for the {@link #getScaleMode() }. oldScaleMode is the previous scaleMode,
     * newScale mode is the new one (which will already be in {@link #getScaleMode() } by the time
     * this method is called, so this parameter is just a convenience). Default impl. ist empty,
     * which is pretty useless unless the subclass doesn't support any ScaleModes or doesn't want
     * to do anything when the scaleMode is set (which would be kind of pointless). Thus, subclasses
     * should normally override this method. Alternatively, they could override
     * {@link \#setScaleMode(de.sofd.viskit.ui.imagelist.ImageListViewBaseImpl.ScaleMode) } and
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
     * Creates a new ImageListViewCell (using doCreateCell()), ensures the cell's
     * PropertyChangeEvents are exposed to {@link \#addCellPropertyChangeListener(java.beans.PropertyChangeListener) },
     * and calls {@link \#ensureCellRepaintsOnRoiDrawingViewerChanges(de.sofd.viskit.ui.imagelist.ImageListViewCell) }.
     * Not normally called by subclasses (and never called by users). Hardly ever overridden;
     * override {@link \#doCreateCell(de.sofd.viskit.ui.imagelist.ImageListViewModelElement) } instead.
     *
     * @param modelElement
     *            model element to create the cell for
     * @param index
     *            index of the element in the list, or -1 if unknown. The method
     *            may make use of this for initializing stuff or whatever
     * @return
     */
    protected ImageListViewCell createCell(ImageListViewModelElement modelElement, int index) {
        ImageListViewCell cell = doCreateCell(modelElement);
        if (index != -1) {
            //intialize these mappings so the repaint that's indirectly triggered
            //by ensureCellRepaintsOnRoiDrawingViewerChanges() below doesn't too long
            //in refreshCell(cell) in JGridILV. Fixes ticket #43.
            cellToIndexMap.put(cell, index);
            cellsByElementMap.put(modelElement, cell);
        }
        cell.addPropertyChangeListener(cellPropertyChangeEventForwarder);
        fireImageListViewEvent(new ImageListViewCellAddEvent(this, cell));
        ensureCellRepaintsOnRoiDrawingViewerChanges(cell);
        return cell;
    }

    /**
     * Called by {@link \#createCell(de.sofd.viskit.ui.imagelist.ImageListViewModelElement) }
     * for newly created cells. Ensures that any changes to the cell's ROI drawing viewer
     * cause the cell to be repainted. Default impl. registers a {@link DrawingViewerBackend}
     * on the ROI drawing viewer that calls {@link \#refreshCell(de.sofd.viskit.ui.imagelist.ImageListViewCell) }
     * whenever a repaint is requested.
     *
     * @param cell
     */
    protected void ensureCellRepaintsOnRoiDrawingViewerChanges(final ImageListViewCell cell) {
        cell.getRoiDrawingViewer().setBackend(new DrawingViewerBackend() {
            @Override
            public void connected(DrawingViewer viewer) {
            }
            @Override
            public void repaint() {
                refreshCell(cell);
            }
            @Override
            public void repaint(double x, double y, double width, double height) {
                refreshCell(cell);
            }
            @Override
            public void disconnecting() {
            }
        });
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
            ImageListViewBaseImpl.this.fireCellPropertyChangeEvent(evt);
        }
    };

    private PropertyChangeListener modelElementPropertyChangeEventForwarder = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            onModelElementPropertyChange(evt);
            ImageListViewBaseImpl.this.fireModelElementPropertyChangeEvent(evt);
        }
    };

    /**
     * Default internal callback that's invoked for any PropertyChangeEvent on
     * any model element in the list. Default implementation refreshed the
     * element/cell. Subclasses may override, possibly calling super, to
     * implement their own handling for this.
     * 
     * @param evt
     */
    protected void onModelElementPropertyChange(PropertyChangeEvent evt) {
        refreshCellForElement((ImageListViewModelElement)evt.getSource());
    }

    @Override
    public ImageListViewCell getCellFor(ImageListViewModelElement modelElement) {
        return cellsByElementMap.get(modelElement);
    }

    private void clearCellsByElementMap() {
        for (ImageListViewModelElement elt: cellsByElementMap.keySet()) {
            ImageListViewCell cell = cellsByElementMap.get(elt);
            fireImageListViewEvent(new ImageListViewCellRemoveEvent(this, cell));
            beforeCellRemoval(cell, elt);
            elt.removePropertyChangeListener(modelElementPropertyChangeEventForwarder);
        }
        cellsByElementMap.clear();
    }

    private void fillCellsByElementMap() {
        for (int i = 0; i < getModel().getSize(); i++) {
            ImageListViewModelElement elt = (ImageListViewModelElement) getModel().getElementAt(i);
            elt.addPropertyChangeListener(modelElementPropertyChangeEventForwarder);
            ImageListViewCell cell = createCell(elt, i);
            cellsByElementMap.put(elt, cell);
        }
    }

    /**
     * Get the value of selectionModel
     *
     * @return the value of selectionModel
     */
    @Override
    public ListSelectionModel getSelectionModel() {
        return selectionModel;
    }

    /**
     * Set the value of selectionModel
     *
     * @param selectionModel new value of selectionModel
     */
    @Override
    public void setSelectionModel(ListSelectionModel selectionModel) {
        ListSelectionModel oldSelectionModel = this.selectionModel;
        if (oldSelectionModel != null) {
            oldSelectionModel.removeListSelectionListener(listSelectionListener);
        }
        this.selectionModel = selectionModel;
        if (this.selectionModel != null) {
            this.selectionModel.addListSelectionListener(listSelectionListener);
        }
        firePropertyChange(PROP_SELECTIONMODEL, oldSelectionModel, selectionModel);
    }

    @Override
    public int getSelectedIndex() {
        return getMinSelectionIndex();
    }

    @Override
    public ImageListViewModelElement getSelectedValue() {
        int i = getMinSelectionIndex();
        return (i == -1) ? null : (ImageListViewModelElement) getModel().getElementAt(i);
    }

    @Override
    public ImageListViewModelElement[] getSelectedValues() {
        ListSelectionModel sm = getSelectionModel();
        int minSI = sm.getMinSelectionIndex();
        int maxSI = sm.getMaxSelectionIndex();
        ImageListViewModelElement[] tmp = new ImageListViewModelElement[1 + (maxSI - minSI)];
        int n = 0;
        for (int i = minSI; i <= maxSI; i++) {
            if (sm.isSelectedIndex(i)) {
                tmp[n++] = (ImageListViewModelElement) getModel().getElementAt(i);
            }
        }
        ImageListViewModelElement[] result = new ImageListViewModelElement[n];
        System.arraycopy(tmp, 0, result, 0, n);
        return result;
    }

    @Override
    public int[] getSelectedIndices() {
        ListSelectionModel sm = getSelectionModel();
        int minSI = sm.getMinSelectionIndex();
        int maxSI = sm.getMaxSelectionIndex();
        int[] tmp = new int[1 + (maxSI - minSI)];
        int n = 0;
        for (int i = minSI; i <= maxSI; i++) {
            if (sm.isSelectedIndex(i)) {
                tmp[n++] = i;
            }
        }
        int[] result = new int[n];
        System.arraycopy(tmp, 0, result, 0, n);
        return result;
    }

    @Override
    public int getMinSelectionIndex() {
        return getSelectionModel().getMinSelectionIndex();
    }

    @Override
    public int getMaxSelectionIndex() {
        return getSelectionModel().getMaxSelectionIndex();
    }

    @Override
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
    @Override
    public void addListSelectionListener(ListSelectionListener listener) {
        listSelectionListeners.add(listener);
    }

    @Override
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
            ImageListViewBaseImpl.this.selectionChanged(e);
            fireListSelectionEvent(e.getFirstIndex(), e.getLastIndex(), e.getValueIsAdjusting());
        }
    };

    /**
     * Called if the selection of this list has changed. Default implementation
     * calls {@link #refreshCells()}, subclasses may override.
     * 
     * @param e
     */
    protected void selectionChanged(ListSelectionEvent e) {
        refreshCells();
    }

    /**
     * Find the index of a cell in this list. Reverse operation to
     * {@link \#getCell(int)}.
     * <p>
     * This operation will generally be efficient (O(1)) if the cell is present
     * in the list.
     * 
     * @param cell
     *            a cell of this list
     * @return index of cell in this list, or -1 if the cell isn't part of the
     *         list
     */
    @Override
    public int getIndexOf(ImageListViewCell cell) {
        // use cellToIndexMap to look up cell's index quickly, creating/updating
        // cellToIndexMap lazily along the way if necessary.
        // In most cases, the cellToIndexMap should contain the correct index for cell.
        // we optimize for that case (O(1)).
        // cellToIndexMap will become temporarily outdated if somebody
        // adds/removes cells from the list. We catch such cases here.
        if (null == getModel()) { return -1; }
        Integer index = cellToIndexMap.get(cell);
        ImageListViewCell foundCell = null;
        if (index != null && index < getModel().getSize()) {
            foundCell = getCell(index);
        }
        if (index != null && foundCell == cell) {
            return index;
        } else {
            int eltCount = getModel().getSize();
            for (int i = 0; i < eltCount; i++) {
                ImageListViewCell c2 = getCell(i);
                cellToIndexMap.put(c2, i);
                if (c2 == cell) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    /**
     *
     * @return start of currently displayed interval of model elements
     */
    @Override
    public int getFirstVisibleIndex() {
       return firstVisibleIndex;
    }

    /**
     * Programmatically "scrolls" this list to a different position by setting
     * the index of the first model element to display.
     * <p>
     * Only elements within the bounds of {@link #getLowerVisibilityLimit()} /
     * {@link #getUpperVisibilityLimit()} may be visible.
     * <p>
     * This base class method deals with changing the property value (
     * {@link #getFirstVisibleIndex()}) and firing the property change event. It
     * also accounts for the visibility bounds explained above -- i.e., if the
     * new index would lead to indices outside the bounds to be displayed, the
     * method will do nothing. For predicting the new lastVisible index in this
     * case, it is assumed that {@link #getLastVisibleIndex()} -
     * {@link #getFirstVisibleIndex()} will not be modified by changes to the
     * firstVisibleIndex. This may or may not be an appropriate way to do this,
     * depending on the implementation.
     * <p>
     * Subclasses must override, possibly calling super, or re-implementing both
     * the getter and the setter from scratch.
     * 
     * @param newValue
     *            the new index
     */
    @Override
    public void setFirstVisibleIndex(int newValue) {
        Integer lowerLimit = getLowerVisibilityLimit();
        if (null != lowerLimit && newValue < lowerLimit) {
            //return;
            newValue = lowerLimit;
        }
        Integer upperLimit = getUpperVisibilityLimit();
        if (null != upperLimit) {
            int predictedNewLastVisible = getLastVisibleIndex() + (newValue - getFirstVisibleIndex());
            if (predictedNewLastVisible > upperLimit) {
                //return;
                newValue -= (predictedNewLastVisible - upperLimit);
                if (newValue < 0) {
                    // may happen if lower and upper limit are very close
                    newValue = 0;
                }
            }
        }
        int oldValue = getFirstVisibleIndex();
        if (newValue == oldValue) { return; }
        this.firstVisibleIndex = newValue;
        firePropertyChange(PROP_FIRSTVISIBLEINDEX, oldValue, newValue);
    }

    @Override
    public abstract int getLastVisibleIndex();
    
    @Override
    public boolean isVisibleIndex(int i) {
        return i >= getFirstVisibleIndex() && i <= getLastVisibleIndex() && getModel() != null && i < getModel().getSize();
    }
    
    @Override
    public abstract void ensureIndexIsVisible(int idx);

    /**
     * Returns the current lower limit of indices that may be made visible by
     * scrolling. Scrolling further than this limit should be prevented by the
     * list. null indicates "no limit".
     * <p>
     * The default implementation just returns the property set by
     * {@link #setLowerVisibilityLimit(Integer)}. Subclasses may
     * override/replace.
     * 
     * @return current lower limit, or null for "no limit"
     */
    @Override
    public Integer getLowerVisibilityLimit() {
        return lowerVisibilityLimit;
    }

    /**
     * Sets the lower visibility limit.
     * <p>
     * This base class method only deals with changing the property value (
     * {@link #getLowerVisibilityLimit()}) and firing the property change event.
     * Subclasses must override, possibly calling super, or re-implementing both
     * the getter and the setter from scratch.
     * 
     * @param newValue
     *            the new index
     */
    @Override
    public void setLowerVisibilityLimit(Integer newValue) {
        Integer oldValue = getLowerVisibilityLimit();
        if (newValue == oldValue || (newValue != null && oldValue != null && newValue.intValue() == oldValue.intValue())) {
            return;
        }
        this.lowerVisibilityLimit = newValue;
        firePropertyChange(PROP_LOWERVISIBILITYLIMIT, oldValue, newValue);
    }
    
    /**
     * Returns the current upper limit of indices that may be made visible by
     * scrolling. Scrolling further than this limit should be prevented by the
     * list. null indicates "no limit".
     * <p>
     * The default implementation just returns the property set by
     * {@link #setUpperVisibilityLimit(Integer)}. Subclasses may
     * override/replace.
     * 
     * @return current lower limit, or null for "no limit"
     */
    @Override
    public Integer getUpperVisibilityLimit() {
        return upperVisibilityLimit;
    }
    
    /**
     * Sets the upper visibility limit.
     * <p>
     * This base class method only deals with changing the property value (
     * {@link #getLowerVisibilityLimit()}) and firing the property change event.
     * Subclasses must override, possibly calling super, or re-implementing both
     * the getter and the setter from scratch.
     * 
     * @param newValue
     *            the new index
     */
    @Override
    public void setUpperVisibilityLimit(Integer newValue) {
        Integer oldValue = getUpperVisibilityLimit();
        if (newValue == oldValue || (newValue != null && oldValue != null && newValue.intValue() == oldValue.intValue())) {
            return;
        }
        this.upperVisibilityLimit = newValue;
        firePropertyChange(PROP_UPPERVISIBILITYLIMIT, oldValue, newValue);
    }
    
    @Override
    public void disableVisibilityLimits() {
        setLowerVisibilityLimit(null);
        setUpperVisibilityLimit(null);
    }
    
    @Override
    public void selectSomeVisibleCell() {
        ListSelectionModel sm = getSelectionModel();
        if (sm != null) {
            int idx = sm.getLeadSelectionIndex();
            if (idx != -1) {
                if (idx < getFirstVisibleIndex()) {
                    sm.setSelectionInterval(getFirstVisibleIndex(), getFirstVisibleIndex());
                } else if (idx > getLastVisibleIndex()) {
                    sm.setSelectionInterval(getLastVisibleIndex(), getLastVisibleIndex());
                }
            }
        }
    }
    
    @Override
    public void scrollToSelection() {
        ListSelectionModel sm = getSelectionModel();
        if (null != sm) {
            int li = sm.getLeadSelectionIndex();
            if (sm.isSelectedIndex(li)) {
                ensureIndexIsVisible(li);
            }
        }
    }
    
    /**
     * Called if a the cells of this viewer need to be refreshed. Normally
     * called internally by subclasses if they determine the need to refresh the cells,
     * but may also be called explicitly from the outside.
     * <p>
     * Uses the same special casing hack as {@link #refreshCellForIndex(int) }.
     */
    @Override
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
    @Override
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
     * called internally by subclasses if they determine the need to refresh a
     * cell, but may also be called explicitly from the outside.
     * <p>
     * Default implementation finds the cell displaying elt, then calls
     * {@link #refreshCell(ImageListViewCell))}.
     * 
     * @param elt
     *            model element corresponding to the cell
     */
    @Override
    public void refreshCellForElement(ImageListViewModelElement elt) {
        if (null == getModel()) { return; }
        ImageListViewCell cell = cellsByElementMap.get(elt);
        if (null != cell) {
            refreshCell(cell);
        }
    }

    /**
     * Called if a specific cell of this viewer needs to be refreshed. Normally
     * called internally by subclasses if they determine the need to refresh a
     * cell, but may also be called explicitly from the outside.
     * <p>
     * Default implementation uses {@link #getIndexOf(ImageListViewCell)} and
     * then {@link #refreshCellForIndex(int) }.
     * 
     * @param cell
     *            the cell
     */
    @Override
    public void refreshCell(ImageListViewCell cell) {
        int index = getIndexOf(cell);
        if (index != -1) {
            refreshCellForIndex(index);
        }
    }

    private Collection<ImageListViewListener> imageListViewListeners =
            new ArrayList<ImageListViewListener>();

    @Override
    public void addImageListViewListener(ImageListViewListener listener) {
        imageListViewListeners.add(listener);
    }

    @Override
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
    @Override
    public void addCellPropertyChangeListener(PropertyChangeListener listener) {
        cellPropertyChangeListeners.add(listener);
    }

    @Override
    public void removeCellPropertyChangeListener(PropertyChangeListener listener) {
        cellPropertyChangeListeners.remove(listener);
    }

    protected void fireCellPropertyChangeEvent(PropertyChangeEvent e) {
        for (PropertyChangeListener l : cellPropertyChangeListeners) {
            l.propertyChange(e);
        }
    }


    private Collection<PropertyChangeListener> modelElementPropertyChangeListeners =
        new ArrayList<PropertyChangeListener>();

    /**
     * Add a PropertyChangeListener that receives property change events for all
     * properties of all model elements of this list. This is a convenient way
     * to be informed of all such property changes without having to manually
     * add listeners to all model elements (and track elements being
     * added/removed to the list, and correctly add/remove the listeners to
     * them). This method takes care of all that internally.
     * 
     * @param listener
     */
    @Override
    public void addModelElementPropertyChangeListener(PropertyChangeListener listener) {
        modelElementPropertyChangeListeners.add(listener);
    }
    
    @Override
    public void removeModelElementPropertyChangeListener(PropertyChangeListener listener) {
        modelElementPropertyChangeListeners.remove(listener);
    }
    
    protected void fireModelElementPropertyChangeEvent(PropertyChangeEvent e) {
        for (PropertyChangeListener l : modelElementPropertyChangeListeners) {
            l.propertyChange(e);
        }
    }

    /**
     * Add a MouseListener that receives mouse events for all cells of this list.
     * This is a convenient way to be
     * informed of all such mouse events without having to manually add
     * listeners to all cells (and track elements and corresponding cells being
     * added/removed to the list, and correctly add/remove the listeners to them).
     * This method takes care of all that internally.
     * <p>
     * The event's source will be the cell, the mouse coordinates will be relative
     * to the cell's upper-left border.
     * <p>
     * The zOrder parameter specifies the z position at which the mouse listener is
     * added. Mouse listeners with higher z positions are invoked before ones with
     * lower z positions. You may use one of the PAINT_ZORDER_* constants for
     * pre-defined z positions.
     *
     * @param zOrder
     * @param listener
     */
    @Override
    public void addCellMouseListener(int zOrder, MouseListener listener) {
        addAnyCellMouseListener(zOrder, listener);
    }

    @Override
    public void addCellMouseListener(MouseListener listener) {
        addAnyCellMouseListener(PAINT_ZORDER_DEFAULT, listener);
    }
    
    @Override
    public void removeCellMouseListener(MouseListener listener) {
        removeAnyCellMouseListener(listener);
    }
    
    protected void fireCellMouseEvent(MouseEvent e) {
        fireAnyCellMouseEvent(e);
    }

    /**
     * Like {@link \#addCellMouseListener(int, java.awt.event.MouseListener) }, but for
     * MouseMotionListeners.
     *
     * @param zOrder
     * @param listener
     */
    @Override
    public void addCellMouseMotionListener(int zOrder, MouseMotionListener listener) {
        addAnyCellMouseListener(zOrder, listener);
    }

    @Override
    public void addCellMouseMotionListener(MouseMotionListener listener) {
        addAnyCellMouseListener(PAINT_ZORDER_DEFAULT, listener);
    }
    
    @Override
    public void removeCellMouseMotionListener(MouseMotionListener listener) {
        removeAnyCellMouseListener(listener);
    }

    protected void fireCellMouseMotionEvent(MouseEvent e) {
        fireAnyCellMouseEvent(e);
    }


    /**
     * Like {@link \#addCellMouseListener(int, java.awt.event.MouseListener) }, but for
     * MouseWheelListener.
     *
     * @param zOrder
     * @param listener
     */
    @Override
    public void addCellMouseWheelListener(int zOrder, MouseWheelListener listener) {
        addAnyCellMouseListener(zOrder, listener);
    }

    @Override
    public void addCellMouseWheelListener(MouseWheelListener listener) {
        addAnyCellMouseListener(PAINT_ZORDER_DEFAULT, listener);
    }
    
    @Override
    public void removeCellMouseWheelListener(MouseWheelListener listener) {
        removeAnyCellMouseListener(listener);
    }

    protected void fireCellMouseWheelEvent(MouseWheelEvent e) {
        fireAnyCellMouseEvent(e);
    }


    
    private NavigableSet<ListenerRecord<EventListener>> cellMouseListeners =
        new TreeSet<ListenerRecord<EventListener>>();
    
    protected void addAnyCellMouseListener(int zOrder, EventListener listener) {
        // check if it's been added before already. TODO: this is not really correct, get rid of it?
        //   (it was added for compatibility with clients that call all three add methods with just
        //   one listener instance (extending MouseHandler and thus implementing all Mouse*Listener interfaces),
        //   and expect the listener to be called only once per event.
        //   Check how standard Swing components handle this)
        for (Iterator<ListenerRecord<EventListener>> it = cellMouseListeners.iterator(); it.hasNext();) {
            if (it.next().listener == listener) {
                return;
            }
        }
        cellMouseListeners.add(new ListenerRecord<EventListener>(listener, zOrder));
    }

    protected void removeAnyCellMouseListener(EventListener listener) {
        for (Iterator<ListenerRecord<EventListener>> it = cellMouseListeners.iterator(); it.hasNext();) {
            if (it.next().listener == listener) {
                it.remove();
                return;
            }
        }
    }

    protected void fireAnyCellMouseEvent(MouseEvent e) {
        for (ListenerRecord<EventListener> rec : cellMouseListeners.descendingSet()) {
            boolean eventProcessed = false;
            if (rec.listener instanceof MouseWheelListener && e instanceof MouseWheelEvent) {
                MouseWheelListener l = (MouseWheelListener) rec.listener;
                l.mouseWheelMoved((MouseWheelEvent) e);
                eventProcessed = true;
            }
            if (!eventProcessed && rec.listener instanceof MouseMotionListener) {
                MouseMotionListener l = (MouseMotionListener) rec.listener;
                switch (e.getID()) {
                case MouseEvent.MOUSE_MOVED:
                    l.mouseMoved(e);
                    eventProcessed = true;
                    break;
                case MouseEvent.MOUSE_DRAGGED:
                    l.mouseDragged(e);
                    eventProcessed = true;
                    break;
                }
            }
            if (!eventProcessed && rec.listener instanceof MouseListener) {
                MouseListener l = (MouseListener) rec.listener;
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
            if (e.isConsumed()) {
                break;
            }
        }
    }
    


    /**
     * Add a new listener for CellPaintEvents, with default z-order
     * (PAINT_ZORDER_DEFAULT).
     * 
     * @param listener
     */
    @Override
    public void addCellPaintListener(ImageListViewCellPaintListener listener) {
        addCellPaintListener(PAINT_ZORDER_DEFAULT, listener);
    }

    /**
     * Add a new listener to for CellPaintEvents. The listener will be invoked
     * whenever a cell in this list needs to be (re)painted. The zOrder
     * parameter determines the order in which the listeners are invoked (if
     * more than one listener was added). Listeners with higher zOrder numbers
     * are called after ones with lower zOrder numbers (when the numbers are
     * equal, insertion order decides). A listener that is called later will end
     * up drawing on top of what the listeners called earlier have drawn.
     * <p>
     * You can pass any of the PAINT_ZORDER_* constants as the zOrder, or any
     * other number.
     * 
     * @param zOrder
     * @param listener
     */
    @Override
    public void addCellPaintListener(int zOrder, ImageListViewCellPaintListener listener) {
        cellPaintListeners.add(new ListenerRecord<ImageListViewCellPaintListener>(listener, zOrder));
    }
    
    @Override
    public void removeCellPaintListener(ImageListViewCellPaintListener listener) {
        for (Iterator<ListenerRecord<ImageListViewCellPaintListener>> it = cellPaintListeners.iterator(); it.hasNext();) {
            if (it.next().listener == listener) {
                it.remove();
                return;
            }
        }
    }
    
    /**
     * NOT A PUBLIC API! DON'T CALL.
     * 
     * (method is defined public so internal classes in subpackages can call it)
     * @param e
     */
    public void fireCellPaintEvent(ImageListViewCellPaintEvent e) {
        for (ListenerRecord<ImageListViewCellPaintListener> rec : cellPaintListeners) {
            rec.listener.onCellPaint(e);
            if (e.isConsumed()) {
                break;
            }
        }
    }

    protected void forEachCellPaintListenerInZOrder(Runnable1<ImageListViewCellPaintListener> callback) {
        for (ListenerRecord<ImageListViewCellPaintListener> rec : cellPaintListeners) {
            callback.run(rec.listener);
        }
    }
    
    /**
     * NOT A PUBLIC API! DON'T CALL.
     * 
     * (method is defined public so internal classes in subpackages can call it)
     * @param e
     */
    public void fireCellPaintEvent(ImageListViewCellPaintEvent e, int minZ, int maxZ) {
        ImageListViewCellPaintListener dummy = new ImageListViewCellPaintListener() {
            @Override
            public void onCellPaint(ImageListViewCellPaintEvent e) {
            }
        };
        ListenerRecord<ImageListViewCellPaintListener> min = new ListenerRecord<ImageListViewCellPaintListener>(dummy, minZ);
        ListenerRecord<ImageListViewCellPaintListener> max = new ListenerRecord<ImageListViewCellPaintListener>(dummy, maxZ);
        for (ListenerRecord<ImageListViewCellPaintListener> rec : cellPaintListeners.subSet(min, max)) {
            rec.listener.onCellPaint(e);
            if (e.isConsumed()) {
                break;
            }
        }
    }

    private NavigableSet<ListenerRecord<ImageListViewCellPaintListener>> cellPaintListeners =
        new TreeSet<ListenerRecord<ImageListViewCellPaintListener>>();
    
    private static class ListenerRecord<ListenerType> implements Comparable<ListenerRecord<ListenerType>> {
        ListenerType listener;
        Integer zOrder;
        Integer instanceNumber;
        private static int lastInstanceNumber;
        public ListenerRecord(ListenerType listener, int zOrder) {
            this.listener = listener;
            this.zOrder = zOrder;
            this.instanceNumber = lastInstanceNumber++;
        }
        @Override
        public int compareTo(ListenerRecord<ListenerType> o) {
            int res = zOrder.compareTo(o.zOrder);
            if (res == 0) {
                return instanceNumber.compareTo(o.instanceNumber);
            } else {
                return res;
            }
        }
    }

    public int getCellBorderWidth() {
        return 2;
    }

    /**
     * Gives the current size of a cell in this list, including the border (
     * {@link #getCellBorderWidth()}). In this base class, this method and
     * {@link #getCurrentCellDisplayAreaSize(ImageListViewCell)} are implemented in
     * terms of each other, so subclasses MUST override one of them or an
     * infinite recursion will occur.
     * 
     * @param cell
     * @return
     */
    @Override
    public Dimension getCurrentCellSize(ImageListViewCell cell) {
        Dimension result = getCurrentCellDisplayAreaSize(cell);
        result.width += 2 * getCellBorderWidth();
        result.height += 2 * getCellBorderWidth();
        return result;
    }

    /**
     * Gives the current size of the display area of a cell in this list, i.e.
     * the {@link #getCurrentCellSize(ImageListViewCell)} minus the
     * {@link #getCellBorderWidth()}. In this base class, this method and
     * {@link #getCurrentCellSize(ImageListViewCell)} are implemented in terms
     * of each other, so subclasses MUST override one of them or an infinite
     * recursion will occur.
     * 
     * @param cell
     * @return
     */
    @Override
    public Dimension getCurrentCellDisplayAreaSize(ImageListViewCell cell) {
        Dimension result = getCurrentCellSize(cell);
        result.width -= 2 * getCellBorderWidth();
        result.height -= 2 * getCellBorderWidth();
        return result;
    }

    /**
     * Gives the current size that a cell in this list would preferably attain
     * (i.e. image scaled to 1.0 etc.), including the border (
     * {@link #getCellBorderWidth()}). In this base class, this method and
     * {@link #getUnscaledPreferredCellDisplayAreaSize(ImageListViewCell)} are
     * implemented in terms of each other, so subclasses MUST override one of
     * them or an infinite recursion will occur.
     * 
     * @param cell
     * @return
     */
    @Override
    public Dimension getUnscaledPreferredCellSize(ImageListViewCell cell) {
        Dimension result = getUnscaledPreferredCellDisplayAreaSize(cell);
        result.width += 2 * getCellBorderWidth();
        result.height += 2 * getCellBorderWidth();
        return result;
    }

    /**
     * Gives the current size that a cell's display area would preferably attain
     * (i.e. image scaled to 1.0 etc.), excluding the border (that is, the
     * {@link #getUnscaledPreferredCellSize(ImageListViewCell)} minus the
     * {@link #getCellBorderWidth()}). In this base class, this method and
     * {@link #getUnscaledPreferredCellSize(ImageListViewCell)} are implemented
     * in terms of each other, so subclasses MUST override one of them or an
     * infinite recursion will occur.
     * 
     * @param cell
     * @return
     */
    @Override
    public Dimension getUnscaledPreferredCellDisplayAreaSize(ImageListViewCell cell) {
        Dimension result = getUnscaledPreferredCellSize(cell);
        result.width -= 2 * getCellBorderWidth();
        result.height -= 2 * getCellBorderWidth();
        return result;
    }
    
    @Override
    public ImageListViewBackend getBackend() {
        return backend;
    }

}
