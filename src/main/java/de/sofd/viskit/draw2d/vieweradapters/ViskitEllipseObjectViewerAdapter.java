package de.sofd.viskit.draw2d.vieweradapters;

import de.sofd.draw2d.EllipseObject;
import de.sofd.draw2d.viewer.DrawingViewer;
import de.sofd.draw2d.viewer.adapters.EllipseObjectViewerAdapter;
import de.sofd.draw2d.viewer.gc.GC;
import de.sofd.viskit.draw2d.gc.ViskitGC;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.lwjgl.opengl.GL11;

/**
 *
 * @author olaf
 */
public class ViskitEllipseObjectViewerAdapter extends ViskitDrawingObjectViewerAdapter {

    protected EllipseObjectViewerAdapter j2dDelegate;

    public ViskitEllipseObjectViewerAdapter(DrawingViewer viewer, EllipseObject drawingObject) {
        super(viewer, drawingObject);
        j2dDelegate = new EllipseObjectViewerAdapter(viewer, drawingObject);
    }

    @Override
    public EllipseObject getDrawingObject() {
        return (EllipseObject) super.getDrawingObject();
    }

    @Override
    public void paintObjectOn(GC gc) {
        ViskitGC vkgc = (ViskitGC) gc;
        if (vkgc.isGraphics2DAvailable() && !vkgc.isGlPreferred()) {
            j2dDelegate.paintObjectOn(gc);
        } 
        // OpenGL drawing
        else {
            Color c = getDrawingObject().getColor();
            Rectangle2D bounds = getDrawingObject().getBounds2D();
            if (vkgc.isLWJGLPreferred()) {
                GL11.glPushAttrib(GL11.GL_CURRENT_BIT | GL11.GL_ENABLE_BIT);
                try {
                    GL11.glShadeModel(GL11.GL_FLAT);

                    GL11.glColor3f((float) c.getRed() / 255F, (float) c.getGreen() / 255F, (float) c.getBlue() / 255F);

                    glPushObjToDisplayTransform();

                    // gl.glTranslated(-bounds.getCenterX(),
                    // -bounds.getCenterY(), 0);
                    // gl.glScaled(1.0 / bounds.getWidth(), 1.0 /
                    // bounds.getHeight(), 1);
                    GL11.glTranslated(bounds.getCenterX(), bounds.getCenterY(), 0);
                    GL11.glScaled(bounds.getWidth() / 2, bounds.getHeight() / 2, 1);

                    // TODO: for better performance, draw the unit circle from a
                    // vertex array or VBO. See ticket #17 for prerequisites.
                    GL11.glBegin(GL11.GL_LINE_LOOP);
                    int nSteps = 200;
                    double dphi = 2.0 * Math.PI / nSteps;
                    for (int i = 0; i < nSteps; i++) {
                        double phi = dphi * i;
                        GL11.glVertex2d(Math.sin(phi), Math.cos(phi));
                    }
                    GL11.glEnd();

                    glPopTransform();
                } finally {
                    GL11.glPopAttrib();
                }
            } else {
                GL2 gl = vkgc.getGl().getGL2();
                gl.glPushAttrib(GL2.GL_CURRENT_BIT | GL2.GL_ENABLE_BIT);
                try {
                    gl.glShadeModel(GL2.GL_FLAT);
                    gl.glColor3f((float) c.getRed() / 255F, (float) c.getGreen() / 255F, (float) c.getBlue() / 255F);

                    glPushObjToDisplayTransform(gl);
                    // gl.glTranslated(-bounds.getCenterX(),
                    // -bounds.getCenterY(), 0);
                    // gl.glScaled(1.0 / bounds.getWidth(), 1.0 /
                    // bounds.getHeight(), 1);
                    gl.glTranslated(bounds.getCenterX(), bounds.getCenterY(), 0);
                    gl.glScaled(bounds.getWidth() / 2, bounds.getHeight() / 2, 1);

                    // TODO: for better performance, draw the unit circle from a
                    // vertex array or VBO. See ticket #17 for prerequisites.
                    gl.glBegin(GL.GL_LINE_LOOP);
                    int nSteps = 200;
                    double dphi = 2.0 * Math.PI / nSteps;
                    for (int i = 0; i < nSteps; i++) {
                        double phi = dphi * i;
                        gl.glVertex2d(Math.sin(phi), Math.cos(phi));
                    }
                    gl.glEnd();

                    glPopTransform(gl);
                } finally {
                    gl.glPopAttrib();
                }
            }
        }

    }

}
