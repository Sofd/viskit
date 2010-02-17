package de.sofd.viskit.test.image3D;

import javax.swing.*;
import javax.swing.event.*;

import org.apache.log4j.*;
import org.dcm4che2.data.*;

import vtk.*;

import de.sofd.util.*;
import de.sofd.viskit.image.*;
import de.sofd.viskit.image3D.model.*;
import de.sofd.viskit.image3D.vtk.VTK;
import de.sofd.viskit.image3D.vtk.util.*;
import de.sofd.viskit.image3D.vtk.view.*;
import de.sofd.viskit.util.*;

import java.awt.*;
import java.awt.event.*;
import java.nio.*;
import java.util.*;

/**
 * This example reads a volume dataset, extracts two isosurfaces that
 * represent the skin and bone, and then displays them.
 */
@SuppressWarnings("serial")
public class Dicom3DViewer extends JFrame implements ChangeListener, ActionListener {
    static final Logger logger = Logger.getLogger(Dicom3DViewer.class);
    
    protected Dicom3DView dicom3DView;
    protected vtkImageData imageData;
    protected vtkImageGaussianSmooth smooth;
    
    protected vtkShortArray dataArray=null;
    protected short[] shorts;
    
    public Dicom3DViewer() throws Exception
    {
        super("Dicom3D");
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        
//        Collection<DicomObject> dicomList
//            = DicomInputOutput.readDir("D:/dicom/serie7", "1.2.840.113619.2.135.2025.3758242.5289.1206919099.647");
        
        
//        Collection<DicomObject> dicomList
//            = DicomInputOutput.readDir("D:/dicom/1578", "2x.16.756.5.23.5012.70.3563.3.20090827121711.3767977317");
        
        //imageData = DicomReader.readImageDataFromDir("/home/oliver/dicom/series1");
        //imageData = DicomReader.readImageDataFromDir("/home/oliver/Desktop/oliver/dicom/INCISIX");
        
        VolumeConfig volumeConfig = DicomInputOutput.readVolumeConfig();
        VolumeBasicConfig basicConfig = volumeConfig.getBasicConfig();
        ArrayList<DicomObject> dicomList = DicomInputOutput.readDir( basicConfig.getImageDirectory(), null, basicConfig.getImageStart(), basicConfig.getImageEnd(), basicConfig.getImageStride() );
        
        ShortBuffer dataBuf = DicomUtil.getFilledShortBuffer( dicomList );
        
        long time1 = System.currentTimeMillis();
        
        dataArray = new vtkShortArray();
        shorts = new short[dataBuf.capacity()];
        dataBuf.get(shorts);
        System.out.println("capacity : " + dataBuf.capacity());
        
        imageData = new vtkImageData();
        imageData.SetScalarTypeToShort();
        imageData.SetNumberOfScalarComponents(1);
        System.out.println("dims " + basicConfig.getPixelWidth() + " " + basicConfig.getPixelHeight() + " " + basicConfig.getSlices());
        imageData.SetDimensions(basicConfig.getPixelWidth(), basicConfig.getPixelHeight(), basicConfig.getSlices());
        //imageData.SetDimensions(200, 400, 320);
        
        imageData.SetSpacing(basicConfig.getSpacing().toDoubleArray());
        
        imageData.SetOrigin(0, 0, 0);
        //imageData.AllocateScalars();
        
        dataArray.SetJavaArray(shorts);
        System.out.println("array size " + dataArray.GetDataSize());
        
        imageData.GetPointData().SetScalars(dataArray);
        //imageData.GetPointData().CopyAllOn(3);
        
        imageData.Update();
        imageData.UpdateData();
        
        long time2 = System.currentTimeMillis();
        System.out.println("conversion time : " + (time2-time1) + " ms");
        int dim[] =  imageData.GetDimensions();
        
        logger.debug("image dimension : " + dim[0] + " " + dim[1] + " " + dim[2] + " " + imageData.GetPointData().GetScalars().GetSize());
        
        double[] range = imageData.GetScalarRange();
        System.out.println("range " + range[0] + " " + range[1]);
        
        //double factor = Math.min(256.0*256.0*256.0/(dim[0]*dim[1]*dim[2]), 1);
        //double factor = 0.5;
        
        smooth = new vtkImageGaussianSmooth();
        smooth.SetInput(imageData);
        
        /*vtkImageResample resample = new vtkImageResample();
        resample.SetDimensionality(3);
        resample.InterpolateOn();
        resample.SetAxisMagnificationFactor(0, Math.min(150.0/dim[0], 1));
        resample.SetAxisMagnificationFactor(1, Math.min(150.0/dim[1], 1));
        resample.SetAxisMagnificationFactor(2, Math.min(150.0/dim[2], 1));
        resample.SetInput(smooth.GetOutput());*/
        
        dicom3DView = new Dicom3DView(smooth.GetOutput());
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(dicom3DView, BorderLayout.CENTER);
        panel.add(getControlPanel(), BorderLayout.EAST);
                
        getContentPane().add("Center", panel);
        
        
        pack();
    }
    
    private JPanel getControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        
        double[] range = imageData.GetScalarRange();
        System.out.println("range " + range[0] + " " + range[1]);
        
        panel.add(getSliderPanel("Contour Level", (int)range[0], (int)range[1]+300, 250));
        panel.add(getCheckboxPanel("smooth on", true));
        panel.add(getSliderPanel("Smooth radius", 0, 2.0f, 1.0f));
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JPanel getCheckboxPanel(String name, boolean selected) {
        JPanel panel = new JPanel();
        JCheckBox checkbox = new JCheckBox(name, selected);
        checkbox.addActionListener(this);
        panel.add(checkbox);
        return panel;
    }
    
    private JPanel getSliderPanel(String name, float min, float max, float value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(200, 70));
        panel.setMaximumSize(new Dimension(200, 70));
        JSlider slider = new JSlider(0, (int)((max-min)*100), (int)(value*100));
        
        slider.setName("slider:"+name);
        slider.addChangeListener(this);
        
        JPanel panelN = new JPanel(new GridLayout(1, 2));
        panelN.add(new JLabel(name));
        
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, 0.1f));
        spinner.setName("spinner:"+name);
        spinner.addChangeListener(this);
        panelN.add(spinner);
        
        panel.add(panelN, BorderLayout.NORTH);
        panel.add(slider, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel getSliderPanel(String name, int min, int max, int value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(200, 70));
        panel.setMaximumSize(new Dimension(200, 70));
        JSlider slider = new JSlider(min, max, value);
        
        slider.setName("slider:"+name);
        slider.addChangeListener(this);
        slider.setPaintLabels(true);
        
        JPanel panelN = new JPanel(new GridLayout(1, 2));
        panelN.add(new JLabel(name));
        
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, 0, max, 5));
        spinner.setName("spinner:"+name);
        spinner.addChangeListener(this);
        panelN.add(spinner);
        
        panel.add(panelN, BorderLayout.NORTH);
        panel.add(slider, BorderLayout.SOUTH);
        
        return panel;
    }

    public void startTimers()
    {
        dicom3DView.startTimer();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if ( "smooth on".equals(e.getActionCommand()))
        {
            JCheckBox smoothOn = (JCheckBox)e.getSource();
            if ( smoothOn.isSelected())
            {
                smooth.SetInput(imageData);
                dicom3DView.setInput(smooth.GetOutput());
            }
            else
            {
                dicom3DView.setInput(imageData);
            }
        }
        
        
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        Component source = (Component)e.getSource();
        
        if ( "slider:Contour Level".equals(source.getName()))
        {
            JSlider slider = (JSlider)source;
            
            if ( ! slider.getValueIsAdjusting())
                dicom3DView.updateContourLevel(slider.getValue());
        }
        else if ( "spinner:Contour Level".equals(source.getName()))
        {
            JSpinner spinner = (JSpinner)source;
            dicom3DView.updateContourLevel(((SpinnerNumberModel)spinner.getModel()).getNumber().intValue());
        }
        else if ( "slider:Smooth radius".equals(source.getName()))
        {
            JSlider slider = (JSlider)source;
            
            if ( ! slider.getValueIsAdjusting())
            {
                smooth.SetRadiusFactor(slider.getValue()/100);
            }
                
        }
        else if ( "spinner:Smooth radius".equals(source.getName()))
        {
            JSpinner spinner = (JSpinner)source;
            smooth.SetRadiusFactor(((SpinnerNumberModel)spinner.getModel()).getNumber().floatValue());
        }
        
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


