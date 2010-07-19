package de.sofd.viskit.model;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ListModel;

import org.apache.log4j.Logger;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import de.sofd.util.FloatRange;

/**
 * 
 * @author honglinh
 *
 */
public class DicomModelFactory extends ModelFactory {
    
    protected static final Logger logger = Logger.getLogger(DicomModelFactory.class);
    
    protected ModelElementFactory factory;
    protected String cachePath;
    protected Map<String,ListModel> keyModelMap = new HashMap<String,ListModel>();
    protected Map<String,float[]> keyMinMaxMap = new HashMap<String,float[]>();
    
    public DicomModelFactory(ModelElementFactory factory, String cachePath)
    {
        this.factory = factory;
        this.cachePath = cachePath;
        //TODO enable caching of pixelrange values
    }
    
    private void insertModelAndCalculatePixelValueRange(String key, ListModel model) {
        if(model.getSize() > 0) {

            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            CachingDicomImageListViewModelElement element = (CachingDicomImageListViewModelElement)model.getElementAt(0);
            DicomObject metadata =element.getDicomImageMetaData();
            
            // DICOM object contains pixel value range for complete series
            if(metadata.contains(Tag.SmallestPixelValueInSeries) && metadata.contains(Tag.LargestPixelValueInSeries)) {
                min = metadata.getInt(Tag.SmallestPixelValueInSeries);
                max = metadata.getInt(Tag.LargestPixelValueInSeries);
                logger.debug("Pixel value range in series found: "+element);
            }
            // iterate through all model elements and find the smallest
            else {
                logger.debug("Pixel value range in Series not found, iterate through all model elements to calculate pixel value range for series: "+key);
                
                long startTime = System.currentTimeMillis();
                for(int i = 0; i<model.getSize()-1;i++) {
                    //TODO if this model element is a frame calculation just for the first frame
                    element = (CachingDicomImageListViewModelElement)model.getElementAt(i);
                    FloatRange range = element.getUsedPixelValuesRange();
                    float currentMin = range.getMin();
                    float currentMax = range.getMax();
                    
                    if(currentMin < min) {
                        min = (int)currentMin;
                    }
                    if(currentMax > max) {
                        max = (int)currentMax;
                    }
                }
                logger.debug("Calculation finished! Processing time: "+(System.currentTimeMillis()-startTime)+" ms");
            }
            float[] range = {min,max};
            keyMinMaxMap.put(key, range);
        }
        else {
            logger.info("Model for Key: "+key+" does not contain any DICOM files!");
        }
    }
    
    @Override
    public void addModel(String key, File dir) {
        ListModel model = factory.createModelFromDir(dir);
        keyModelMap.put(key,model);

        
    }
    
    @Override
    public void addModel(String key, Collection<File> paths) {
        ListModel model = factory.createModelFromFileCollection(paths);
        keyModelMap.put(key,model);
    }

    @Override
    public float[] getPixelRange(String key) {
        if(keyMinMaxMap.get(key) == null) {
            insertModelAndCalculatePixelValueRange(key, keyModelMap.get(key));
        }
        return keyMinMaxMap.get(key);
    }

    @Override
    public ListModel getModel(String key) {
        return keyModelMap.get(key);
    }
}