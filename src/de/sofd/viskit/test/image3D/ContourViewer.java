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
public class ContourViewer extends JFrame implements ChangeListener, ActionListener {
    static final Logger logger = Logger.getLogger(ContourViewer.class);
    
    public static void main(String s[]) {
        try {
            VTK.init();
            
            final ContourViewer viewer = new ContourViewer();
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
        
    protected ContourView contourView;
    
    public ContourViewer() throws Exception
    {
        super("ContourViewer");
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setSize(700, 500);
        
        VolumeConfig volumeConfig = DicomInputOutput.readVolumeConfig();
        VolumeBasicConfig basicConfig = volumeConfig.getBasicConfig();
        ArrayList<DicomObject> dicomList = DicomInputOutput.readDir( basicConfig.getImageDirectory(), null, basicConfig.getImageStart(), basicConfig.getImageEnd(), basicConfig.getImageStride() );
        
        contourView = new ContourView(dicomList);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(contourView, BorderLayout.CENTER);
        panel.add(getControlPanel(), BorderLayout.EAST);
                
        getContentPane().add("Center", panel);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        
        if ( "Image smoothing".equals(cmd)) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            contourView.setImageSmoothing(checkBox.isSelected());
            contourView.reconnectFilters();
        } else if ( "Decimation".equals(cmd))    {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            contourView.setDecimating(checkBox.isSelected());
            contourView.reconnectFilters();
        } else if ( "Mesh smoothing".equals(cmd)) {
            JCheckBox checkBox = (JCheckBox)e.getSource();
            contourView.setMeshSmoothing(checkBox.isSelected());
            contourView.reconnectFilters();
        } else if ( "resample".equals(cmd) ) {
            JTextField textField = (JTextField)e.getSource();
            try {
                contourView.setResampleWidth(Integer.parseInt(textField.getText()));
                contourView.updateResampler();
            } catch (Exception ex) {
                
            }
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
    
    
    private JPanel getSliderPanel(String name, int min, int max, int value, int stepSize) {
        JSliderWithSpinnerPanel panel = new JSliderWithSpinnerPanel(name, min, max, value, stepSize, this);
        panel.setPreferredSize(new Dimension(200, 45));
        panel.setMaximumSize(new Dimension(200, 45));
        
        panel.getSlider().addChangeListener(this);
        panel.getSpinner().addChangeListener(this);
        
        return panel;
    }
    
    private JPanel getControlPanel() {
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(200, 500));
        panel.setMaximumSize(new Dimension(200, 500));
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        
        double[] range = contourView.getImageData().GetScalarRange();
        System.out.println("range " + range[0] + " " + range[1]);
        
        panel.add(getSliderPanel("Contour Level : ", (int)range[0], (int)range[1]+300, 500, 25));
        panel.add(Box.createVerticalStrut(15));
        panel.add(getCheckboxPanel("Image smoothing", true));
        panel.add(getCheckboxPanel("Decimation", false));
        panel.add(getCheckboxPanel("Mesh smoothing", false));
        panel.add(getResamplePanel());
        panel.add(Box.createVerticalGlue());
        
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        return panel;
    }
    
    private JPanel getResamplePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setSize(new Dimension(200, 30));
        panel.setMaximumSize(new Dimension(200, 30));
        
        JTextField textField = new JTextField(""+contourView.getResampleWidth());
        textField.setActionCommand("resample");
        textField.addActionListener(this);
        
        panel.add(new JLabel("Resample width : "), BorderLayout.WEST);
        panel.add(textField, BorderLayout.CENTER);
        
        return panel;
    }
    
    public void startTimers()
    {
        contourView.startTimer();
    }
        
    @Override
    public void stateChanged(ChangeEvent e) {
        Component source = (Component)e.getSource();
        
        if ( "Contour Level : ".equals(source.getName()) && source instanceof JSlider )
        {
            JSlider slider = (JSlider)source;
            
            if ( ! slider.getValueIsAdjusting()) {
                contourView.updateContourLevel(slider.getValue());
            }
        }
        else if ( "Contour Level : ".equals(source.getName()) && source instanceof JSpinner )
        {
            JSpinner spinner = (JSpinner)source;
            
            int value = ((SpinnerNumberModel)spinner.getModel()).getNumber().intValue();
            
            contourView.updateContourLevel(value);
        }
    }
    
    

    
}

