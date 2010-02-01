package de.sofd.viskit.image3D.control;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import de.sofd.viskit.image3D.jogl.control.*;
import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.jogl.view.*;
import de.sofd.viskit.image3D.model.*;
import de.sofd.viskit.image3D.view.*;

public class TransferController implements ActionListener, ChangeListener
{
    
    public class TransferUpdateRunnable extends Thread {

        protected SliceCanvas sliceCanvas;

        protected GPUVolumeView volumeView;
        
        protected boolean isAdjusting;
        
        protected boolean running;
        
        public void setAdjusting(boolean isAdjusting) {
            this.isAdjusting = isAdjusting;
        }

        public TransferUpdateRunnable(SliceCanvas sliceCanvas, GPUVolumeView volumeView)
        {
            super();
            this.sliceCanvas = sliceCanvas;
            this.volumeView = volumeView;
        }
        
        @Override
        public void run() {
            if ( running ) return;
            
            running = true;
            
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            sliceCanvas.display();
            
            if ( !isAdjusting )
                volumeView.display(true);
            else
                volumeView.display(false);
            
            running = false;
            
        }
        
    }
    
    protected SliceCanvas sliceCanvas;

    protected GPUVolumeView volumeView;

    protected VolumeObject volumeObject;

    protected WindowingMode windowingMode;

    protected TransferFrame transferFrame;
    
    protected TransferUpdateRunnable thread;

    public TransferController( SliceCanvas sliceCanvas, GPUVolumeView volumeView, VolumeObject volumeObject )
    {
        this.sliceCanvas = sliceCanvas;
        this.volumeView = volumeView;
        this.volumeObject = volumeObject;

        this.windowingMode = WindowingMode.WINDOWING_MODE_LOCAL;
        
        this.thread = new TransferUpdateRunnable(sliceCanvas, volumeView);
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
            volumeView.display(true);
        }
        else if ( "restore".equals( cmd ) )
        {
            this.windowingMode = WindowingMode.WINDOWING_MODE_LOCAL;

            transferFrame.resetWindowingModeBox();

            volumeObject.reloadOriginalWindowing();

            transferFrame.updateValues();

            sliceCanvas.display();
            volumeView.display(true);
        }
        else if ( "transferFunction".equals( cmd ) )
        {
            JComboBox comboBox = (JComboBox)event.getSource();
            String tf = (String)comboBox.getSelectedItem();

            volumeObject.setTransferFunction( LutController.getLutMap().get(tf) );
            
            sliceCanvas.display();
            volumeView.display(true);
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
        final JSlider slider = (JSlider)event.getSource();

        if ( "winCenter".equals( slider.getName() ) )
        {
            volumeObject.updateWindowCenter( (short)slider.getValue(), windowingMode );
            
            if ( ! slider.getValueIsAdjusting() )
            {
                volumeObject.setUpdateGradientTexture(true);
                volumeObject.setUpdateConvolutionTexture(true);
            }
        }
        else if ( "winWidth".equals( slider.getName() ) )
        {
            volumeObject.updateWindowWidth( (short)slider.getValue(), windowingMode );
            
            if ( ! slider.getValueIsAdjusting() ) {
                volumeObject.setUpdateGradientTexture(true);
                volumeObject.setUpdateConvolutionTexture(true);
            }
            
            
        }
        
        thread.setAdjusting(slider.getValueIsAdjusting());
        
        SwingUtilities.invokeLater( thread );

    }

}