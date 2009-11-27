package de.sofd.viskit.test.image3D.jogl;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;

import javax.swing.*;
import javax.swing.event.*;

import org.apache.log4j.*;

import vtk.*;

import com.sun.opengl.util.Animator;

import de.sofd.viskit.image3D.jogl.view.*;
import de.sofd.viskit.image3D.vtk.*;
import de.sofd.viskit.image3D.vtk.util.*;

@SuppressWarnings("serial")
public class ARBSliceViewer extends JFrame implements ChangeListener
{
    static final Logger logger = Logger.getLogger(ARBSliceViewer.class);
    
    protected static Animator animator;
    
    protected ARBSliceView sliceView;
    
    protected vtkImageData imageData;
    protected vtkImageGaussianSmooth smooth;
    
    public ARBSliceViewer() throws IOException
    {
        super("Slice Viewer");
        
        imageData = DicomReader.readImageData("D:/dicom/serie3");
        imageData.Update();
        int dim[] =  imageData.GetDimensions();
        
        double[] range = imageData.GetScalarRange();
        double rangeDist = range[1] - range[0];
        rangeDist = ( rangeDist > 0 ? rangeDist : 1 );
        
        logger.debug("image dimension : " + dim[0] + " " + dim[1] + " " + dim[2] + " " + imageData.GetPointData().GetScalars().GetSize());
        
        smooth = new vtkImageGaussianSmooth();
        smooth.SetInput(imageData);
        
        sliceView = new ARBSliceView(smooth.GetOutput()); 
        
        getContentPane().setLayout(new BorderLayout());
        
        JPanel slicePanel = new JPanel(new BorderLayout());
        
        slicePanel.add(sliceView, BorderLayout.CENTER);
        slicePanel.add(getSlider("sliceLevel", 1, dim[2], 1), BorderLayout.SOUTH);
        getContentPane().add(slicePanel, BorderLayout.CENTER);
        
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.PAGE_AXIS));
        controlPanel.add(getSliderPanel("windowCenter", (int)range[0], (int)range[1], 325));
        controlPanel.add(getSliderPanel("windowWidth", (int)range[0], (int)range[1], 581));
        controlPanel.add(Box.createVerticalGlue());
        getContentPane().add(controlPanel, BorderLayout.EAST);
                
        setSize(700, 550);
        setLocationRelativeTo(null);
        
        animator = new Animator(sliceView);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
              System.exit(0);
            }
          });
    }
    
    private JSlider getSlider(String name, int min, int max, int value) {
        JSlider slider = new JSlider(min, max, value);
        
        slider.setName(name);
        slider.addChangeListener(this);
        
        return slider;
    }

    public ARBSliceView getSliceView() {
        return sliceView;
    }
    
    private JPanel getSliderPanel(String name, int min, int max, int value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(200, 50));
        panel.setMaximumSize(new Dimension(200, 50));
        JSlider slider = new JSlider(min, max, value);
        
        slider.setName(name);
        slider.addChangeListener(this);
        
        panel.add(new JLabel(name), BorderLayout.WEST);
        panel.add(slider, BorderLayout.CENTER);
        
        return panel;
    }
    
    

    @Override
    public void stateChanged(ChangeEvent e) {
        JSlider slider = (JSlider)e.getSource();
        
        if ( "sliceLevel".equals(slider.getName()))
        {
            sliceView.setCurrentSlice(slider.getValue());
        }
        if ( "windowCenter".equals(slider.getName()))
        {
            sliceView.setWindowCenter((slider.getValue()));
            //logger.info("windowCenter : " + slider.getValue());
        }
        if ( "windowWidth".equals(slider.getName()))
        {
            sliceView.setWindowWidth(slider.getValue());
            //logger.info("windowWidth : " + slider.getValue());
        }
        
    }
    
    public static void main(String args[])
    {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        
        
        try {
            SwingUtilities.invokeAndWait(new Runnable()
            {
                public void run()
                {
                    try {
                        
                        UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
                        
                        VTK.init();
                        
                        final ARBSliceViewer sliceViewer = new ARBSliceViewer();
                        
                        sliceViewer.setVisible(true);
                        
                        sliceViewer.getSliceView().requestFocus();
                    
                    } catch (IOException e) {
                        logger.error(e);
                        e.printStackTrace();
                    } catch (Exception e) {
                        logger.error(e);
                        e.printStackTrace();
                    } 
                }
            });
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        animator.start();
        
        
    }

    

    
}