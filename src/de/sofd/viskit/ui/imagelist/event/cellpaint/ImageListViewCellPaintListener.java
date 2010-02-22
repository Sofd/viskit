package de.sofd.viskit.ui.imagelist.event.cellpaint;

import javax.media.opengl.GLAutoDrawable;

import de.sofd.viskit.ui.imagelist.JImageListView;
import de.sofd.viskit.ui.imagelist.glimpl.JGLImageListView;
import de.sofd.viskit.ui.imagelist.gridlistimpl.JGridImageListView;

/**
 * Listener for receiving {@link ImageListViewCellPaintEvent}s from a
 * {@link JImageListView}.
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
     * For Java2D painting, this method won't ever be called.
     * 
     * @param glAutoDrawable
     */
    void glDrawableInitialized(GLAutoDrawable glAutoDrawable);

    /**
     * An OpenGL/JOGL drawable that has previously been initialized (and for
     * which {@link #glDrawableInitialized(GLAutoDrawable)} was called then) is
     * being disposed.
     * 
     * @param glAutoDrawable
     */
    void glDrawableDisposing(GLAutoDrawable glAutoDrawable);

    // TODO: maybe refactor glDrawableIntialized/-Disposed to also be called
    // during J2D graphics initialization/disposal? Maybe people want to do things
    // there too.
    
}
