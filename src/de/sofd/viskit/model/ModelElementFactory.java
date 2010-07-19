package de.sofd.viskit.model;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import javax.swing.DefaultListModel;

/**
 * Abstract class for creating a model {@link DefaultListModel} with model
 * elements {@link ImageListViewModelElement} from a directory. This class
 * contains an abstract factory method that subclasses must implement. If a
 * comparator is set it will be used for sorting the files
 * 
 * @author honglinh
 * 
 */
public abstract class ModelElementFactory {
    
    protected Comparator<File> comparator;
    

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
        else {
            Arrays.sort(files, comparator);
        }
        return files;
    }

    /**
     * Abstract method for creating a model from reading files of a directory
     * 
     * @param dir
     * @return the default list model
     */
    public abstract DefaultListModel createModelFromDir(File dir);
    
    /**
     * Abstract method for creating a model from a collection of files
     * 
     * @param file collection
     * @return the default list model
     */    
    public abstract DefaultListModel createModelFromFileCollection(Collection<File> fileCollection);
}