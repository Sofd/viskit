package de.sofd.viskit.model;

import java.io.File;
import java.util.Comparator;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import de.sofd.util.FloatRange;

/**
 * Concrete class that creates a model based on DICOM objects.If the object is a
 * singleframe DICOM a model element {@link ImageListViewModelElement} will be
 * created. If the object is a multiframe DICOM for each frame a model element
 * {@link ImageListViewModelElement} will be created. The unique key that is
 * used shall identify a DICOM series. A pattern may be PatientId +
 * StudyInstanceUID + SeriesInstanceUID.
 * 
 * @author honglinh
 * 
 */
public class DicomModelFactory extends ModelFactory {

    protected static final Logger logger = Logger.getLogger(DicomModelFactory.class);

    private boolean supportMultiframes = true;

    public DicomModelFactory() {
        super(null, null);
    }

    public DicomModelFactory(boolean supportMultiframes) {
        super(null, null);
        this.supportMultiframes = supportMultiframes;
    }

    /**
     * Constructor
     * 
     * 
     * @param supportMultiframes
     * @param cachePath
     *            if cachePath is null, caching is deactivated
     */
    public DicomModelFactory(boolean supportMultiframes, String cachePath) {
        super(cachePath, null);
        this.supportMultiframes = supportMultiframes;
    }

    /**
     * Constructor
     * 
     * @param c
     * @param supportMultiframes
     * @param cachePath
     *            if cachePath is null, caching is deactivated
     */
    public DicomModelFactory(Comparator<File> c, boolean supportMultiframes, String cachePath) {
        super(cachePath, c);
        this.supportMultiframes = supportMultiframes;
    }

    @Override
    protected float[] calculatePixelValueRange(ListModel model) {
        float[] minMaxRange = new float[2];
        if (model.getSize() > 0) {

            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            CachingDicomImageListViewModelElement element = (CachingDicomImageListViewModelElement) model
                    .getElementAt(0);
            DicomObject metadata = element.getDicomImageMetaData();

            // DICOM object contains pixel value range for complete series
            if (metadata.contains(Tag.SmallestPixelValueInSeries) && metadata.contains(Tag.LargestPixelValueInSeries)) {
                min = metadata.getInt(Tag.SmallestPixelValueInSeries);
                max = metadata.getInt(Tag.LargestPixelValueInSeries);
                logger.debug("Pixel value range in series found: " + element);
            }
            // iterate through all model elements and find the smallest
            else {
                logger
                        .debug("Pixel value range in Series not found, iterate through all model elements to calculate pixel value range for series");

                long startTime = System.currentTimeMillis();
                for (int i = 0; i < model.getSize(); i++) {
                    // TODO if this model element is a frame calculation just
                    // for the first frame
                    element = (CachingDicomImageListViewModelElement) model.getElementAt(i);
                    FloatRange range = element.getUsedPixelValuesRange();
                    float currentMin = range.getMin();
                    float currentMax = range.getMax();

                    if (currentMin < min) {
                        min = (int) currentMin;
                    }
                    if (currentMax > max) {
                        max = (int) currentMax;
                    }
                }
                logger.debug("[Min,Max] Range Calculation finished! Processing time: "
                        + (System.currentTimeMillis() - startTime) + " ms");
            }
            minMaxRange[0] = min;
            minMaxRange[1] = max;
        } else {
            logger.info("Model does not contain any DICOM files!");
        }
        return minMaxRange;
    }

    @Override
    protected void addElementToModel(DefaultListModel model, Object obj) {
        DicomImageListViewModelElement element = createDicomImageListViewModelElement(obj);
        model.addElement(element);
        if (supportMultiframes) {
            int numFrames = element.getTotalFrameNumber();
            logger.info("DICOM object " + obj + " has " + numFrames + " frames...");
            if (numFrames > 1) {
                for (int i = 1; i < numFrames; i++) {
                    DicomImageListViewModelElement felt = createDicomImageListViewModelElement(obj, i);
                    model.addElement(felt);
                }
            }
        }
    }

    protected DicomImageListViewModelElement createDicomImageListViewModelElement(Object f) {
        return new FileBasedDicomImageListViewModelElement((File) f);  //variant without frameNumber avoids reading the DICOM, which can increase model creation speed 10x
    }

    protected DicomImageListViewModelElement createDicomImageListViewModelElement(Object f, int frameNumber) {
        FileBasedDicomImageListViewModelElement felt = new FileBasedDicomImageListViewModelElement((File) f);
        felt.setFrameNumber(frameNumber);
        return felt;
    }

}