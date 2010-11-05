package de.sofd.viskit.draw2d.vieweradapters;

import de.sofd.draw2d.DrawingObject;
import de.sofd.draw2d.viewer.DrawingViewer;
import de.sofd.draw2d.viewer.adapters.DrawingObjectViewerAdapter;
import de.sofd.draw2d.viewer.adapters.MouseHandle;
import de.sofd.draw2d.viewer.gc.GC;
import de.sofd.viskit.draw2d.gc.ViskitGC;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.lwjgl.NondirectBufferWrapper;
import org.lwjgl.opengl.GL11;

/**
 * Base class for draw2d DrawingObjectViewerAdapters that can draw onto
 * {@link ViskitGC}s, using Java2D or OpenGL (LWJGL,JGL) depending on the capabilities
 * of the passed-in ViskitGC.
 * <p>
 * Java2D drawing will be delegated to the superclass
 *
 * @author olaf
 */
public class ViskitDrawingObjectViewerAdapter extends DrawingObjectViewerAdapter {

    public ViskitDrawingObjectViewerAdapter(DrawingViewer viewer, DrawingObject drawingObject) {
        super(viewer, drawingObject);
    }

    // local variables for glPushObjToDisplayTransform,
    // defined here to relieve the GC
    private final double[] comps = new double[6];
    private final float[] glcomps = new float[16];

    /**
     * used in OpenGL JOGL drawing context
     * @param gl
     */
    protected void glPushObjToDisplayTransform(GL gl) {
        GL2 gl2 = gl.getGL2();
        // TODO: efficiency...
        AffineTransform t = getViewer().getObjectToDisplayTransform();
        // comps = { m00 m10 m01 m11 m02 m12 }
        t.getMatrix(comps);
        glcomps[0] = (float) comps[0];
        glcomps[1] = (float) comps[1];
        glcomps[2] = 0;
        glcomps[3] = 0;
        glcomps[4] = (float) comps[2];
        glcomps[5] = (float) comps[3];
        glcomps[6] = 0;
        glcomps[7] = 0;
        glcomps[8] = (float) comps[4];
        glcomps[9] = (float) comps[5];
        glcomps[10] = 0;
        glcomps[11] = 0;
        glcomps[12] = 0;
        glcomps[13] = 0;
        glcomps[14] = 0;
        glcomps[15] = 1;

        gl2.glPushMatrix();
        gl2.glMultMatrixf(glcomps, 0);
    }

    /**
     * used in OpenGL JOGL drawing context
     * @param gl
     */
    protected void glPopTransform(GL gl) {
        gl.getGL2().glPopMatrix();
    }
    
    /**
     * used in OpenGL LWJGL drawing context
     */
    protected void glPushObjToDisplayTransform() {
        // TODO: efficiency...
        AffineTransform t = getViewer().getObjectToDisplayTransform();
        // comps = { m00 m10 m01 m11 m02 m12 }
        t.getMatrix(comps);
        glcomps[0] = (float) comps[0];
        glcomps[1] = (float) comps[1];
        glcomps[2] = 0;
        glcomps[3] = 0;
        glcomps[4] = (float) comps[2];
        glcomps[5] = (float) comps[3];
        glcomps[6] = 0;
        glcomps[7] = 0;
        glcomps[8] = (float) comps[4];
        glcomps[9] = (float) comps[5];
        glcomps[10] = 0;
        glcomps[11] = 0;
        glcomps[12] = 0;
        glcomps[13] = 0;
        glcomps[14] = 0;
        glcomps[15] = 1;

        GL11.glPushMatrix();
        GL11.glMultMatrix(NondirectBufferWrapper.wrapDirect(FloatBuffer.wrap(glcomps)));
    }

    /**
     * used in OpenGL LWJGL drawing context
     */
    protected void glPopTransform() {
        GL11.glPopMatrix();
    }

    @Override
    public void paintObjectOn(GC gc) {
        ViskitGC vkgc = (ViskitGC) gc;
        if (vkgc.isGraphics2DAvailable() && ! vkgc.isGlPreferred()) {
            super.paintObjectOn(gc);
        } else {

        }
    }

    @Override
    public void paintSelectionVisualizationOn(GC gc, boolean isSelected) {
        ViskitGC vkgc = (ViskitGC) gc;
        if (vkgc.isGraphics2DAvailable() && ! vkgc.isGlPreferred()) {
            // need to draw using Java2D -> delegate to superclass
            super.paintSelectionVisualizationOn(gc, isSelected);
        } else {
            // need to draw using OpenGL
            if (isSelected) {
                if(vkgc.isLWJGLPreferred()) {
                    GL11.glPushAttrib(GL11.GL_CURRENT_BIT | GL11.GL_ENABLE_BIT);
                    try {
                        GL11.glShadeModel(GL11.GL_FLAT);
                        GL11.glColor3f(0, 1, 0); // green

                        // by default, draw all the object's handles
                        int count = getHandleCount();
                        for (int i = 0; i < count; ++i) {
                            MouseHandle handle = getHandle(i);
                            if (null != handle) { // should always be the
                                                    // case...
                                // TODO: maybe draw a GL point instead of a
                                // small quad?
                                GL11.glBegin(GL11.GL_QUADS);
                                Point2D posn = getViewer().objToDisplay(handle.getPosition());
                                GL11.glVertex2d(posn.getX() - HANDLE_BOX_WIDTH / 2, posn.getY() - HANDLE_BOX_WIDTH / 2);
                                GL11.glVertex2d(posn.getX() - HANDLE_BOX_WIDTH / 2, posn.getY() + HANDLE_BOX_WIDTH / 2);
                                GL11.glVertex2d(posn.getX() + HANDLE_BOX_WIDTH / 2, posn.getY() + HANDLE_BOX_WIDTH / 2);
                                GL11.glVertex2d(posn.getX() + HANDLE_BOX_WIDTH / 2, posn.getY() - HANDLE_BOX_WIDTH / 2);
                                GL11.glEnd();
                            }
                        }
                        // ... and a dashed rectangle around the object
                        glPushObjToDisplayTransform();
                        GL11.glLineStipple(3, (short) 0xaaaa);
                        GL11.glEnable(GL11.GL_LINE_STIPPLE);
                        GL11.glBegin(GL11.GL_LINE_LOOP);
                        Rectangle2D bounds = getDrawingObject().getBounds2D();
                        GL11.glVertex2d(bounds.getMinX(), bounds.getMinY());
                        GL11.glVertex2d(bounds.getMinX(), bounds.getMaxY());
                        GL11.glVertex2d(bounds.getMaxX(), bounds.getMaxY());
                        GL11.glVertex2d(bounds.getMaxX(), bounds.getMinY());
                        GL11.glEnd();
                        glPopTransform();
                    } finally {
                        GL11.glPopAttrib();
                    }
                } else {
                    GL2 gl = vkgc.getGl().getGL2();
                    // TODO: for better performance, draw transformed unit
                    // squares/quads from vertex array or VBO
                    gl.glPushAttrib(GL2.GL_CURRENT_BIT | GL2.GL_ENABLE_BIT);
                    try {
                        gl.glShadeModel(GL2.GL_FLAT);
                        gl.glColor3f(0, 1, 0); // green

                        // by default, draw all the object's handles
                        int count = getHandleCount();
                        for (int i = 0; i < count; ++i) {
                            MouseHandle handle = getHandle(i);
                            if (null != handle) { // should always be the
                                                    // case...
                                // TODO: maybe draw a GL point instead of a
                                // small quad?
                                gl.glBegin(GL2.GL_QUADS);
                                Point2D posn = getViewer().objToDisplay(handle.getPosition());
                                gl.glVertex2d(posn.getX() - HANDLE_BOX_WIDTH / 2, posn.getY() - HANDLE_BOX_WIDTH / 2);
                                gl.glVertex2d(posn.getX() - HANDLE_BOX_WIDTH / 2, posn.getY() + HANDLE_BOX_WIDTH / 2);
                                gl.glVertex2d(posn.getX() + HANDLE_BOX_WIDTH / 2, posn.getY() + HANDLE_BOX_WIDTH / 2);
                                gl.glVertex2d(posn.getX() + HANDLE_BOX_WIDTH / 2, posn.getY() - HANDLE_BOX_WIDTH / 2);
                                gl.glEnd();
                            }
                        }
                        // ... and a dashed rectangle around the object
                        glPushObjToDisplayTransform(gl);
                        gl.glLineStipple(3, (short) 0xaaaa);
                        gl.glEnable(GL2.GL_LINE_STIPPLE);
                        gl.glBegin(GL.GL_LINE_LOOP);
                        Rectangle2D bounds = getDrawingObject().getBounds2D();
                        gl.glVertex2d(bounds.getMinX(), bounds.getMinY());
                        gl.glVertex2d(bounds.getMinX(), bounds.getMaxY());
                        gl.glVertex2d(bounds.getMaxX(), bounds.getMaxY());
                        gl.glVertex2d(bounds.getMaxX(), bounds.getMinY());
                        gl.glEnd();
                        glPopTransform(gl);
                    } finally {
                        gl.glPopAttrib();
                    }
                }
            }
        }
    }

}
