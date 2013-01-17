package de.sofd.viskit.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import de.sofd.util.FloatRange;
import de.sofd.viskit.model.ImageListViewModelElement.InitializationState;

/**
 * Concrete class that creates a model based on DICOM objects. If the object is a
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
    private boolean checkFileReadability = true;
    private boolean asyncMode = false;
    
    protected Map<Object,DefaultListModel> elementModelMap = new HashMap<Object,DefaultListModel>();

    public DicomModelFactory(String cachePath, Comparator<ImageListViewModelElement> comparator) {
        super(cachePath, comparator);
    }

    public DicomModelFactory() {
        super(null, null);
    }
    
    public boolean isSupportMultiframes() {
        return supportMultiframes;
    }

    /**
     * Specify whether the factory should check for multiframe images when creating
     * models. This requires that all DICOM files be read in before the model can
     * be created, so the model creation time may increase substantially.
     * 
     * @param supportMultiframes
     */
    public void setSupportMultiframes(boolean supportMultiframes) {
        this.supportMultiframes = supportMultiframes;
    }
    
    public boolean isCheckFileReadability() {
        return checkFileReadability;
    }

    /**
     * Specify whether the factory should check for file readability as it creates
     * model elements. Will increase model creation time moderately.
     * 
     * @param checkFileReadability
     */
    public void setCheckFileReadability(boolean checkFileReadability) {
        this.checkFileReadability = checkFileReadability;
    }
    
    public boolean isAsyncMode() {
        return asyncMode;
    }

    /**
     * Specify whether the factory should create all model elements in asynchronous
     * mode.
     * 
     * @param asyncMode
     */
    public void setAsyncMode(boolean asyncMode) {
        this.asyncMode = asyncMode;
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
                        .debug("Pixel value range in series not found, iterate through all model elements to calculate pixel value range for series");

                long startTime = System.currentTimeMillis();
                for (int i = 0; i < model.getSize(); i++) {
                    // TODO if this model element is a frame calculation just
                    // for the first frame
                    element = (CachingDicomImageListViewModelElement) model.getElementAt(i);
                    FloatRange range = element.getImage().getUsedPixelValuesRange();
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
    protected ImageListViewModelElement createModelElement(Object obj, final String key) {
        DicomImageListViewModelElement element = createDicomImageListViewModelElement(obj);
        final float[] currentRange = this.keyMinMaxMap.get(key);
        element.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (ImageListViewModelElement.PROP_INITIALIZATIONSTATE.equals(evt.getPropertyName())) {
                    if(evt.getNewValue().equals(InitializationState.INITIALIZED)) {
                        // calculate pixel value range of this initialized model element
                        // and compare it with the actual pixel value range and update the range
                        CachingDicomImageListViewModelElement element= (CachingDicomImageListViewModelElement)evt.getSource();
                        DicomObject metadata = element.getDicomImageMetaData();

                        // DICOM object contains pixel value range for complete series
                        if (metadata.contains(Tag.SmallestPixelValueInSeries) && metadata.contains(Tag.LargestPixelValueInSeries)) {
                            currentRange[0] = metadata.getInt(Tag.SmallestPixelValueInSeries);
                            currentRange[1] = metadata.getInt(Tag.LargestPixelValueInSeries);
                            logger.debug("Pixel value range in series found: " + element);
                            fireModelPixelValuesRangeChange(key,element,currentRange);
                        }
                        else {
                            logger
                            .debug("Iterate through all pixels of model element and calculate pixel value range");
                            FloatRange range = element.getImage().getUsedPixelValuesRange();
                            
                            float currentMin = currentRange[0];
                            float currentMax = currentRange[1];
                            if(range.getMin() < currentMin) {
                                currentRange[0] = range.getMin();
                                fireModelPixelValuesRangeChange(key,element,currentRange);
                            }
                            if(range.getMax() > currentMax) {
                                currentRange[1] = range.getMax();
                                fireModelPixelValuesRangeChange(key,element,currentRange);
                            }
                        }
                    }
                }
            }
        });
        return element;
    }

    @Override
    protected void appendElementToModel(ImageListViewModelElement elt, DefaultListModel model, String key) {
        super.appendElementToModel(elt, model, key);
        if (supportMultiframes && elt instanceof FileBasedDicomImageListViewModelElement) {
            FileBasedDicomImageListViewModelElement element = (FileBasedDicomImageListViewModelElement) elt;
            int numFrames = element.getTotalFrameNumber();
            logger.info("DICOM object " + element.getFile() + " has " + numFrames + " frames...");
            if (numFrames > 1) {
                for (int i = 1; i < numFrames; i++) {
                    DicomImageListViewModelElement felt = createDicomImageListViewModelElement(element.getFile(), i);
                    model.addElement(felt);
                }
            }

        }
    }

    protected DicomImageListViewModelElement createDicomImageListViewModelElement(Object f) {
        FileBasedDicomImageListViewModelElement result = new FileBasedDicomImageListViewModelElement((File) f, checkFileReadability);  //variant without frameNumber avoids reading the DICOM, which can increase model creation speed 10x
        result.setAsyncMode(asyncMode);
        return result;
    }

    protected DicomImageListViewModelElement createDicomImageListViewModelElement(Object f, int frameNumber) {
        FileBasedDicomImageListViewModelElement felt = new FileBasedDicomImageListViewModelElement((File) f, checkFileReadability);
        felt.setFrameNumber(frameNumber);
        return felt;
    }
}