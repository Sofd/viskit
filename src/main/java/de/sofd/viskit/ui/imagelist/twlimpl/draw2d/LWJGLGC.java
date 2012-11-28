package de.sofd.viskit.ui.imagelist.twlimpl.draw2d;

import java.awt.Rectangle;

import de.matthiasmann.twl.renderer.Renderer;
import de.sofd.draw2d.viewer.gc.GC;

/**
 * Draw2d GC implementation for LWJGL drawing. GC instances of this class are
 * passed around whenever painting tasks must be delegated.
 * 
 * @author olaf
 */
public class LWJGLGC extends GC {

    private final Renderer renderer;
    
    public LWJGLGC(Renderer renderer) {
        super(null);
        this.renderer = renderer;
    }

    @Override
    public Rectangle getClipBounds() {
        return null;
    }

    public Renderer getTWLRenderer() {
        return renderer;
    }
}
