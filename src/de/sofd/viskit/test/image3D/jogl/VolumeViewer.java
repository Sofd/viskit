package de.sofd.viskit.test.image3D.jogl;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

import javax.swing.*;
import javax.swing.event.*;

import org.apache.log4j.*;

import vtk.*;

import com.sun.opengl.util.Animator;

import de.sofd.viskit.image3D.jogl.view.*;
import de.sofd.viskit.image3D.vtk.*;
import de.sofd.viskit.image3D.vtk.util.*;

@SuppressWarnings("serial")
public class VolumeViewer extends JFrame implements ChangeListener 
{
    static final Logger logger = Logger.getLogger(VolumeViewer.class);
    
    protected static Animator animator;
    
    protected VolumeView volumeView;
    
    public VolumeViewer() throws IOException
    {
        super("Volume Viewer");
        
        vtkImageData imageData = DicomReader.readImageData("D:/dicom/serie3");
        imageData.Update();
        int dim[] =  imageData.GetDimensions();
        
        vtkImageGaussianSmooth smooth = new vtkImageGaussianSmooth();
        smooth.SetInput(imageData);
        smooth.Update();
        vtkImageData imageData2 = smooth.GetOutput();
        
        logger.debug("image dimension : " + dim[0] + " " + dim[1] + " " + dim[2] + " " + imageData.GetPointData().GetScalars().GetSize());
        
        volumeView = new VolumeView(imageData); 
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(volumeView, BorderLayout.CENTER);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(200, 500));
        panel.setMaximumSize(new Dimension(200, 500));
        
        JSlider slider1 = new JSlider(10, 1000, 100);
        slider1.setName("slices");
        slider1.addChangeListener(this);
        panel.add(slider1, BorderLayout.NORTH);
        
        JSlider slider2 = new JSlider(0, 100, 50);
        slider2.setName("alpha");
        slider2.addChangeListener(this);
        panel.add(slider2, BorderLayout.SOUTH);
        
        getContentPane().add(panel, BorderLayout.EAST);
        
        setSize(700, 500);
        setLocationRelativeTo(null);
        
        animator = new Animator(volumeView);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
              // Run this on another thread than the AWT event queue to
              // make sure the call to Animator.stop() completes before
              // exiting
              new Thread(new Runnable() {
                  public void run() {
                    animator.stop();
                    System.exit(0);
                  }
                }).start();
            }
          });
    }
    
    public static void main(String args[])
    {
        try {
            VTK.init();
            
            VolumeViewer volumeViewer = new VolumeViewer();
            
            volumeViewer.setVisible(true);
            animator.start();
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        } 
        
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JSlider slider = (JSlider)e.getSource();
        
        if ( "slices".equals(slider.getName()))
        {
            volumeView.setSlices(slider.getValue());
        }
        else if ( "alpha".equals(slider.getName()))
        {
            volumeView.setAlpha(slider.getValue()/100.0f);
        }
            
        
    }
}