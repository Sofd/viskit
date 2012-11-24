package de.sofd.viskit.ui.imagelist.glimpl.draw2d;

import java.awt.Rectangle;

import javax.media.opengl.GL;

import de.sofd.draw2d.viewer.gc.GC;

/**
 * Draw2d GC implementation for JOGL drawing. GC instances of this class are
 * passed around whenever painting tasks must be delegated
 * 
 * @author olaf
 */
public class GLGC extends GC {
    protected GL gl;
    
    public GLGC(GL gl) {
        super(null);
        this.gl = gl;
    }

    public GL getGl() {
        return gl;
    }
    
    @Override
    public Rectangle getClipBounds() {
        return null;
    }

}
