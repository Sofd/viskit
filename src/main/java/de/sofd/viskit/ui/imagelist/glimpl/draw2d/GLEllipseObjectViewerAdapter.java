package de.sofd.viskit.ui.imagelist.glimpl.draw2d;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import de.sofd.draw2d.EllipseObject;
import de.sofd.draw2d.viewer.DrawingViewer;
import de.sofd.draw2d.viewer.gc.GC;

/**
 *
 * @author olaf
 */
public class GLEllipseObjectViewerAdapter extends GLDrawingObjectViewerAdapter {

    public GLEllipseObjectViewerAdapter(DrawingViewer viewer, EllipseObject drawingObject) {
        super(viewer, drawingObject);
    }

    @Override
    public EllipseObject getDrawingObject() {
        return (EllipseObject) super.getDrawingObject();
    }

    @Override
    public void paintObjectOn(GC gc) {
        GLGC glgc = (GLGC) gc;
        Color c = getDrawingObject().getColor();
        Rectangle2D bounds = getDrawingObject().getBounds2D();
        GL2 gl = glgc.getGl().getGL2();
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
