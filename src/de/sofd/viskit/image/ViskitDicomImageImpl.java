package de.sofd.viskit.image;

import org.dcm4che2.data.DicomObject;

/**
 * Simple ViskitImage that directly represents a frame of a DicomObject.
 * 
 * @author Olaf Klischat
 */
public class ViskitDicomImageImpl extends ViskitDicomImageBase {

    private final DicomObject dicomObject;

    public ViskitDicomImageImpl(DicomObject dicomObject, int frameNumber) {
        super(frameNumber);
        this.dicomObject = dicomObject;
    }
    
    @Override
    public DicomObject getDicomObject() {
        return dicomObject;
    }

}
