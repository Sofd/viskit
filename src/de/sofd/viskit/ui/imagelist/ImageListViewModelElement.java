package de.sofd.viskit.ui.imagelist;

import de.sofd.draw2d.Drawing;
import java.awt.image.BufferedImage;

/**
 *
 * @author olaf
 */
public interface ImageListViewModelElement {

    BufferedImage getImage();

    Drawing getRoiDrawing();
}
