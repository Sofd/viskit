package de.sofd.viskit.image;

import de.sofd.draw2d.Drawing;
import de.sofd.viskit.ui.imagelist.ImageListViewModelElement;
import java.awt.image.BufferedImage;
import org.apache.log4j.Logger;

/**
 *
 */
public class DcmImageListViewModelElement implements ImageListViewModelElement {

    static final Logger log4jLogger = Logger.getLogger(DcmImageListViewModelElement.class);
    private Dcm dcm;
    private String label;
    private final Drawing roiDrawing;

    public DcmImageListViewModelElement(Dcm dcm) {
        this.dcm = dcm;
        roiDrawing = new Drawing();        
    }

    @Override
    public BufferedImage getImage() {
        return dcm.getImage();
    }

    @Override
    public Drawing getRoiDrawing() {
        return roiDrawing;
    }

    public Dcm getDcm() {
        return dcm;
    }

    public void setDcm(Dcm dcm) {
        this.dcm = dcm;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
