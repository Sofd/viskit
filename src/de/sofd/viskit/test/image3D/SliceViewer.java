package de.sofd.viskit.test.image3D;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.*;

import vtk.*;

import de.sofd.viskit.image3D.vtkk.VTK;
import de.sofd.viskit.image3D.vtkk.model.ImagePlane;
import de.sofd.viskit.image3D.vtkk.view.*;

import java.awt.*;
import java.io.*;
import java.util.*;

/**
 * This example reads a volume dataset, extracts two isosurfaces that
 * represent the skin and bone, and then displays them.
 */
@SuppressWarnings("serial")
public class SliceViewer extends JFrame implements ChangeListener {
    static final Logger logger = Logger.getLogger(SliceViewer.class);
    
    SliceView sliceView;
    
    public SliceViewer(ImagePlane imagePlane) throws Exception
    {
        super("SliceViewer");
        
        
  
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        vtkImageData imageData = readImageData();
        imageData.Update();
        int dim[] =  imageData.GetDimensions();
        
        logger.debug("image dimension : " + dim[0] + " " + dim[1] + " " + dim[2] + " " + imageData.GetPointData().GetScalars().GetSize());
        sliceView = new SliceView(imageData, imagePlane);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(sliceView, BorderLayout.CENTER);
        
        JSlider slider = new JSlider(0, ( imagePlane == ImagePlane.PLANE_TRANSVERSE ? dim[2]-1 : dim[1]-1 ));
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(1);
        //slider.setPaintLabels(true);
        //slider.setPaintTicks(true);
        slider.setSnapToTicks(true);
        slider.addChangeListener(this);
        slider.setValue(sliceView.getCurrentSliceNr());
        panel.add(slider, BorderLayout.SOUTH);
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
        vDicom.SetDirectoryName("D:/dicom/serie4");
        //vDicom.SetDataSpacing(3.2, 3.2, 1.5);
       
        return vDicom.GetOutput();
    }

    public void startTimers()
    {
        sliceView.startTimer();
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        int sliceNr = (int)source.getValue();
        sliceView.showSlice(sliceNr);
     }
    
    public static void main(String s[]) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        
        SwingUtilities.invokeLater(
            new Runnable(){
                public void run()
                {
                    try
                    {
                        
                        UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
                            
                    } catch ( Exception e )
                    {
                        e.printStackTrace();
                    }
                }
            }
        );

        try {
            VTK.init();
            
            PropertyConfigurator.configureAndWatch("log4j.properties", 6000);
            
            final SliceViewer viewer1 = new SliceViewer(ImagePlane.PLANE_TRANSVERSE);
            //viewer1.setLocation(300, 100);
            viewer1.setLocationRelativeTo(null);
            viewer1.setVisible(true);
            
            /*final SliceViewer viewer2 = new SliceViewer(ImagePlane.PLANE_CORONAL);
            viewer2.setLocation(300, 500);
            viewer2.setVisible(true);
            
            final SliceViewer viewer3 = new SliceViewer(ImagePlane.PLANE_SAGITTAL);
            viewer3.setLocation(700, 500);
            viewer3.setVisible(true);*/
            
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    viewer1.startTimers();
                    //viewer2.startTimers();
                    //viewer3.startTimers();
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
  }

    
}


