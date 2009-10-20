package de.sofd.viskit.image;

import de.sofd.draw2d.Drawing;
import de.sofd.viskit.ui.imagelist.ImageListViewModelElement;
import java.awt.image.BufferedImage;
import java.util.Random;
import org.apache.log4j.Logger;

/**
 *
 */
public class DcmImageListViewModelElement implements ImageListViewModelElement {

    static final Logger log4jLogger = Logger.getLogger(DcmImageListViewModelElement.class);
    private static Random random = new Random();
    private Dcm dcm;
    private String id = String.valueOf(System.currentTimeMillis()) + String.valueOf(random.nextLong());

    @Override
    public BufferedImage getImage() {        
        return new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public Drawing getRoiDrawing() {
        return new Drawing();
    }
}
