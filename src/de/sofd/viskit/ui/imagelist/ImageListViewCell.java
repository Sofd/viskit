package de.sofd.viskit.ui.imagelist;

import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.model.LookupTable;
import de.sofd.draw2d.viewer.DrawingViewer;
import de.sofd.viskit.controllers.ImageListViewMouseWindowingController;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;

/**
 * Represents a single element "cell" in the UI of a {@link JImageListView},
 * i.e. a part of the UI that visually displays a single element of
 * the lists's ListModel.
 * <p>
 * References its JImageListView and the model element it displays. Holds
 * additional data that's associated with the model element in this list, i.e.
 * is not part of the model element itself ("cell data"). Atm. this would be
 * the windowing parameters, the zoom/pan settings, and the {@link DrawingViewer} for the
 * element's ROI drawing. The ImageListViewCell is "live", i.e. interactive
 * UI changes to the cell will be reflected in the corresponding ImageListViewCell
 * properties immediately, and programmatically
 * manipulating the cell data will be reflected in the UI immediately.
 * <p>
 * Created by the JImageListView; associated 1:1 with
 * model elements.
 *
 * @author olaf
 */
public interface ImageListViewCell {
    public static final String PROP_WINDOWLOCATION = "windowLocation";
    public static final String PROP_CENTEROFFSET = "centerOffset";
    public static final String PROP_INTERACTIVEWINDOWINGINPROGRESS = "interactiveWindowingInProgress";
    public static final String PROP_SCALE = "scale";
    public static final String PROP_WINDOWWIDTH = "windowWidth";
    public static final String PROP_LOOKUPTABLE = "lookupTable";

    JImageListView getOwner();

    ImageListViewModelElement getDisplayedModelElement();

    /**
     * Get the value of windowLocation
     *
     * @return the value of windowLocation
     */
    int getWindowLocation();

    /**
     * Set the value of windowLocation
     *
     * @param windowLocation new value of windowLocation
     */
    void setWindowLocation(int windowLocation);

    /**
     * Get the value of centerOffset
     *
     * @return the value of centerOffset
     */
    Point2D getCenterOffset();

    /**
     * Get the value of roiDrawingViewer
     *
     * @return the value of roiDrawingViewer
     */
    DrawingViewer getRoiDrawingViewer();

    /**
     * Get the value of scale
     *
     * @return the value of scale
     */
    double getScale();

    /**
     * 
     * @return latest displayed size of this cell, as set via {@link #setLatestSize(java.awt.Dimension) }
     */
    Dimension getLatestSize();

    /**
     * Get the value of windowWidth
     *
     * @return the value of windowWidth
     */
    int getWindowWidth();

    /**
     * Get the value of interactiveWindowingInProgress
     *
     * @return the value of interactiveWindowingInProgress
     */
    boolean isInteractiveWindowingInProgress();

    LookupTable getLookupTable();
    
    /**
     * Set the value of centerOffset
     *
     * @param centerOffset new value of centerOffset
     */
    void setCenterOffset(Point2D centerOffset);

    /**
     * Set the value of centerOffset
     *
     * @param x
     * @param y
     */
    public void setCenterOffset(double x, double y);

    /**
     * Set the value of scale
     *
     * @param scale new value of scale
     */
    void setScale(double scale);

    /**
     * Store the latest size that this cell was drawn with. A cell renderer
     * component that draws this cell may store the size here, and then, later,
     * the list may retrieve the size (using {@link #getLatestSize() }) when
     * it needs to transform the x/y coordinates of a mouse event to image coordinates.
     *
     * @param size
     */
    void setLatestSize(Dimension size);

    /**
     * Set the value of windowWidth
     *
     * @param windowWidth new value of windowWidth
     */
    void setWindowWidth(int windowWidth);

    void setLookupTable(LookupTable lut);
    
    /**
     * The properties of this cell
     * that are currently being changed as a direct result of an end user interaction,
     * e.g. with the mouse or keyboard. For example, if the end user changes the
     * {@link #setScale(scale) scale} of the cell using the mouse, PROP_SCALE will be included
     * in this property. If some piece of code (e.g. a controller like
     * {@link GenericILVCellPropertySyncController}) copies this change over
     * into another cell, the resulting property change in that cell will
     * *not* lead to PROP_SCALE being included in that cell's
     * interactivelyChangingProps, because the change was caused by
     * a piece of code rather than the end user directly.
     * <p>
     * This allows interested parties to react differently to property
     * changes depending on whether they occur directly by user interaction
     * or by some program. Almost all controllers that somehow synchronize
     * property changes between cells (e.g. the mentioned
     * {@link GenericILVCellPropertySyncController}) will normally do that
     * only if the initial change was the result of a user interaction. This
     * is normally the desired behaviour, as it prevents inadvertend copying
     * of automatic or internal property changes.
     * <p>
     * This property can't be managed automatically. External parties
     * (e.g. controllers like {@link ImageListViewMouseWindowingController})
     * must decide themselves which property changes are "interactive"
     * and which are not, and include the respective property in this
     * property if necessary. Convenience methods like
     * {@link #setInteractively(java.lang.String, java.lang.Object) } or
     * {@link #runWithPropChangingInteractively(java.lang.String, java.lang.Runnable) }
     * make this easier.
     *
     * @return the value of interactivelyChangingProps
     */
    public String[] getInteractivelyChangingProps();

    /**
     * Convenience query method that tells whether propName is
     * currently included in {@link #getInteractivelyChangingProps() }.
     *
     * @param propName
     * @return
     */
    public boolean isInteractivelyChangingProp(String propName);

    /**
     * Run the runnable with propName included in {@link #getInteractivelyChangingProps() }
     * as long as the runnable runs, and being removed afterwards.
     *
     * @param propName
     * @param runnable
     */
    public void runWithPropChangingInteractively(String propName, Runnable runnable);

    /**
     * Set the property named propName to value, with the "interactively changing"
     * flag (see {@link #getInteractivelyChangingProps() }) of the property activated.
     * <p>
     * For example, cell.setInteractively("windowWidth", 100)
     * will have the same effect as cell.setWindowWidth(100), except
     * that "windowWidth" will be included in {@link #getInteractivelyChangingProps() }
     * as long as the setInteractively method runs.
     *
     * @param propName
     * @param value
     */
    public void setInteractively(String propName, Object value);

    void refresh();

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

}
