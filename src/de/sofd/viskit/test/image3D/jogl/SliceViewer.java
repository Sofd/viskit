package de.sofd.viskit.test.image3D.jogl;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.nio.*;
import java.util.ArrayList;

import javax.swing.*;

import org.apache.log4j.*;
import org.dcm4che2.data.DicomObject;

import de.sofd.util.*;
import de.sofd.viskit.image.DicomInputOutput;
import de.sofd.viskit.image3D.jogl.control.*;
import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.jogl.view.*;
import de.sofd.viskit.image3D.util.Image3DUtil;
import de.sofd.viskit.util.*;

@SuppressWarnings( "serial" )
public class SliceViewer extends JFrame implements MouseListener 
{
    static final Logger logger = Logger.getLogger( SliceViewer.class );

    public static int getWinHeight()
    {
        return height;
    }

    public static int getWinWidth()
    {
        return width;
    }
    
    public static void main( String args[] )
    {
        JFrame.setDefaultLookAndFeelDecorated( true );
        JDialog.setDefaultLookAndFeelDecorated( true );

        SwingUtilities.invokeLater( new Runnable()
        {
            public void run()
            {
                try
                {

                    UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
            }
        } );

        try
        {
            SwingUtilities.invokeLater( new Runnable()
            {
                public void run()
                {
                    try
                    {
                        SliceViewer sliceViewer = new SliceViewer();
                        
                        sliceViewer.setVisible( true );
                        sliceViewer.getSliceCanvas().requestFocus();
                    }
                    catch ( Exception e )
                    {
                        e.printStackTrace();
                    }
                    // sliceViewer.pack();
                }
            } );
        }
        catch ( Exception e )
        {
            logger.error( e );
            e.printStackTrace();
        }

    }
    
    protected SliceCanvas sliceCanvas;

    protected static int height = 650;
    
    protected static int width = 600;

    public SliceViewer() throws Exception
    {
        super( "Slice Viewer" );

        setBackground( Color.BLACK );

        int zStride = Image3DUtil.getzStride();
        ArrayList<DicomObject> dicomList = DicomInputOutput.readDir( "/home/oliver/dicom/series1", null, zStride );
        //ArrayList<DicomObject> dicomList = DicomInputOutput.readDir( "/home/oliver/Desktop/Laufwerk_D/dicom/1578", null, Integer.parseInt(System.getProperty("de.sofd.viskit.image3d.sliceStart")), Integer.parseInt(System.getProperty("de.sofd.viskit.image3d.sliceCount")), Integer.parseInt(System.getProperty("de.sofd.viskit.image3d.sliceStride")) );
        //ArrayList<DicomObject> dicomList = DicomInputOutput.readDir( "/home/oliver/Desktop/Laufwerk_D/dicom/1578", null, Integer.parseInt(System.getProperty("de.sofd.viskit.image3d.sliceStride")) );

        ShortBuffer dataBuf = DicomUtil.getFilledShortBuffer( dicomList );
        ShortRange range = ImageUtil.getRange( dataBuf );
        ShortBuffer windowing = DicomUtil.getWindowing( dicomList, range );
        
        VolumeObject volumeObject = new VolumeObject( dicomList, windowing, dataBuf, zStride, range );
        sliceCanvas = new SliceCanvas( volumeObject );
        
        getContentPane().setLayout( new BorderLayout() );
        getContentPane().setBackground( Color.BLACK );
        getContentPane().add( sliceCanvas, BorderLayout.CENTER );

        this.setSize( new Dimension( width, height ) );
        //this.setMinimumSize( new Dimension( width, height ) );

        setLocationRelativeTo( null );

        addWindowListener( new DefaultWindowAdapter(this) );
        addMouseListener( this );
        
        GraphicsDevice[] devices
            = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        
        for ( GraphicsDevice device : devices )
        {
            logger.info( "device : " + device.getIDstring() );
            logger.info( device.toString() );
            logger.info( "acc memory : " + device.getAvailableAcceleratedMemory() );
        }

    }

    public SliceCanvas getSliceCanvas()
    {
        return sliceCanvas;
    }

    @Override
    public void mouseClicked( MouseEvent arg0 )
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mouseEntered( MouseEvent arg0 )
    {
        this.requestFocus();
        
    }

    @Override
    public void mouseExited( MouseEvent arg0 )
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mousePressed( MouseEvent arg0 )
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mouseReleased( MouseEvent arg0 )
    {
        // TODO Auto-generated method stub
        
    }

    
}