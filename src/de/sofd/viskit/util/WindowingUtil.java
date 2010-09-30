package de.sofd.viskit.util;

import de.sofd.util.FloatRange;
import de.sofd.viskit.model.DicomImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

public class WindowingUtil {
    static final Logger logger = Logger.getLogger(WindowingUtil.class);

    public static void setWindowingToDcm(ImageListViewCell cell) {
        DicomImageListViewModelElement elt = (DicomImageListViewModelElement) cell.getDisplayedModelElement();
        DicomObject dobj = elt.getDicomImageMetaData();
        if (dobj.contains(Tag.WindowCenter) && dobj.contains(Tag.WindowWidth)) {
            cell.setWindowLocation((int) dobj.getFloat(Tag.WindowCenter));
            cell.setWindowWidth((int) dobj.getFloat(Tag.WindowWidth));
        }
    }

    public static synchronized void setWindowingToOptimal(ImageListViewCell cell) {
        DicomImageListViewModelElement elt = (DicomImageListViewModelElement) cell.getDisplayedModelElement();
        try {
            /*Histogram histogram = elt.getHistogram();
            cell.setWindowWidth((int)(histogram.getStandardDeviation()*6));
            cell.setWindowLocation((int)histogram.getExpectedValue());
            System.out.println("histogram : " + histogram.toString());*/
            FloatRange usedRange = elt.getUsedPixelValuesRange();
            cell.setWindowWidth((int) usedRange.getDelta());
            cell.setWindowLocation((int) (usedRange.getMin() + usedRange.getMax()) / 2);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(elt.getImage().getImageKey());
            System.out.println(elt.getDicomImageMetaData().toString());
        }
    }

    public static void setWindowingToQC(ImageListViewCell cell) {
        DicomImageListViewModelElement element = (DicomImageListViewModelElement) cell.getDisplayedModelElement();

        DicomObject dicomObject = element.getDicomImageMetaData();
        Integer wc = (int) dicomObject.getFloat(Tag.WindowCenter);
        Integer ww = (int) dicomObject.getFloat(Tag.WindowWidth);
        if (wc != null && ww != null && !(wc == -1 && ww == -1)) {
            cell.setWindowLocation(wc);
            cell.setWindowWidth(ww);
        }
    }
}