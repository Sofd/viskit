package de.sofd.viskit.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import org.apache.log4j.Logger;

/**
 * Abstract class for creating a model {@link DefaultListModel} with model
 * elements {@link ImageListViewModelElement} from a directory, a collection
 * of files or an existing model and calculate the pixel value range of a model. A model is
 * identified by a unique key. The factory enables calculation of pixel value
 * ranges of a model. This calculation may be time consuming. So a cache can be
 * used to store and retrieve the pixel value range of a model. If a comparator
 * is set it will be used for sorting the files in a directory. 
 * 
 * TODO: model factory has to be adopted to asynchronous mode:
 * 
 * getPixelRange()
 * writeInCache()
 * calculatePixelValuesRange() still necessary?
 * 
 * perhaps a flag is needed to know that all model elements have been initialized once
 * 
 * @author honglinh
 * 
 */
public abstract class ModelFactory {
    
    protected String cachePath;
    protected boolean enableRangeCaching = true;
    protected Comparator<File> comparator;
    protected String cacheFile;
    protected Map<String,ListModel> keyModelMap = new LinkedHashMap<String,ListModel>();
    protected Map<String,float[]> keyMinMaxMap = new HashMap<String,float[]>();
    protected Collection<ModelPixelValuesRangeChangeListener> listener = new ArrayList<ModelPixelValuesRangeChangeListener>();
    protected static final Logger logger = Logger.getLogger(ModelFactory.class);
    
    public ModelFactory(String cachePath, Comparator<File> comparator) {
        this.cachePath = cachePath;
        this.comparator = comparator;
        if(cachePath == null || !enableRangeCaching) {
            logger.info("Pixel Range Caching disabled");
            enableRangeCaching = false;
        }
        else {
            logger.info("Initialize Pixel Range Caching");
            initializedCache();
        }
    }
    
    /**
     * initializes the cache. Cache entry pattern is <key,min,max>
     */
    private void initializedCache() {
        Scanner scanner = null;
        File cacheFile = new File(cachePath);
        try {
            // cache file does not exist yet, so create a new one
            if (!cacheFile.exists()) {
                cacheFile.createNewFile();
            } 
            // cache file already exists, so read all the cache values from file
            else {
                scanner = new Scanner(cacheFile);
                while (scanner.hasNextLine()) {
                    Scanner lineScanner = new Scanner(scanner.nextLine());
                    lineScanner.useDelimiter(",");
                    if (lineScanner.hasNext()) {
                        String keyValue = lineScanner.next();

                        float[] range = new float[2];
                        range[0] = Float.valueOf(lineScanner.next());
                        range[1] = Float.valueOf(lineScanner.next());
                        keyMinMaxMap.put(keyValue, range);
                        logger.info("Pixel Range Caching Entry found in Cache file: Key=" + keyValue + ", Min="
                                + range[0] + ", Max= " + range[1]);
                    }
                    lineScanner.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            enableRangeCaching = false;
        }
        finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }
    
    /**
     * write the range of with the key of the model into the cache file. Cache entry pattern is <key,min,max>
     */
    private void writeRangeInCache(String key, float[] range) {
        File cacheFile = new File(cachePath);
        Writer output = null;
        try {
            output = new BufferedWriter(new FileWriter(cacheFile,true));
            output.write(key + "," + range[0]+ "," + range[1]+System.getProperty("line.separator"));
            logger.info("Pixel Range Caching Entry added in Cache file: Key="+key+", Min="+range[0]+", Max= "+range[1]);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * abstract method to calculate the used pixel value range for a model. This method may be time consuming
     * 
     * @param model
     * @return the minimum and maximum used pixel values
     */
    protected abstract float[] calculatePixelValueRange(ListModel model);
    
    /**
     * abstract method to create a model element or many model elements from a file and add it to the model
     * 
     * @param model
     * @param file
     * @param key
     */
    protected abstract void addElementToModel(DefaultListModel model, Object file, String key);
    

    /**
     * This method reads files from a directory and sorts the files according to
     * the natural order or to the order the comparator defines
     * 
     * @param dir
     * @return sorted files of the directory
     */
    protected File[] readFilesFromDir(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            throw new NullPointerException(dir.getAbsolutePath() + " does not contain any files or is not a directory");
        }
        // sort files
        if (comparator == null) {
            Arrays.sort(files);
        }
        // sort files using the comparator
        else {
            Arrays.sort(files, comparator);
        }
        return files;
    }
    
    /**
     * create a model from a file directory. The model is identified by a unique key
     * 
     * @param key
     * @param dir
     */
    public void addModel(String key, File dir) {
        keyMinMaxMap.put(key, new float[]{0,1});
        ListModel model = createModelFromDir(dir,key);
        keyModelMap.put(key,model);
    }
    
    /**
     * create a model from a collection. The model is identified by a unique key
     * 
     * @param key
     * @param paths
     */
    public void addModel(String key, Collection<?> paths) {
        keyMinMaxMap.put(key, new float[]{0,1});
        ListModel model = createModelFromCreationContextCollection(paths,key);                
        keyModelMap.put(key,model);
    }
    
    /**
     * Just add a model that was already created and filled externally.
     * 
     * @param key
     * @param model
     */
    public void addModel(String key, ListModel model) {
        keyMinMaxMap.put(key, new float[]{0,1});
        keyModelMap.put(key,model);
    }

    protected DefaultListModel createModelFromDir(File dir, String key) {
        DefaultListModel result = new DefaultListModel();
        File[] files = readFilesFromDir(dir);
        for (File f : files) {
            addElementToModel(result, f, key);
        }
        return result;
    }

    protected DefaultListModel createModelFromCreationContextCollection(Collection<?> collection,String key) {
        DefaultListModel result = new DefaultListModel();
        for (Object f : collection) {
            addElementToModel(result, f, key);
        }
        return result;
    }
    
    /**
     * get current pixel value range for a model
     * 
     * @param key
     * @return
     */
    public float[] getPixelRange(String key) {
        if(keyMinMaxMap.get(key) == null) {
            float[]range = calculatePixelValueRange(keyModelMap.get(key));
            keyMinMaxMap.put(key, range);
            
            // write in cache
            if(enableRangeCaching) {
                writeRangeInCache(key,range);
            }
        }
        return keyMinMaxMap.get(key);
    }

    /**
     * get the model identified by the unique key
     * 
     * @param key
     * @return
     */
    public ListModel getModel(String key) {
        return keyModelMap.get(key);
    }

    /**
     * Get the keys of all models known to this factory, in the order of their creation
     * @return
     */
    public Collection<String> getAllModelKeys() {
        return keyModelMap.keySet();
    }
    
    public int getModelsCount() {
        return keyModelMap.size();
    }
    
    public boolean isEnableRangeCaching() {
        return enableRangeCaching;
    }

    public void setEnableRangeCaching(boolean enableRangeCaching) {
        if(cachePath == null) {
            logger.warn("Cache path is null! Could not enable caching");
        }
        else {
            this.enableRangeCaching = enableRangeCaching;
        }
    }
    
    public static interface ModelPixelValuesRangeChangeListener extends EventListener {

        public void pixelvaluesRangeChange(String modelKey,ImageListViewModelElement element, float[] range);

    }

    public void addModelPixelValuesRangeChangeListener(ModelPixelValuesRangeChangeListener l) {
        listener.add(l);
    }

    public void removeModelPixelValuesRangeChangeListener(ModelPixelValuesRangeChangeListener l) {
        listener.remove(l);
    }

    protected void fireModelPixelValuesRangeChange(String modelKey, ImageListViewModelElement element, float[] range) {
        logger.debug("Pixel values range changed for model with key:"+modelKey+" new range: ["+range[0]+","+range[1]+"]");
        logger.debug("Fire model pixel values range change event");
        for (ModelPixelValuesRangeChangeListener l : listener) {
            l.pixelvaluesRangeChange(modelKey,element, range);
        }
    }
}