package de.sofd.viskit.draw2d.gc;

import de.sofd.draw2d.viewer.gc.GC;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.media.opengl.GL;

/**
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
