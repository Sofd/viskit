package de.sofd.viskit.image3D.jogl.control;

import de.sofd.viskit.image3D.jogl.view.*;
import java.awt.event.*;

public class VolumeWindowAdapter extends WindowAdapter
{
    protected SliceCanvas sliceCanvas;
    protected GPUVolumeView volumeView;
    
    public VolumeWindowAdapter( SliceCanvas sliceCanvas, GPUVolumeView volumeView )
    {
        super();
        this.sliceCanvas = sliceCanvas;
        this.volumeView = volumeView;
    }

    @Override
    public void windowClosing( WindowEvent e )
    {
        if ( sliceCanvas != null ) sliceCanvas.cleanUp();
        if ( volumeView != null ) volumeView.cleanUp();
        
        System.exit( 0 );
    }
}