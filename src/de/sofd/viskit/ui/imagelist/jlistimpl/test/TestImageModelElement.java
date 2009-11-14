package de.sofd.viskit.ui.imagelist.jlistimpl.test;

import de.sofd.draw2d.Drawing;
import de.sofd.draw2d.EllipseObject;
import de.sofd.draw2d.RectangleObject;
import de.sofd.viskit.ui.imagelist.ImageListViewModelElement;
import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/**
 *
 * @author olaf
 */
public class TestImageModelElement implements ImageListViewModelElement {

    private static BufferedImage image;
    private final Drawing roiDrawing;

    public TestImageModelElement(int number) {
        roiDrawing = new Drawing();
        RectangleObject rect = new RectangleObject();
        rect.setLocation(50, 30, 200, 125);
        rect.setColor(Color.RED);

        EllipseObject ellipse = new EllipseObject();
        ellipse.setLocation(90, 10, 135, 200);
        ellipse.setColor(Color.CYAN);

        EllipseObject ell2 = new EllipseObject();
        ell2.setLocation(rect.getLocation());
        ell2.setColor(Color.YELLOW);

        roiDrawing.addDrawingObject(rect);
        roiDrawing.addDrawingObject(ellipse);
        roiDrawing.addDrawingObject(ell2);
    }

    @Override
    public BufferedImage getImage() {
        if (null == image) {
            try {
                image = ImageIO.read(this.getClass().getResourceAsStream("mri_brain_small.jpg"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return image;
    }

    @Override
    public Drawing getRoiDrawing() {
        return roiDrawing;
    }

}
