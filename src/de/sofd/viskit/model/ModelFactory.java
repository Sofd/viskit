package de.sofd.viskit.model;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;

/**
 * Abstract class for creating a model {@link DefaultListModel} with model
 * elements {@link ImageListViewModelElement} from a directory and calculate the
 * pixel value range of a model. If a comparator is set it will be used for
 * sorting the files
 * 
 * @author honglinh
 * 
 */
public abstract class ModelFactory {
    
    protected Comparator<File> comparator;
    protected String cachePath;
    protected Map<String,ListModel> keyModelMap = new HashMap<String,ListModel>();
    protected Map<String,float[]> keyMinMaxMap = new HashMap<String,float[]>();
    
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
     */
    protected abstract void addElementToModel(DefaultListModel model, File file);
    

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
     * create a model from a file directory, which is identified by a unique key
     * 
     * @param key
     * @param dir
     */
    public void addModel(String key, File dir) {
        ListModel model = createModelFromDir(dir);
        keyModelMap.put(key,model);
    }
    
    /**
     * create a model from a collection of files, which is identified by a unique key
     * 
     * @param key
     * @param paths
     */
    public void addModel(String key, Collection<File> paths) {
        ListModel model = createModelFromFileCollection(paths);
        keyModelMap.put(key,model);
    }
    
    protected DefaultListModel createModelFromDir(File dir) {
        DefaultListModel result = new DefaultListModel();
        File[] files = readFilesFromDir(dir);
        for (File f : files) {
            addElementToModel(result, f);
        }
        return result;
    }

    protected DefaultListModel createModelFromFileCollection(Collection<File> fileCollection) {
        DefaultListModel result = new DefaultListModel();
        for (File f : fileCollection) {
            addElementToModel(result, f);
        }
        return result;
    }
    
    /**
     * get the pixel value range for a model (lazy calculation of the range)
     * 
     * @param key
     * @return
     */
    public float[] getPixelRange(String key) {
        if(keyMinMaxMap.get(key) == null) {
            float[] range = calculatePixelValueRange(keyModelMap.get(key));
            keyMinMaxMap.put(key, range);
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
}