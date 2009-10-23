package de.sofd.viskit.image;

import java.net.URL;
import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

/**
 *
 */
public class Dcm {

    static final Logger log4jLogger = Logger.getLogger(Dcm.class);
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

    public Integer getWindowWidthTag() {
        Integer result = null;
        try {
            result = (int) dicomObject.getFloat(Tag.WindowWidth);
        } catch (Exception ex) {
            log4jLogger.error("getWindowWidthTag", ex);
        }
        return result;
    }

    public Integer getDicomWindowCenterTag() {
        Integer result = null;
        try {
            result = (int) dicomObject.getFloat(Tag.WindowCenter);
        } catch (Exception ex) {
            log4jLogger.error("getDicomWindowCenterTag", ex);
        }
        return result;
    }
}
