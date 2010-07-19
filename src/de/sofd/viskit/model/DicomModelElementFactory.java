package de.sofd.viskit.model;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import org.apache.log4j.Logger;

/**
 * Concrete class that creates a model from a directory containing DICOM files.
 * If a file is a singleframe DICOM a model element
 * {@link ImageListViewModelElement} will be created. If a file is a multiframe
 * DICOM for each frame a model element {@link ImageListViewModelElement} will
 * be created.
 * 
 * @author honglinh
 * 
 */
public class DicomModelElementFactory extends ModelElementFactory {
    
    private boolean supportMultiframes = true;
    
    public DicomModelElementFactory() {
    }
    
    public DicomModelElementFactory(boolean supportMultiframes) {
        this.supportMultiframes = supportMultiframes;
    }

    public DicomModelElementFactory(Comparator<File> c) {
        this(c, true);
    }

    public DicomModelElementFactory(Comparator<File> c, boolean supportMultiframes) {
        this.supportMultiframes = supportMultiframes;
        this.comparator = c;
    }
    
    protected static final Logger logger = Logger.getLogger(DicomModelElementFactory.class);
    
    @Override
    public DefaultListModel createModelFromDir(File dir) {
        DefaultListModel result = new DefaultListModel();
        File[] files = readFilesFromDir(dir);
        for (File f : files) {
            addElementToModel(result,f);
        }
        return result;
    }

    @Override
    public DefaultListModel createModelFromFileCollection(Collection<File> fileCollection) {
        DefaultListModel result = new DefaultListModel();
        for(File f : fileCollection) {
            addElementToModel(result,f);            
        }
        return result;
    }
    
    private void addElementToModel(DefaultListModel model, File file) {
        if (!file.getName().toLowerCase().endsWith(".dcm")) {
            return;
        }
        logger.debug(file.getName());
        FileBasedDicomImageListViewModelElement element = new FileBasedDicomImageListViewModelElement(file);
        model.addElement(element);
        if (supportMultiframes) {
            int numFrames = element.getTotalFrameNumber();
            logger.info("DICOM object "+file.getName()+" has "+ numFrames +" frames...");          
            if (numFrames > 1) {
                for (int i = 1; i < numFrames; i++) {
                    FileBasedDicomImageListViewModelElement felt = new FileBasedDicomImageListViewModelElement(file);
                    felt.setFrameNumber(i);
                    model.addElement(felt);
                }
            }
        }
    }
}
