package de.sofd.viskit.ui.imagelist.event.cellpaint;

import java.util.Map;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import de.sofd.viskit.ui.imagelist.ImageListView;
import de.sofd.viskit.ui.imagelist.glimpl.JGLImageListView;
import de.sofd.viskit.ui.imagelist.gridlistimpl.JGridImageListView;

/**
 * Listener for receiving {@link ImageListViewCellPaintEvent}s from a
 * {@link ImageListView}.
 * 
 * @author olaf
 */
public interface ImageListViewCellPaintListener {
    /**
     * Invoked every time a cell is being painted. The listener must perform its
     * painting tasks here.
     * 
     * @param e
     *            event describing the paint request. The
     *            {@link ImageListViewCellPaintEvent#getGc() ViskitGC} of the
     *            paint event tells whether the painting is being done on an
     *            OpenGL or Java2D graphics context
     */
    void onCellPaint(ImageListViewCellPaintEvent e);

    /**
     * An OpenGL/JOGL drawable is being initialized. Happens with any lists that
     * use JOGL somewhere in their painting pipeline. Whether or not (and if so,
     * how many) GL drawables are created by a list depends on the list (e.g.,
     * {@link JGLImageListView} uses one GL drawable for the whole list view,
     * while {@link JGridImageListView} with renderer type OPENGL uses one GL
     * drawable for each visible cell.
     * <p>
     * The only guarantee is that any GL drawable that will ever be passed in a
     * cell paint event to {@link #onCellPaint(ImageListViewCellPaintEvent)}
     * will first be passed through this method.
     * <p>
     * This method will be called at least once per listener. I.e., if you
     * register a listener with a list that is already being displayed (and
     * thus, has already initialized its GL canvasses), this method will still
     * be called once, right after listener registration.
     * <p>
     * For Java2D painting, this method won't ever be called.
     * 
     * @param glAutoDrawable
     */
    void glDrawableInitialized(GLAutoDrawable glAutoDrawable);
    

    /**
     * Method that will be called every time a GL context is being set up whose
     * data (e.g., textures and display lists) may later be shared with new GL
     * contexts being created. sharedData is a place where the listener can put
     * arbitrary data (e.g., IDs of created textures or display lists) that it
     * needs for painting. The sharedData will be passed to
     * {@link #onCellPaint(ImageListViewCellPaintEvent)} again (in
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
     * 
     * @param gl
     * @param sharedData
     */
    void glSharedContextDataInitialization(GL gl, Map<String, Object> sharedData);

    /**
     * An OpenGL/JOGL drawable that has previously been initialized (and for
     * which {@link #glDrawableInitialized(GLAutoDrawable)} was called then) is
     * being disposed.
     * <p>
     * For Java2D painting, this method won't ever be called.
     * 
     * @param glAutoDrawable
     */
    void glDrawableDisposing(GLAutoDrawable glAutoDrawable);

    // TODO: void glSharedContextDataDisposing(GL gl, Map<String, Object> sharedData);
    
    // TODO: maybe refactor glDrawableIntialized/-Disposed to also be called
    // during J2D graphics initialization/disposal? Maybe people want to do things
    // there too.
    // TODO: or, maybe move the gl* methods to a new subinterface
    
}
