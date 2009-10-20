package de.sofd.viskit.image;

import java.net.URL;
import org.dcm4che2.data.DicomObject;

/**
 *
 */
public class Dcm {

    DicomObject dicomObject;
    URL url;

    public Dcm() {
    }

    public DicomObject getDicomObject() {
        return dicomObject;
    }

    public void setDicomObject(DicomObject dicomObject) {
        this.dicomObject = dicomObject;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }
}
