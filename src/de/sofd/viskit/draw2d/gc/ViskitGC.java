package de.sofd.viskit.draw2d.gc;

import de.sofd.draw2d.viewer.gc.GC;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.media.opengl.GL;

/**
 * Draw2d GC implementation for Viskit. Wraps either a Java2D Graphics2D or
 * an OpenGL/Jogl GL (or both at the same time). Instances of this class are
 * passed aroung in viskit's 2D liste viewer code whenever painting tasks
 * must be delegated, e.g. when a JImageListView instance wants to delegate
 * the painting of one of its cells to an external component such as a
 * controller.
 * <p>
 * The painting code can then query the received ViskitGC instance for whether
 * it wraps a Graphics2D or a GL (or, if  both are present, which one should
 * be used preferrably). The painting code then goes ahead and paints its
 * stuff using whatever facility it supports and is available to use.
 *
 * @author olaf
 */
public class ViskitGC extends GC {

    protected GL gl;
    protected boolean isGlPreferred = false;

    public ViskitGC(Graphics2D g2d) {
        this(g2d, null, false);
    }

    public ViskitGC(GL gl) {
        this(null, gl, true);
    }

    public ViskitGC(Graphics2D g2d, GL gl, boolean isGlpreferred) {
        super(g2d);
        this.gl = gl;
        this.isGlPreferred = isGlpreferred;
    }

    public GL getGl() {
        return gl;
    }

    public boolean isGlAvailable() {
        return (gl != null);
    }

    public boolean isGraphics2DAvailable() {
        return (getGraphics2D() != null);
    }

    public boolean isGlPreferred() {
        return isGlPreferred;
    }

    @Override
    public Rectangle getClipBounds() {
        if (isGraphics2DAvailable() && ! isGlPreferred()) {
            return super.getClipBounds();
        } else {
            return null;
        }
    }

}
