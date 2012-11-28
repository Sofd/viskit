package de.sofd.viskit.ui.imagelist.event.cellpaint;

import javax.media.opengl.GLAutoDrawable;

import de.sofd.viskit.ui.imagelist.ImageListView;

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

    // TODO: void glSharedContextDataDisposing(GL gl, Map<String, Object> sharedData);
    
    // TODO: maybe refactor glDrawableIntialized/-Disposed to also be called
    // during J2D graphics initialization/disposal? Maybe people want to do things
    // there too.
    // TODO: or, maybe move the gl* methods to a new subinterface
    
}
