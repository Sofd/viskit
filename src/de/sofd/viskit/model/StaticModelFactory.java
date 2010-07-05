package de.sofd.viskit.model;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.swing.DefaultListModel;

import org.apache.log4j.Logger;
import org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReaderSpi;

/**
 * 
 * @author honglinh
 *
 */
public class StaticModelFactory {
    
    protected static final Logger logger = Logger.getLogger(StaticModelFactory.class);
    
    public static DefaultListModel createModelFromDir(File dir) {
        DefaultListModel result = new DefaultListModel();
        File[] files = dir.listFiles();
        Arrays.sort(files);
        for (File f : files) {
            if (!f.getName().toLowerCase().endsWith(".dcm")) {
                continue;
            }
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
                e.printStackTrace();
            }
        }
        return result;
    }
}