package de.sofd.viskit.test.image3D.jogl;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.apache.log4j.*;
import org.dcm4che2.data.*;

import com.sun.opengl.util.Animator;

import de.sofd.util.*;
import de.sofd.viskit.image.*;
import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.jogl.view.*;
import de.sofd.viskit.image3D.vtk.*;
import de.sofd.viskit.util.*;

@SuppressWarnings("serial")
public class TexSliceVolumeViewer extends JFrame implements ChangeListener 
{
    static final Logger logger = Logger.getLogger(TexSliceVolumeViewer.class);
    
    protected static Animator animator;
    
    protected TexSliceVolumeView volumeView;
    
    public TexSliceVolumeViewer() throws Exception
    {
        super("Volume Viewer");
        
        setBackground( Color.BLACK );

        ArrayList<DicomObject> dicomList = DicomInputOutput.readDir( "../dicom/series3", null );
        
        //ShortBuffer dataBuf = DicomUtil.getFilledShortBuffer( dicomList );
        ArrayList<ShortBuffer> dataBufList = DicomUtil.getFilledShortBufferList(dicomList);
        
        ShortRange range = ImageUtil.getRange( dataBufList );
        ShortBuffer windowing = DicomUtil.getWindowing( dicomList, range );
        
        VolumeObject volumeObject = new VolumeObject( dicomList, windowing, dataBufList, null, range );
        
        volumeView = new TexSliceVolumeView(volumeObject); 
        
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
                System.exit(0);
            }
          });
    }
    
    public static void main(String args[])
    {
        try {
            VTK.init();
            
            TexSliceVolumeViewer volumeViewer = new TexSliceVolumeViewer();
            
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
            volumeView.setAlpha(slider.getValue()/5.0f);
        }
            
        
    }
}