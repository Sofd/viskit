package de.sofd.viskit.image3D.control;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.*;
import java.util.*;

import javax.swing.*;

import org.apache.log4j.*;
import org.dcm4che2.data.*;

import de.sofd.util.*;
import de.sofd.viskit.image.*;
import de.sofd.viskit.image3D.jogl.control.*;
import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.jogl.view.*;
import de.sofd.viskit.image3D.model.*;
import de.sofd.viskit.image3D.view.*;
import de.sofd.viskit.test.image3D.jogl.*;
import de.sofd.viskit.util.*;

public class VolumeViewStartController implements ActionListener
{
    static final Logger logger = Logger.getLogger( VolumeViewStartController.class );
    
    protected VolumeConfig volumeConfig;
    protected VolumeConfigFrame volumeConfigFrame;
    
    public VolumeViewStartController(VolumeConfigFrame volumeConfigFrame, VolumeConfig volumeConfig)
    {
        this.volumeConfigFrame = volumeConfigFrame;
        this.volumeConfig = volumeConfig;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        closeConfigFrame();
        startViewers();
    }

    private void closeConfigFrame() {
        volumeConfigFrame.dispose();
    }

    private void startViewers() {
        SwingUtilities.invokeLater( new Runnable()
        {
            public void run()
            {
                try
                {
                
                    /** VolumeObject erstellen */
                    VolumeBasicConfig basicConfig = volumeConfig.getBasicConfig();
                    ArrayList<DicomObject> dicomList = DicomInputOutput.readDir( basicConfig.getImageDirectory(), null, basicConfig.getImageStart(), basicConfig.getImageEnd(), basicConfig.getImageStride() );
                    ShortBuffer dataBuf = DicomUtil.getFilledShortBuffer( dicomList );
                    ShortRange range = ImageUtil.getRange( dataBuf );
                    ShortBuffer windowing = DicomUtil.getWindowing( dicomList, range );
                    VolumeObject volumeObject = new VolumeObject( dicomList, windowing, dataBuf, volumeConfig, range );
                    
                    //liste wird von hier an nicht mehr benötigt
                    dicomList = null;
                    
                    /** Resourcen laden */
                    LutController.init(volumeObject.getVolumeConfig().getProperties().getProperty("volumeConfig.transfer.dir"));
                    LutController.loadFiles();
                                
                    /** SliceViewer erstellen */
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
                        
                    /** VolumeViewer erstellen */
                    final GPUVolumeViewer volumeViewer = new GPUVolumeViewer( volumeObject, sliceCanvas.getContext() );
                    volumeViewer.setLocation( x2, y2 );
                    volumeViewer.setVisible( true );
                    GPUVolumeView volumeView = volumeViewer.getVolumeView();
                    
                    /** TransferFrame erstellen */
                    TransferController transferController
                        = new TransferController( sliceCanvas, volumeView, volumeObject );
                    final TransferFrame transferFrame = new TransferFrame( volumeObject, transferController );
                    transferFrame.setLocationRelativeTo( null );
                    transferFrame.setVisible( true );
                    transferController.setTransferFrame( transferFrame );
                    sliceCanvas.getSliceViewController().setTransferFrame( transferFrame );
                    
                    /** VolumeControlFrame erstellen */
                    VolumeController volumeController = new VolumeController( volumeView, volumeObject );
                    final VolumeControlFrame volumeControlView = new VolumeControlFrame( volumeObject.getVolumeConfig(), volumeController );
                    volumeControlView.setLocationRelativeTo( null );
                    volumeControlView.setVisible( true );
                    sliceCanvas.getSliceViewController().setVolumeView( volumeView );
                    

                    /** Positionen setzen */                    
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
}