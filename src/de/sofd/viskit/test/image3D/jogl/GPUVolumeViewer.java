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
public class GPUVolumeViewer extends JFrame implements ChangeListener 
{
    static final Logger logger = Logger.getLogger(GPUVolumeViewer.class);
    
    protected static Animator animator;
    
    protected GPUVolumeView volumeView;
    
    public GPUVolumeViewer() throws IOException
    {
        super("Volume Viewer");
        
        vtkImageData imageData = DicomReader.readImageData("D:/dicom/serie3");
        imageData.Update();
        int dim[] =  imageData.GetDimensions();
        
        /*vtkImageGaussianSmooth smooth = new vtkImageGaussianSmooth();
        smooth.SetInput(imageData);
        smooth.Update();
        vtkImageData imageData2 = smooth.GetOutput();*/
        
        logger.debug("image dimension : " + dim[0] + " " + dim[1] + " " + dim[2] + " " + imageData.GetPointData().GetScalars().GetSize());
        
        volumeView = new GPUVolumeView(imageData); 
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(volumeView, BorderLayout.CENTER);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setPreferredSize(new Dimension(200, 500));
        panel.setMaximumSize(new Dimension(200, 500));
        
        panel.add(getSliderPanel("sliceStep", 1, 1000, 220, 200, 40));
        panel.add(getSliderPanel("alpha", 0, 100, 50, 20, 4));
        panel.add(getSliderPanel("bias", 0, 100, 90, 20, 4));
        
        panel.add(getSliderPanel("yLevel", 0, 100, 100, 20, 4));
        panel.add(getSliderPanel("zLevelMin", 0, 100, 0, 20, 4));
        panel.add(getSliderPanel("zLevelMax", 0, 100, 100, 20, 4));
        
        panel.add(Box.createVerticalGlue());
        
        getContentPane().add(panel, BorderLayout.EAST);
        
        setSize(700, 500);
        setLocationRelativeTo(null);
        
        animator = new Animator(volumeView);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
              System.exit(0);
            }
          });
    }
    
    private JPanel getSliderPanel(String name, int min, int max, int value, int major, int minor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(200, 50));
        panel.setMaximumSize(new Dimension(200, 50));
        JSlider slider = new JSlider(min, max, value);
        
        slider.setName(name);
        slider.addChangeListener(this);
        slider.setMajorTickSpacing(major);
        slider.setMinorTickSpacing(minor);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        slider.setSnapToTicks(true);
        
        panel.add(new JLabel(name), BorderLayout.WEST);
        panel.add(slider, BorderLayout.CENTER);
        
        return panel;
    }

    public GPUVolumeView getVolumeView() {
        return volumeView;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        JSlider slider = (JSlider)e.getSource();
        
        if ( "sliceStep".equals(slider.getName()))
        {
            volumeView.setSliceStep(1.0f/slider.getValue());
        }
        else if ( "alpha".equals(slider.getName()))
        {
            volumeView.setAlpha(slider.getValue()/25.0f);
        }
        else if ( "bias".equals(slider.getName()))
        {
            volumeView.setBias(slider.getValue()/100.0f);
        }
        else if ( "yLevel".equals(slider.getName()))
        {
            volumeView.setYLevel(slider.getValue()/100.0f);
        }
        else if ( "zLevelMin".equals(slider.getName()))
        {
            volumeView.setZLevelMin(slider.getValue()/100.0f);
        }
        else if ( "zLevelMax".equals(slider.getName()))
        {
            volumeView.setZLevelMax(slider.getValue()/100.0f);
        }    
        
    }
    
    public static void main(String args[])
    {
        try {
            VTK.init();
            
            final GPUVolumeViewer volumeViewer = new GPUVolumeViewer();
            
            SwingUtilities.invokeAndWait(new Runnable()
            {
                public void run()
                {
                    volumeViewer.setVisible(true);
                }
            });
            
            volumeViewer.getVolumeView().requestFocus();
            animator.start();
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        } 
        
    }

    
}