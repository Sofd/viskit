package de.sofd.viskit.ui.imagelist.event.cellpaint;

import de.sofd.viskit.draw2d.gc.ViskitGC;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;

import java.awt.Rectangle;
import java.util.EventObject;
import java.util.Map;

/**
 * Event that indicates that a {@link ImageListViewCell} of a
 * {@link JImageListView} is being repainted. The receiver of the event (an
 * instance of {@link ImageListViewCellPaintListener}) can use the passed
 * {@link ViskitGC} of the event to paint anything into the cell.
 * <p>
 * There may be several {@link ImageListViewCellPaintListener}s on the same
 * {@link JImageListView}, in which case they all participate in painting the
 * cells of the JImageListView (they're called in the order of their z-order
 * (passed to
 * {@link JImageListView#addCellPaintListener(int, ImageListViewCellPaintListener)}
 * )), and thus paint on top of one another in that order.
 * 
 * @author Olaf Klischat
 */
public class ImageListViewCellPaintEvent extends EventObject {

    private final ViskitGC gc;
    private final Rectangle clip;
    private Map<String, Object> sharedContextData;
    private boolean consumed = false;
    
    public ImageListViewCellPaintEvent(ImageListViewCell source, ViskitGC gc, Rectangle clip, Map<String, Object> sharedContextData) {
        super(source);
        this.gc = gc;
        this.clip = clip;
        this.sharedContextData = sharedContextData;
    }
    
    @Override
    public ImageListViewCell getSource() {
        return (ImageListViewCell) super.getSource();
    }

    /**
     * 
     * @return {@link ViskitGC} to draw onto
     */
    public ViskitGC getGc() {
        return gc;
    }

    /**
     * 
     * @return clipping rectangle, or null if not available
     */
    public Rectangle getClip() {
        return clip;
    }

    /**
     * Shared context data for the paint event. See
     * {@link ImageListViewCellPaintListener#glSharedContextDataInitialization(javax.media.opengl.GL, Map)}
     * for details.
     * <p>
     * Not relevant for Java2D paint events.
     * 
     * @return
     */
    public Map<String, Object> getSharedContextData() {
        return sharedContextData;
    }

    public boolean isConsumed() {
        return consumed;
    }
    
    public void consume() {
        consumed = true;
    }

}
