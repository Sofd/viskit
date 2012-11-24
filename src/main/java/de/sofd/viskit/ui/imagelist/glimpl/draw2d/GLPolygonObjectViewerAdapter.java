package de.sofd.viskit.ui.imagelist.glimpl.draw2d;

import java.awt.Color;
import java.awt.geom.Point2D;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import de.sofd.draw2d.PolygonObject;
import de.sofd.draw2d.viewer.DrawingViewer;
import de.sofd.draw2d.viewer.gc.GC;

/**
 *
 * @author olaf
 */
public class GLPolygonObjectViewerAdapter extends GLDrawingObjectViewerAdapter {

    public GLPolygonObjectViewerAdapter(DrawingViewer viewer, PolygonObject drawingObject) {
        super(viewer, drawingObject);
    }

    @Override
    public PolygonObject getDrawingObject() {
        return (PolygonObject) super.getDrawingObject();
    }

    @Override
    public void paintObjectOn(GC gc) {
        GLGC glgc = (GLGC) gc;
        Color c = getDrawingObject().getColor();
        GL2 gl = glgc.getGl().getGL2();
        gl.glPushAttrib(GL2.GL_CURRENT_BIT|GL2.GL_ENABLE_BIT);
        try {
            gl.glShadeModel(GL2.GL_FLAT);
            gl.glColor3f((float) c.getRed() / 255F,
                         (float) c.getGreen() / 255F,
                         (float) c.getBlue() / 255F);
            int ptCount = getDrawingObject().getPointCount();
            if (ptCount > 1) {
                glPushObjToDisplayTransform(gl);
                gl.glBegin(getDrawingObject().isClosed() ? GL.GL_LINE_LOOP : GL.GL_LINE_STRIP);
                for (int i = 1; i < ptCount; ++i) {
                    Point2D nextPt = getDrawingObject().getPoint(i);
                    gl.glVertex2d(nextPt.getX(), nextPt.getY());
                }
                gl.glEnd();
                glPopTransform(gl);
            }
        } finally {
            gl.glPopAttrib();
        }
    }
    
}
