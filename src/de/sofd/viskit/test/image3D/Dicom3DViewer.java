package de.sofd.viskit.test.image3D;

import javax.swing.*;

import org.apache.log4j.*;

import vtk.*;

import de.sofd.viskit.image3D.*;
import de.sofd.viskit.image3D.view.*;

import java.awt.*;
import java.io.*;
import java.util.*;

/**
 * This example reads a volume dataset, extracts two isosurfaces that
 * represent the skin and bone, and then displays them.
 */
@SuppressWarnings("serial")
public class Dicom3DViewer extends JFrame {
    static final Logger logger = Logger.getLogger(Dicom3DViewer.class);
    
    static { 
        System.loadLibrary("vtkCommonJava"); 
        System.loadLibrary("vtkFilteringJava"); 
        System.loadLibrary("vtkIOJava"); 
        System.loadLibrary("vtkImagingJava"); 
        System.loadLibrary("vtkGraphicsJava"); 
        System.loadLibrary("vtkRenderingJava"); 
        try {
          System.loadLibrary("vtkHybridJava");
        } catch (Throwable e) {
          System.out.println("cannot load vtkHybrid, skipping...");
        }
        try {
          System.loadLibrary("vtkVolumeRenderingJava");
        } catch (Throwable e) {
          System.out.println("cannot load vtkVolumeRendering, skipping...");
        }
      }
    
    public Dicom3DViewer() throws Exception
    {
        super("Dicom3D");
  
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        vtkImageData imageData = readImageData();
        
        int dim = imageData.GetDataDimension();
        logger.debug("image dimension : " + dim);
        Dicom3DView dicom3DView = new Dicom3DView(imageData);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(dicom3DView.panel, BorderLayout.CENTER);
        getContentPane().add("Center", panel);
        pack();
    }
    
    protected vtkImageData readImageData() throws IOException {
        Properties props = new Properties();
        props.load(
            this.getClass().getResourceAsStream("/de/sofd/viskit/test/singleframe/SingleFrameTestApp.properties")
        );
        
        String rootDirName = props.getProperty("rootDir");
        if (null == rootDirName) {
            throw new IllegalStateException("SingleFrameTestApp.properties file does not contain a rootDir property");
        }
    
        // The following reader is used to read a series of 2D slices (images)
        // that compose the volume. The slice dimensions are set, and the
        // pixel spacing. The data Endianness must also be specified. The reader
        // usese the FilePrefix in combination with the slice number to construct
        // filenames using the format FilePrefix.%d. (In this case the FilePrefix
        // is the root name of the file: quarter.)
        vtkDICOMImageReader vDicom = new vtkDICOMImageReader();
        vDicom.SetDataByteOrderToLittleEndian();
        
        logger.debug(rootDirName + "/de/sofd/viskit/test/resources/series/series2");
        
        //vDicom.SetDirectoryName(rootDirName + "/de/sofd/viskit/test/resources/series/series4");
        vDicom.SetDirectoryName("D:/dicom/serie1");
        //vDicom.SetDataSpacing(3.2, 3.2, 1.5);
    
        return vDicom.GetOutput();
    }

    public static void main(String s[]) {
        try {
            //VTK.init();
            
            //Logger log = Logger.getLogger(Dicom3DViewer.class);
            PropertyConfigurator.configureAndWatch("log4j.properties", 6000);
            /*System.setOut(new PrintStream(new LoggingOutputStream(log, Level.INFO), true));
            System.setErr(new PrintStream(new LoggingOutputStream(log, Level.ERROR), true));*/
          
            Dicom3DViewer viewer = new Dicom3DViewer();
            viewer.setVisible(true);
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
  }
}


