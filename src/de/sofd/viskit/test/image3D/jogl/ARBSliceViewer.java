package de.sofd.viskit.test.image3D.jogl;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.apache.log4j.*;
import org.dcm4che2.data.*;

import vtk.*;

import com.sun.opengl.util.Animator;

import de.sofd.viskit.image.*;
import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.jogl.view.*;
import de.sofd.viskit.image3D.vtk.*;
import de.sofd.viskit.image3D.vtk.util.*;
import de.sofd.viskit.model.*;
import de.sofd.viskit.util.*;

@SuppressWarnings("serial")
public class ARBSliceViewer extends JFrame implements ChangeListener
{
    static final Logger logger = Logger.getLogger(ARBSliceViewer.class);
    
    protected static Animator animator;
    
    protected ARBSliceView sliceView;
    
    protected vtkImageData imageData;
    protected vtkImageGaussianSmooth smooth;
    
    protected String testfiles[] =
    {
        //"8-bit Uncompressed Gray.dcm",
        
        /* not supported */
        //"8-bit RunLength Gray.dcm",
        //"8-bit JPEG Lossy Gray.dcm",
        //"8-bit JPEG Lossless Gray.dcm",
        //"8-bit J2K Lossy Gray.dcm",
        //"8-bit J2K Lossless Gray.dcm",
        "24-bit Uncompressed Color.dcm",
        //"24-bit RunLength Color.dcm",
        //"24-bit JPEG Lossy Color.dcm",
        //"24-bit JPEG Lossless Color.dcm",
        //"24-bit J2K Lossy Color.dcm",
        //"24-bit J2K Lossless Color.dcm",
        
        //"16-bit Uncompressed Gray.dcm",
        
        /* not supported */    
        //"16-bit RunLength Gray.dcm",
        //"16-bit JPEG Lossless Gray.dcm",
        //"16-bit J2K Lossy Gray.dcm",
        //"16-bit J2K Lossless Gray.dcm"
            
        //"atl-rgb-24bit.dcm",
    };
    
    public ARBSliceViewer() throws IOException
    {
        super("Slice Viewer");
        setBackground( Color.BLACK );

        ArrayList<DicomObject> dicomList = DicomInputOutput.readDir( "/home/oliver/dicom/series1", null );
        //ArrayList<DicomObject> dicomList = DicomInputOutput.readDir( "/home/oliver/Desktop/Laufwerk_D/dicom/1578", null, 400, 100 );
        
//        for ( String testfile : testfiles )
//        {
//            try {
//                System.out.println("read " + testfile);
//                imageData = DicomReader.readImageDataFromFile("D:/dicom/testbilder/" + testfile);
//                imageData.Update();
//                dim =  imageData.GetDimensions();
//                
//                range = imageData.GetScalarRange();
//                double rangeDist = range[1] - range[0];
//                rangeDist = ( rangeDist > 0 ? rangeDist : 1 );
//                
//                System.out.println(testfile + " supported!");
//                logger.info("image dimension : " + dim[0] + " " + dim[1] + " " + dim[2] + " " + imageData.GetPointData().GetScalars().GetSize());
//                logger.info("range : [" + range[0] + "," + range[1] + "]");
//            } catch (Exception e1) {
//                System.err.println(testfile + " not supported!");
//            }
//        }
//        
        
        sliceView = new ARBSliceView(dicomList); 
        
        getContentPane().setLayout(new BorderLayout());
        
        JPanel slicePanel = new JPanel(new BorderLayout());
        slicePanel.setBackground(Color.BLACK);
        slicePanel.add(sliceView, BorderLayout.CENTER);
        slicePanel.add(getSlider("sliceLevel", 1, dicomList.size(), 1), BorderLayout.SOUTH);
        getContentPane().add(slicePanel, BorderLayout.CENTER);
        
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.PAGE_AXIS));
        //controlPanel.add(getSliderPanel("windowCenter", (int)range[0], (int)range[1], (int)range[0]));
        //controlPanel.add(getSliderPanel("windowWidth", (int)range[0], (int)range[1], (int)range[0]));
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
        System.out.println("" + min + ", " + max + ", " + value);
        
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
            System.out.println("slider value : "+slider.getValue());
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