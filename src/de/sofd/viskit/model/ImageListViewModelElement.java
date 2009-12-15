package de.sofd.viskit.model;

import de.sofd.draw2d.Drawing;
import java.awt.image.BufferedImage;

/**
 *
 * @author olaf
 */
public interface ImageListViewModelElement {

    BufferedImage getImage();

    /**
     * Returns a key that uniquely identifies the image returned by getImage().
     * <p>
     * The key can be used by various front-end (view) and other components for caching data associated
     * with the view.
     * <p>
     * This key should be constant under equals()/hashCode() throughout the lifetime of <i>this</i>. This method will
     * be called often, so it should operate quickly. It should not call getImage() if getImage() may take
     * long to execute.
     *
     * @return
     */
    Object getImageKey();

    Drawing getRoiDrawing();
}
