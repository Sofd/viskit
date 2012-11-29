package de.sofd.viskit.ui.imagelist.testapp;

import de.sofd.draw2d.Drawing;
import de.sofd.draw2d.EllipseObject;
import de.sofd.draw2d.RectangleObject;
import de.sofd.viskit.model.FileBasedDicomImageListViewModelElement;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 *
 * @author olaf
 */
public class TestImageModelElement extends FileBasedDicomImageListViewModelElement {

    private static BufferedImage image;
    private final Drawing roiDrawing;

    public TestImageModelElement(int number) {
        super(TestImageModelElement.class.getResource("/de/sofd/viskit/test/resources/series/series2/cd014__center001__24.dcm"));
        roiDrawing = getRoiDrawing();
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

}
