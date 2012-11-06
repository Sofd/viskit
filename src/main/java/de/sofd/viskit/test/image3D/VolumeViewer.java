package de.sofd.viskit.test.image3D;

import de.sofd.swing.*;
import de.sofd.viskit.image.*;
import de.sofd.viskit.image3D.model.*;
import de.sofd.viskit.image3D.vtk.VTK;
import de.sofd.viskit.image3D.vtk.view.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.apache.log4j.*;
import org.dcm4che2.data.*;

@SuppressWarnings("serial")
public class VolumeViewer extends JFrame implements ChangeListener, ActionListener {
    static final Logger logger = Logger.getLogger(VolumeViewer.class);
    
    public static void main(String s[]) {
        try {
            VTK.init();
            
            final VolumeViewer viewer = new VolumeViewer();
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
  }
        
    protected VolumeView volumeView;
    
    public VolumeViewer() throws Exception
    {
        super("VolumeViewer");
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setSize(700, 500);
        
        VolumeConfig volumeConfig = DicomInputOutput.readVolumeConfig();
        VolumeBasicConfig basicConfig = volumeConfig.getBasicConfig();
        ArrayList<DicomObject> dicomList = DicomInputOutput.readDir( basicConfig.getImageDirectory(), null, basicConfig.getImageStart(), basicConfig.getImageEnd(), basicConfig.getImageStride() );
        
        volumeView = new VolumeView(dicomList);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(volumeView, BorderLayout.CENTER);
        panel.add(getControlPanel(), BorderLayout.EAST);
                
        getContentPane().add("Center", panel);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        
        if ("Image smoothing".equals(cmd)) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            volumeView.setImageSmoothing(checkBox.isSelected());
            volumeView.reconnectFilters();
        } 
        
    }
    
    private JPanel getCheckboxPanel(String name, boolean selected) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        panel.setPreferredSize(new Dimension(200, 30));
        panel.setMaximumSize(new Dimension(200, 30));
        
        JCheckBox checkbox = new JCheckBox(name, selected);
        checkbox.addActionListener(this);
        
        panel.add(checkbox);
        
        return panel;
    }
    
    private JPanel getControlPanel() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(200, 500));
        panel.setMaximumSize(new Dimension(200, 500));
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        
        double[] range = volumeView.getImageData().GetScalarRange();
        System.out.println("range " + range[0] + " " + range[1]);
        
        panel.add(getSliderPanel("Center : ", (int)range[0], (int)range[1], volumeView.getWindowingCenter(), 25));
        panel.add(Box.createVerticalStrut(10));
        panel.add(getSliderPanel("Width : ", 0, (int)(range[1]-range[0]), volumeView.getWindowingWidth(), 25));
        panel.add(Box.createVerticalStrut(10));
        panel.add(getSliderPanel("Sample Dist : ", 1, 200, (int)(volumeView.getSampleDist()), 1));
        panel.add(Box.createVerticalStrut(10));
        panel.add(getSliderPanel("Sample Dist Final : ", 1, 200, (int)(volumeView.getSampleDistFinal()), 1));
        panel.add(Box.createVerticalStrut(10));
        
        panel.add(getCheckboxPanel("Image smoothing", volumeView.isImageSmoothing()));
        
        panel.add(Box.createVerticalGlue());
        
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        return panel;
    }
    
    private JPanel getSliderPanel(String name, int min, int max, int value, int stepSize) {
        JSliderWithSpinnerPanel panel = new JSliderWithSpinnerPanel(name, min, max, value, stepSize, this);
        panel.setPreferredSize(new Dimension(200, 45));
        panel.setMaximumSize(new Dimension(200, 45));
        
        panel.getSlider().addChangeListener(this);
        panel.getSpinner().addChangeListener(this);
        
        return panel;
    }
    
    public void startTimers()
    {
        volumeView.startTimer();
    }
        
    @Override
    public void stateChanged(ChangeEvent e) {
        Component source = (Component)e.getSource();
        
        JSlider slider = (JSlider)source;
        
        if ( "Center : ".equals(source.getName()))
        {
            volumeView.setWindowingCenter(slider.getValue());
            volumeView.updateTransfer();
        }
        else if ( "Width : ".equals(source.getName()))
        {
            volumeView.setWindowingWidth(slider.getValue());
            volumeView.updateTransfer();
        }
        else if ( "Sample Dist : ".equals(source.getName()))
        {
            volumeView.setSampleDist(slider.getValue());
            volumeView.updateSampleDist();
        }
        else if ( "Sample Dist Final : ".equals(source.getName()))
        {
            volumeView.setSampleDistFinal(slider.getValue());
            volumeView.updateSampleDistFinal();
        }
        
        volumeView.setFinalRendering(!slider.getValueIsAdjusting());
        volumeView.updateMapper();
        
    }
    
    

    
}


