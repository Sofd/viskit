package de.sofd.viskit.model;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.swing.DefaultListModel;

import org.apache.log4j.Logger;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReaderSpi;

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
    
    public DicomModelFactory() {
    }
    
    public DicomModelFactory(Comparator<File> c) {
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
            ImageReader reader;
            try {
                reader = new DicomImageReaderSpi().createReaderInstance();

            FileImageInputStream input = new FileImageInputStream(f);
            reader.setInput(input);            
            int numFrames = reader.getNumImages(true);
            
            logger.info("DICOM object "+f.getName()+" has "+ numFrames +" frames...");            
            for (int i=0; i < numFrames; i++) {
                FileBasedDicomImageListViewModelElement element = new FileBasedDicomImageListViewModelElement(f);
                element.setFrameNumber(i);
                result.addElement(element);
                logger.debug(" > Frame "+ (i+1));
            }
            } catch (IOException e) {
                throw new IllegalStateException("error trying to extract frames from DICOM object: " + f, e);
            }
        }
        return result;
    }
}
