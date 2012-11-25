package de.sofd.viskit.ui.imagelist.twlimpl.draw2d;

import java.awt.Rectangle;

import de.sofd.draw2d.viewer.gc.GC;

/**
 * Draw2d GC implementation for LWJGL drawing. GC instances of this class are
 * passed around whenever painting tasks must be delegated
 * 
 * TODO: This class isn't really needed (it defines no state; the base class
 * could be used) since there is no LWJGL context class or anything else that an
 * LWJGLGC might wrap. If we were delegating TWL drawing tasks too (rather than
 * just LWJGL drawing tasks), we'd wrap a de.matthiasmann.twl.renderer.Renderer.
 * But we're not doing this atm.
 * 
 * @author olaf
 */
public class LWJGLGC extends GC {

    public LWJGLGC() {
        super(null);
    }

    @Override
    public Rectangle getClipBounds() {
        return null;
    }

}
