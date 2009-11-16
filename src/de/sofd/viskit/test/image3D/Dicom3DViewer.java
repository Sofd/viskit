package de.sofd.viskit.test.image3D;

import javax.swing.*;

import org.apache.log4j.*;

import vtk.*;

import de.sofd.viskit.image3D.vtk.VTK;
import de.sofd.viskit.image3D.vtk.view.*;

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
    
    Dicom3DView dicom3DView;
    
    public Dicom3DViewer() throws Exception
    {
        super("Dicom3D");
  
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        vtkImageData imageData = readImageData();
        imageData.Update();
        int dim[] =  imageData.GetDimensions();
        
        logger.debug("image dimension : " + dim[0] + " " + dim[1] + " " + dim[2] + " " + imageData.GetPointData().GetScalars().GetSize());
        dicom3DView = new Dicom3DView(imageData);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(dicom3DView, BorderLayout.CENTER);
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
        
        //vDicom.SetDirectoryName(rootDirName + "/de/sofd/viskit/test/resources/series/series4");
        vDicom.SetDirectoryName("D:/dicom/serie1");
        //vDicom.SetDataSpacing(3.2, 3.2, 1.5);
       
        return vDicom.GetOutput();
    }
    
    public void startTimers()
    {
        dicom3DView.startTimer();
    }

    public static void main(String s[]) {
        try {
            VTK.init();
            
            PropertyConfigurator.configureAndWatch("log4j.properties", 6000);
            
            final Dicom3DViewer viewer = new Dicom3DViewer();
            viewer.setLocationRelativeTo(null);
            viewer.setVisible(true);
            
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    viewer.startTimers();
                }
            });
            
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        
        

        
        /*SwingUtilities.invokeLater(new Runnable() { 
            @Override 
            public void run () { 
                try {
                    Dicom3DViewer viewer = new Dicom3DViewer();
                    viewer.setVisible(true); 
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                }
                    
            } 
        }); */
  }
}

