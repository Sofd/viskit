package de.sofd.viskit.model;

import java.io.File;
import java.util.Comparator;

import javax.swing.DefaultListModel;

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
public class DicomModelFactory extends ModelFactory {
    
    private boolean supportMultiframes = true;
    
    public DicomModelFactory() {
    }
    
    public DicomModelFactory(boolean supportMultiframes) {
        
    }

    public DicomModelFactory(Comparator<File> c) {
        this(c, true);
    }

    public DicomModelFactory(Comparator<File> c, boolean supportMultiframes) {
        this.supportMultiframes = supportMultiframes;
        this.comparator = c;
    }
    
    protected static final Logger logger = Logger.getLogger(DicomModelFactory.class);
    
    @Override
    public DefaultListModel createModelFromDir(File dir) {
        DefaultListModel result = new DefaultListModel();
        File[] files = readFilesFromDir(dir);
        for (File f : files) {
            if (!f.getName().toLowerCase().endsWith(".dcm")) {
                continue;
            }
            logger.debug(f.getName());
            FileBasedDicomImageListViewModelElement element = new FileBasedDicomImageListViewModelElement(f);
            result.addElement(element);
            if (supportMultiframes) {
                int numFrames = element.getTotalFrameNumber();
                logger.info("DICOM object "+f.getName()+" has "+ numFrames +" frames...");          
                if (numFrames > 1) {
                    for (int i = 1; i < numFrames; i++) {
                        FileBasedDicomImageListViewModelElement felt = new FileBasedDicomImageListViewModelElement(f);
                        felt.setFrameNumber(i);
                        result.addElement(felt);
                        logger.debug(" > Frame "+ (i+1));
                    }
                }
            }
        }
        return result;
    }
}
