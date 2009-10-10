package de.sofd.viskit.ui.imagelist;

import de.sofd.draw2d.viewer.DrawingViewer;
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

    /**
     * Set the value of centerOffset
     *
     * @param centerOffset new value of centerOffset
     */
    void setCenterOffset(Point2D centerOffset);

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
