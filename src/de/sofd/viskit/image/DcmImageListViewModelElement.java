package de.sofd.viskit.image;

import de.sofd.draw2d.Drawing;
import de.sofd.viskit.model.ImageListViewModelElement;
import java.awt.image.BufferedImage;
import org.apache.log4j.Logger;
import org.dcm4che2.data.Tag;

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
    public Object getImageKey() {
        return dcm.getBasicDicomObject().getString(Tag.SOPInstanceUID);
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
