package de.sofd.viskit.test.image3D.jogl;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.*;

import javax.swing.*;

import org.apache.log4j.*;

import vtk.*;

import com.sun.opengl.util.Animator;

import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.jogl.util.*;
import de.sofd.viskit.image3D.jogl.view.*;
import de.sofd.viskit.image3D.vtk.*;
import de.sofd.viskit.image3D.vtk.util.*;

@SuppressWarnings("serial")
public class SliceViewer extends JFrame  
{
    static final Logger logger = Logger.getLogger(SliceViewer.class);
    
    protected static Animator animator;
    
    protected SliceView sliceView;
    
    public SliceViewer() throws Exception
    {
        super("Slice Viewer");
        
        setBackground(Color.BLACK);
        
        vtkImageData imageData = DicomReader.readImageDataFromDir("D:/dicom/serie3");
        imageData.Update();
        
        FloatBuffer dataBuf = Vtk2GL.getFilledFloatBuffer(imageData);
        VolumeObject volumeObject = new VolumeObject(imageData, dataBuf);        
        sliceView = new SliceView(volumeObject); 
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(Color.BLACK);
        getContentPane().add(sliceView, BorderLayout.CENTER);
        
        //sliceView.setSize(volumeObject.getMaxDim()*2, volumeObject.getMaxDim()*2);
        /*sliceView.setPreferredSize(new Dimension(volumeObject.getMaxDim()*2, volumeObject.getMaxDim()*2));
        sliceView.setMaximumSize(new Dimension(volumeObject.getMaxDim()*2, volumeObject.getMaxDim()*2));
        sliceView.setMinimumSize(new Dimension(volumeObject.getMaxDim()*2, volumeObject.getMaxDim()*2));*/
        
        //this.setSize( Toolkit.getDefaultToolkit().getScreenSize() );
        this.setSize( new Dimension(600, 600) );
                
        setLocationRelativeTo(null);
        
        animator = new Animator(sliceView);
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
              System.exit(0);
            }
          });
    }
    
    public SliceView getSliceView() {
        return sliceView;
    }

    public static void main(String args[])
    {
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
            
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    try {
                        SliceViewer sliceViewer = new SliceViewer();
                        sliceViewer.setVisible(true);
                        sliceViewer.getSliceView().requestFocus();
                        animator.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //sliceViewer.pack();
                }
            });
        } catch (IOException e) {
            logger.error(e);
            e.printStackTrace();
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        } 
        
    }

    
}