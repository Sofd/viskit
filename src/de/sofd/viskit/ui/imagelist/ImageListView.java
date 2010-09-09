package de.sofd.viskit.ui.imagelist;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.JImageListView.ScaleMode;
import de.sofd.viskit.ui.imagelist.event.ImageListViewListener;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintListener;

/**
 * Base interface for GUI components displaying a list of elements, which are
 * objects implementing {@link ImageListViewModelElement}.
 * <p>
 * The elements must be fed to the ImageListView via a {@link ListModel} that
 * contains them.
 * <p>
 * A selection of elements is maintained via a {@link ListSelectionModel}.
 * <p>
 * The list automatically maintains a list of "cell" objects, implementing
 * {@link ImageListViewCell}, that will always be associated 1:1 to the current
 * elements (i.e. each element is associated to exactly one cell, and vice
 * versa). A cell is used to hold data that should be associated with a model
 * element in this list, but not elsewhere, i.e. if a model element is displayed
 * simultaneously in more than one JImageListView, each one has separate cell
 * data associated with the element.
 * <p>
 * TODO: Separate sub-interface for lists that display one continuous range of
 * elements (all lists we have now do that, but maybe not all the ones we're
 * going to have in the future will). Move related methods
 * (*First|LastVisibleIndex* etc.) to it.
 * 
 * @author olaf
 */
public interface ImageListView {

    public static final String PROP_MODEL = "model";
    public static final String PROP_SELECTIONMODEL = "selectionModel";
    public static final String PROP_FIRSTVISIBLEINDEX = "firstVisibleIndex";
    public static final String PROP_LOWERVISIBILITYLIMIT = "lowerVisibilityLimit";
    public static final String PROP_UPPERVISIBILITYLIMIT = "upperVisibilityLimit";
    public static final String PROP_SCALEMODE = "scaleMode";

    public boolean isUiInitialized();

    /**
     * Get the value of model
     *
     * @return the value of model
     */
    public ListModel getModel();

    /**
     * Set the value of model
     *
     * @param model new value of model
     */
    public void setModel(ListModel model);

    public int getLength();

    public ImageListViewModelElement getElementAt(int index);

    public ImageListViewCell getCellForElement(
            ImageListViewModelElement elt);

    public ImageListViewModelElement getElementForCell(
            ImageListViewCell cell);

    public ImageListViewCell getCell(int index);

    public String getDisplayName();

    public void setDisplayName(String displayName);

    /**
     * Get the value of scaleMode
     *
     * @return the value of scaleMode
     */
    public ScaleMode getScaleMode();

    /**
     * Set the scaleMode ({@link #getScaleMode()}) of this viewer to one of the {@link #getSupportedScaleModes() }
     * (calling this with an unsupported scale mode will raise an exception). Sets the bean property,
     * fires the corresponding PropertyChangeEvent, and (before firing, but after setting the property) calls
     * {@link #doSetScaleMode(de.sofd.viskit.ui.imagelist.JImageListView.ScaleMode, de.sofd.viskit.ui.imagelist.JImageListView.ScaleMode) },
     * which subclasses should override normally (rather than overriding this method).
     *
     * @param scaleMode new value of scaleMode
     */
    public void setScaleMode(ScaleMode scaleMode);

    /**
     *
     * @return list off ScaleModes supported by this JImageListView implementation. Subclasses must implement.
     */
    public Collection<ScaleMode> getSupportedScaleModes();

    public ImageListViewCell getCellFor(
            ImageListViewModelElement modelElement);

    /**
     * Get the value of selectionModel
     *
     * @return the value of selectionModel
     */
    public ListSelectionModel getSelectionModel();

    /**
     * Set the value of selectionModel
     *
     * @param selectionModel new value of selectionModel
     */
    public void setSelectionModel(ListSelectionModel selectionModel);

    public int getSelectedIndex();

    public ImageListViewModelElement getSelectedValue();

    public int getMinSelectionIndex();

    public int getMaxSelectionIndex();

    public int getLeadSelectionIndex();

    /**
     * Adds a listener that gets notified if this list's selection changes.
     * The source of the selection change events will be this list (rather
     * than the selection model, as would be the case for listeners registered
     * via getSelectionModel().addListSelectionListener())
     * 
     * @param listener
     */
    public void addListSelectionListener(ListSelectionListener listener);

    public void removeListSelectionListener(
            ListSelectionListener listener);

    /**
     * Find the index of a cell in this list. Reverse operation to
     * {@link #getCell(int)}.
     * <p>
     * This operation will generally be efficient (O(1)) if the cell is present
     * in the list.
     * 
     * @param cell
     *            a cell of this list
     * @return index of cell in this list, or -1 if the cell isn't part of the
     *         list
     */
    public int getIndexOf(ImageListViewCell cell);

    /**
     *
     * @return start of currently displayed interval of model elements
     */
    public int getFirstVisibleIndex();

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
    public void setFirstVisibleIndex(int newValue);

    public int getLastVisibleIndex();

    public boolean isVisibleIndex(int i);

    public void ensureIndexIsVisible(int idx);

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
    public Integer getLowerVisibilityLimit();

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
    public void setLowerVisibilityLimit(Integer newValue);

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
    public Integer getUpperVisibilityLimit();

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
    public void setUpperVisibilityLimit(Integer newValue);

    public void disableVisibilityLimits();

    public void selectSomeVisibleCell();

    public void scrollToSelection();

    /**
     * Called if a the cells of this viewer need to be refreshed. Normally
     * called internally by subclasses if they determine the need to refresh the cells,
     * but may also be called explicitly from the outside.
     * <p>
     * Uses the same special casing hack as {@link #refreshCellForIndex(int) }.
     */
    public void refreshCells();

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
    public void refreshCellForIndex(int idx);

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
    public void refreshCellForElement(ImageListViewModelElement elt);

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
    public void refreshCell(ImageListViewCell cell);

    public void setForeground(Color fg);

    public void setBackground(Color bg);

    public void addImageListViewListener(ImageListViewListener listener);

    public void removeImageListViewListener(
            ImageListViewListener listener);

    public void addPropertyChangeListener(PropertyChangeListener listener);
    
    public void removePropertyChangeListener(PropertyChangeListener listener);

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);
    
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
    public void addCellPropertyChangeListener(
            PropertyChangeListener listener);

    public void removeCellPropertyChangeListener(
            PropertyChangeListener listener);

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
    public void addModelElementPropertyChangeListener(
            PropertyChangeListener listener);

    public void removeModelElementPropertyChangeListener(
            PropertyChangeListener listener);

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
    public void addCellMouseListener(int zOrder, MouseListener listener);

    public void addCellMouseListener(MouseListener listener);

    public void removeCellMouseListener(MouseListener listener);

    /**
     * Like {@link #addCellMouseListener(int, java.awt.event.MouseListener) }, but for
     * MouseMotionListeners.
     *
     * @param zOrder
     * @param listener
     */
    public void addCellMouseMotionListener(int zOrder,
            MouseMotionListener listener);

    public void addCellMouseMotionListener(MouseMotionListener listener);

    public void removeCellMouseMotionListener(
            MouseMotionListener listener);

    /**
     * Like {@link #addCellMouseListener(int, java.awt.event.MouseListener) }, but for
     * MouseWheelListener.
     *
     * @param zOrder
     * @param listener
     */
    public void addCellMouseWheelListener(int zOrder,
            MouseWheelListener listener);

    public void addCellMouseWheelListener(MouseWheelListener listener);

    public void removeCellMouseWheelListener(
            MouseWheelListener listener);

    /**
     * Add a new listener for CellPaintEvents, with default z-order
     * (PAINT_ZORDER_DEFAULT).
     * 
     * @param listener
     */
    public void addCellPaintListener(
            ImageListViewCellPaintListener listener);

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
    public void addCellPaintListener(int zOrder,
            ImageListViewCellPaintListener listener);

    public void removeCellPaintListener(
            ImageListViewCellPaintListener listener);

    /**
     * Paint z-order at which the image is normally drawn. Numerical value is 10, which
     * is the lowest of all the PAINT_ZORDER_* constants, so the image will normally be drawn
     * at the bottom of anything else (i.e., everything else will be drawn on top).
     */
    public static final int PAINT_ZORDER_IMAGE = 10;
    /**
     * Paint z-order at which the ROIs are normally drawn. Numerical value is
     * 50, which means the ROIs are drawn on top of the image, but below any
     * labels (see PAINT_ZORDER_LABELS)
     * 
     * (TODO: not implemented)
     */
    public static final int PAINT_ZORDER_ROI = 50;
    /**
     * Paint z-order at which any labels are normally drawn. Numerical value is
     * 200, which is the highest of all the PAINT_ZORDER_* constants, so this
     * will be drawn on top of everything else unless you specify a z-order
     * that's not one of the PAINT_ZORDER_* constants, and is higher than
     * PAINT_ZORDER_LABELS.
     * 
     * (TODO: not implemented)
     */
    public static final int PAINT_ZORDER_LABELS = 200;
    /**
     * Default paint z-order. This is used if you call
     * {@link #addCellPaintListener(ImageListViewCellPaintListener)} (i.e.
     * without explicitly specifying a z-order). Numerical value is 100,
     * meaning that this would normally be drawn between the ROIs and any
     * labels.
     */
    public static final int PAINT_ZORDER_DEFAULT = 100;

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
    public Dimension getCurrentCellSize(ImageListViewCell cell);

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
    public Dimension getCurrentCellDisplayAreaSize(
            ImageListViewCell cell);

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
    public Dimension getUnscaledPreferredCellSize(
            ImageListViewCell cell);

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
    public Dimension getUnscaledPreferredCellDisplayAreaSize(
            ImageListViewCell cell);

}