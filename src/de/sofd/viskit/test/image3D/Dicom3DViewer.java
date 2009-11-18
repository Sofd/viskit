package de.sofd.viskit.test.image3D;

import javax.swing.*;

import org.apache.log4j.*;

import vtk.*;

import de.sofd.viskit.image3D.vtk.VTK;
import de.sofd.viskit.image3D.vtk.util.*;
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
        
        vtkImageData imageData = DicomReader.readImageData("D:/dicom/serie1");
        imageData.Update();
        int dim[] =  imageData.GetDimensions();
        
        logger.debug("image dimension : " + dim[0] + " " + dim[1] + " " + dim[2] + " " + imageData.GetPointData().GetScalars().GetSize());
        dicom3DView = new Dicom3DView(imageData);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(dicom3DView, BorderLayout.CENTER);
        getContentPane().add("Center", panel);
        pack();
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


