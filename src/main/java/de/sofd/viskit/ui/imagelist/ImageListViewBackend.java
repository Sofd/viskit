package de.sofd.viskit.ui.imagelist;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;

import javax.media.opengl.GLAutoDrawable;

import de.sofd.draw2d.viewer.DrawingViewer;
import de.sofd.draw2d.viewer.gc.GC;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintEvent;

/**
 * Interface the defines technology-specific functionality needed by
 * {@link ImageListView} implementations. An {@link ImageListView} instance has
 * exactly one such backend, which is can't change over time.
 * 
 * Will be implemented by each technology backend (e.g. J2D, JOGL).
 * 
 * TODO: Operations are very coarse-grained and non-reusable atm. because theyŕe
 * tailored to the needs of specific controllers. Try to refactor things so the
 * operations resemble a more general drawing API some more.
 * 
 * @author olaf
 */
public interface ImageListViewBackend {

    /**
     * Called once, at the start of the backend's lifetime, with the list the
     * backend will belong to.
     * 
     * @param owner
     */
    void initialize(ImageListView owner);
    
    /**
     * Method that will be called every time a GL context is being set up whose
     * data (e.g., textures and display lists) may later be shared with new GL
     * contexts being created. sharedData is a place where the listener can put
     * arbitrary data (e.g., IDs of created textures or display lists) that it
     * needs for painting. The sharedData will be contained in all
     * cell paint events sent out for the context in question (in
     * {@link ImageListViewCellPaintEvent#getSharedContextData()}).
     * <p>
     * There will be one such sharedData per set of data-sharing GL contexts
     * (most of the time, this amounts to just one per VM), and all listeners
     * will receive that sharedData and be able to write to it. Thus, the
     * listeners must ensure that they're not overwriting each other's values in
     * the map. Thus, it is advised to use unique strings for the keys in the
     * map, e.g. dotted names similar to Java class names.
     * <p>
     * Just like {@link #glDrawableInitialized(GLAutoDrawable)}, this method
     * will be called at least once per listener.
     * <p>
     * For Java2D painting, this method won't ever be called.
     * <p>
     * TODO: do we really need the sharedData?
     * 
     * @param gl the GL context. type depends on backend.
     * @param sharedData
     */
    void glSharedContextDataInitialization(Object gl, Map<String, Object> sharedData);

    /**
     * Create a DrawingViewer for the given cell that's compatible with the backend.
     * 
     * @param elt
     * @param cell
     * @return
     */
    DrawingViewer createRoiDrawingViewer(ImageListViewModelElement elt, ImageListViewCell cell);


    ////TODO: try to modularize these methods more (e.g. drawing lines, texts etc.)
    
    void paintCellImage(ImageListViewCellPaintEvent e);

    void paintCellInitStateIndication(GC gc, String text, int textX, int textY, Color textColor);

    void printLUTIntoCell(ImageListViewCellPaintEvent e, int lutWidth, int lutHeight, int intervals, Point2D lutPosition, Point2D textPosition, Color textColor, List<String> scaleList);

    /**
     * TODO unify with {@link #paintCellInitStateIndication(GC, String, int, int, Color)}
     * @param e
     * @param text
     * @param textPosition
     * @param textColor
     */
    void printTextIntoCell(ImageListViewCellPaintEvent e, String[] text, int textX, int textY, Color textColor);

    void paintCellROIs(ImageListViewCellPaintEvent e);

    /**
     * TODO: split line drawing + text printing, unify the latter with {@link #printTextIntoCell(ImageListViewCellPaintEvent, String[], int, int, Color)}
     * @param e
     */
    void paintMeasurementIntoCell(ImageListViewCellPaintEvent e, Point2D p1, Point2D p2, String text, Color textColor);
}
