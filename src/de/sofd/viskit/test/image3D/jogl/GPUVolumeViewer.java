package de.sofd.viskit.test.image3D.jogl;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.*;
import java.util.*;

import javax.media.opengl.*;
import javax.swing.*;

import org.apache.log4j.*;
import org.dcm4che2.data.*;

import de.sofd.util.*;
import de.sofd.viskit.image.*;
import de.sofd.viskit.image3D.control.*;
import de.sofd.viskit.image3D.jogl.control.*;
import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.jogl.view.*;
import de.sofd.viskit.image3D.view.*;
import de.sofd.viskit.util.*;

@SuppressWarnings( "serial" )
public class GPUVolumeViewer extends JFrame implements MouseListener
{
    static final Logger logger = Logger.getLogger( GPUVolumeViewer.class );
    
    protected static int height = 500;

    protected static int width = 700;

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

        SwingUtilities.invokeLater( new Runnable()
        {
            public void run()
            {
                try
                {
                
                    int zStride = 1;
                    ArrayList<DicomObject> dicomList = DicomInputOutput.readDir( "/home/oliver/dicom/series1", null, zStride );
                    //ArrayList<DicomObject> dicomList = DicomInputOutput.readDir( "/home/oliver/dicom/1578", null, Integer.parseInt(System.getProperty("de.sofd.viskit.image3d.sliceStart")), Integer.parseInt(System.getProperty("de.sofd.viskit.image3d.sliceCount")), Integer.parseInt(System.getProperty("de.sofd.viskit.image3d.sliceStride")) );
                    //ArrayList<DicomObject> dicomList = DicomInputOutput.readDir( "/home/oliver/dicom/1578", null, Integer.parseInt(System.getProperty("de.sofd.viskit.image3d.sliceStride")) );

                    ShortBuffer dataBuf = DicomUtil.getFilledShortBuffer( dicomList );
                    ShortRange range = ImageUtil.getRange( dataBuf );
                    
                    ShortBuffer windowing = DicomUtil.getWindowing( dicomList, range );
                    
                    VolumeObject volumeObject = new VolumeObject( dicomList, windowing, dataBuf, null, range );
                    
                    dicomList = null;
                                        
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    int xSpace = (int)( screenSize.getWidth() - SliceViewer.getWinWidth() - GPUVolumeViewer
                            .getWinWidth() );

                    int x1 = xSpace / 3;
                    int y1 = (int)( screenSize.getHeight() - SliceViewer.getWinHeight() ) / 2;
                    int x2 = x1 + SliceViewer.getWinWidth() + xSpace / 3;
                    int y2 = (int)( screenSize.getHeight() - GPUVolumeViewer.getWinHeight() ) / 2;

                    final SliceViewer sliceViewer = new SliceViewer(volumeObject);
                    sliceViewer.setLocation( x1, y1 );
                    sliceViewer.setVisible( true );
                    
                    SliceCanvas sliceCanvas = sliceViewer.getSliceCanvas();
                                        
                    final GPUVolumeViewer volumeViewer = new GPUVolumeViewer( volumeObject, sliceCanvas.getContext() );
                    volumeViewer.setLocation( x2, y2 );
                    volumeViewer.setVisible( true );
                    
                    GPUVolumeView volumeView = volumeViewer.getVolumeView();
                    
                    TransferController transferController
                        = new TransferController( sliceCanvas, volumeView, volumeObject );
                    
                    final TransferFrame transferFrame = new TransferFrame( volumeObject, transferController );
                    transferFrame.setLocationRelativeTo( null );
                    transferFrame.setVisible( true );
                    
                    transferController.setTransferFrame( transferFrame );
                    sliceCanvas.getSliceViewController().setTransferFrame( transferFrame );
                    
                    VolumeController volumeController = new VolumeController( volumeView, volumeObject );
                    
                    final VolumeControlView volumeControlView = new VolumeControlView( volumeObject, volumeController );
                    volumeControlView.setLocationRelativeTo( null );
                    volumeControlView.setVisible( true );
                    
                    sliceCanvas.getSliceViewController().setVolumeView( volumeView );
                    
//                    FilterKernel3D filter = FilterUtil.getGaussFilter( 3 );
//                    filter.normalize();
//                    
//                    System.out.println("gauss filter : " + filter);
                                        
                    int ySpace = (int)(screenSize.getHeight() - GPUVolumeViewer.getWinHeight() - transferFrame.getWidth());
                    y2 = ySpace / 3;
                    int y3 = 2 * ySpace / 3 + GPUVolumeViewer.getWinHeight();
                    
                    volumeViewer.setLocation( x2, y2 );
                    transferFrame.setLocation( x2, y3 );    
                    
                    int x3 = x2 + transferFrame.getWidth();
                    volumeControlView.setLocation( x3, y3 );
                    
                    volumeViewer.getVolumeView().requestFocus();
                }
                catch ( IOException e )
                {
                    logger.error( e );
                    e.printStackTrace();
                }
                catch ( Exception e )
                {
                    logger.error( e );
                    e.printStackTrace();
                }

            }
        } );

    }

    protected GPUVolumeView volumeView;

    public GPUVolumeViewer( VolumeObject sharedVolumeObject, GLContext sharedContext ) throws NumberFormatException, Exception
    {
        super( "Volume Viewer" );

        setBackground( Color.BLACK );

        VolumeObject volumeObject = sharedVolumeObject;
        
        volumeView = new GPUVolumeView( volumeObject, sharedContext );

        getContentPane().setLayout( new BorderLayout() );
        getContentPane().add( volumeView, BorderLayout.CENTER );
                
        setSize( width, height );
        setLocationRelativeTo( null );

        addWindowListener( new DefaultWindowAdapter(this) );
        getContentPane().addMouseListener( this );

    }

    public GPUVolumeView getVolumeView()
    {
        return volumeView;
    }

    @Override
    public void mouseClicked( MouseEvent e )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered( MouseEvent e )
    {
        this.requestFocus();
        //volumeView.getAnimator().start();

    }

    @Override
    public void mouseExited( MouseEvent e )
    {
        //volumeView.getAnimator().stop();

    }

    @Override
    public void mousePressed( MouseEvent e )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseReleased( MouseEvent e )
    {
        // TODO Auto-generated method stub

    }

}