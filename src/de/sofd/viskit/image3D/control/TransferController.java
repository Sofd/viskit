package de.sofd.viskit.image3D.control;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.jogl.view.*;
import de.sofd.viskit.image3D.model.*;
import de.sofd.viskit.image3D.view.*;
import de.sofd.viskit.util.*;

public class TransferController implements ActionListener, ChangeListener
{
    protected SliceCanvas sliceCanvas;

    protected GPUVolumeView volumeView;

    protected VolumeObject volumeObject;

    protected WindowingMode windowingMode;

    protected TransferFrame transferFrame;

    public TransferController( SliceCanvas sliceCanvas, GPUVolumeView volumeView, VolumeObject volumeObject )
    {
        this.sliceCanvas = sliceCanvas;
        this.volumeView = volumeView;
        this.volumeObject = volumeObject;

        this.windowingMode = WindowingMode.WINDOWING_MODE_LOCAL;
    }

    @Override
    public synchronized void actionPerformed( ActionEvent event )
    {
        String cmd = event.getActionCommand();
        System.out.println( "cmd : " + cmd );
        if ( "windowingMode".equals( cmd ) )
        {
            JComboBox comboBox = (JComboBox)event.getSource();
            String mode = (String)comboBox.getSelectedItem();

            System.out.println( "mode : " + mode );

            if ( "Local ( per image )".equals( mode ) )
                windowingMode = WindowingMode.WINDOWING_MODE_LOCAL;
            else if ( "Global ( relative )".equals( mode ) )
                windowingMode = WindowingMode.WINDOWING_MODE_GLOBAL_RELATIVE;
            else if ( "Global ( absolute )".equals( mode ) )
                windowingMode = WindowingMode.WINDOWING_MODE_GLOBAL_ABSOLUTE;

            volumeObject.updateWindowing( windowingMode );

            sliceCanvas.display();
            volumeView.display();
        }
        else if ( "restore".equals( cmd ) )
        {
            this.windowingMode = WindowingMode.WINDOWING_MODE_LOCAL;

            transferFrame.resetWindowingModeBox();

            volumeObject.reloadOriginalWindowing();

            transferFrame.updateValues();

            sliceCanvas.display();
            volumeView.display();
        }
        else if ( "transferFunction".equals( cmd ) )
        {
            JComboBox comboBox = (JComboBox)event.getSource();
            String tf = (String)comboBox.getSelectedItem();

            if ( "Greyscale".equals( tf ) )
            {
                volumeObject.setTransferFunction( ImageUtil.getRGBATransferFunction( Color.BLACK, Color.WHITE, 0.0, 1.0 ) );
            }
            else if ( "Gold".equals( tf ) )
            {
                volumeObject.setTransferFunction(  ImageUtil.getRGBATransferFunction( Color.BLACK, Color.ORANGE, 0.0, 1.0 ) );
            }
            else if ( "Rainbow".equals( tf ) )
            {
                volumeObject.setTransferFunction( ImageUtil.getRainbowTransferFunction( 0.0, 1.0 ) );
            }

            sliceCanvas.display();
            volumeView.display();
        }

    }

    public TransferFrame getTransferFrame()
    {
        return transferFrame;
    }

    public void setTransferFrame( TransferFrame transferFrame )
    {
        this.transferFrame = transferFrame;
    }

    @Override
    public void stateChanged( ChangeEvent event )
    {
        JSlider slider = (JSlider)event.getSource();

        if ( "winCenter".equals( slider.getName() ) )
        {
            volumeObject.updateWindowCenter( (short)slider.getValue(), windowingMode );
            sliceCanvas.display();
            
            if ( ! slider.getValueIsAdjusting() )
                volumeObject.setUpdateGradientTexture(true);
            
            volumeView.display();
        }
        else if ( "winWidth".equals( slider.getName() ) )
        {
            volumeObject.updateWindowWidth( (short)slider.getValue(), windowingMode );
            sliceCanvas.display();
            
            if ( ! slider.getValueIsAdjusting() )
                volumeObject.setUpdateGradientTexture(true);
            
            volumeView.display();
        }

    }

}