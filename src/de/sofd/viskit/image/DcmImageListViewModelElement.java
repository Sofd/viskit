package de.sofd.viskit.image;

import de.sofd.viskit.model.AbstractImageListViewModelElement;
import java.awt.image.BufferedImage;
import org.apache.log4j.Logger;
import org.dcm4che2.data.Tag;

/**
 *
 */
public class DcmImageListViewModelElement extends AbstractImageListViewModelElement {

    static final Logger log4jLogger = Logger.getLogger(DcmImageListViewModelElement.class);
    private Dcm dcm;
    private String label;

    public DcmImageListViewModelElement(Dcm dcm) {
        this.dcm = dcm;
    }

    @Override
    public boolean hasBufferedImage() {
        return true;
    }

    @Override
    public BufferedImage getImage() {
        return dcm.getImage();
    }

    @Override
    public Object getImageKey() {
        return dcm.getBasicDicomObject().getString(Tag.SOPInstanceUID);
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
