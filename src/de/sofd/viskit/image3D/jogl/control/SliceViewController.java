package de.sofd.viskit.image3D.jogl.control;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import de.sofd.viskit.image3D.jogl.minigui.view.Component;
import de.sofd.viskit.image3D.jogl.view.*;
import de.sofd.viskit.image3D.view.*;

public class SliceViewController implements MouseListener, MouseMotionListener
{
    protected SliceCanvas sliceCanvas;

    protected ArrayList<OrthoViewportController> orthoViewportControllerList = new ArrayList<OrthoViewportController>();
    
    protected TransferFrame transferFrame;
    
    protected GPUVolumeView volumeView;
    
    protected Robot robot;

    public SliceViewController( SliceCanvas sliceCanvas, Robot robot )
    {
        this.sliceCanvas = sliceCanvas;
        this.robot = robot;
        
        for ( Component orthoViewport : sliceCanvas.getSliceView().getViewports() )
        {
            if ( orthoViewport instanceof SliceViewport )
                orthoViewportControllerList.add( new SliceViewportController( (SliceViewport)orthoViewport, sliceCanvas, robot ) );
        }
    }

    @Override
    public synchronized void mouseClicked( MouseEvent e )
    {
        boolean resizeRequired = false;
        for ( OrthoViewportController orthoViewportController : orthoViewportControllerList )
        {
            OrthoViewport orthoViewport = orthoViewportController.getOrthoViewport();
            if ( orthoViewport.isVisible() )
            {
                boolean maximizedBefore = orthoViewport.isMaximized();
                orthoViewportController.mouseClicked( e.getButton(), e.getX(), sliceCanvas.getViewportHeight() - e.getY(), e.getClickCount() );
                boolean maximizedAfter = orthoViewport.isMaximized();
                
                resizeRequired = resizeRequired || ( maximizedBefore != maximizedAfter );
            }
        }
        
        if ( resizeRequired )
        {
            sliceCanvas.getSliceView().getLayout().checkMaximized();
            sliceCanvas.getSliceView().resizeLayout();
        }
        
        sliceCanvas.display();
        
        if ( transferFrame != null )
            transferFrame.updateValues();
    }

    @Override
    public synchronized void mouseDragged( MouseEvent e )
    {
        for ( OrthoViewportController orthoViewportController : orthoViewportControllerList )
        {
            if ( ! orthoViewportController.getOrthoViewport().isVisible() ) continue;
            
            boolean draggingLocked = false;
            for ( OrthoViewportController orthoViewportController2 : orthoViewportControllerList )
            {
                if ( ! orthoViewportController2.getOrthoViewport().isVisible() ) continue;
                
                if ( orthoViewportController != orthoViewportController2 && orthoViewportController2.isDragging() )
                    draggingLocked = true;
            }
            
            if ( ! draggingLocked )
                orthoViewportController.mouseDragged( e, e.getX(), sliceCanvas.getViewportHeight() - e.getY() );
        }
        
        boolean cutterIsActive = false;

        for ( OrthoViewportController orthoViewportController : orthoViewportControllerList )
        {
            if ( ! orthoViewportController.getOrthoViewport().isVisible() ) continue;
            
            if ( orthoViewportController instanceof SliceViewportController )
            {
                SliceViewportController sliceViewportController = (SliceViewportController)orthoViewportController;
                
                sliceViewportController.updateComponents();
                cutterIsActive |= sliceViewportController.getSlicePlaneController().getCutterController().isActive();
                
            }
        }
        
        sliceCanvas.display();
        
        if ( volumeView != null && cutterIsActive ) {
            volumeView.display(false);
        }
        
            
    }

    @Override
    public void mouseEntered( MouseEvent arg0 )
    {
        sliceCanvas.requestFocus();
        //sliceCanvas.getAnimator().start();

    }

    @Override
    public void mouseExited( MouseEvent e )
    {
        
    }

    @Override
    public synchronized void mouseMoved( MouseEvent e )
    {
        for ( OrthoViewportController orthoViewportController : orthoViewportControllerList )
            if ( orthoViewportController.getOrthoViewport().isVisible() )
                orthoViewportController.mouseMoved( e, e.getX(), sliceCanvas.getViewportHeight() - e.getY() );

        for ( OrthoViewportController orthoViewportController : orthoViewportControllerList )
        {
            if ( orthoViewportController instanceof SliceViewportController )
            {
                ( (SliceViewportController)orthoViewportController ).updateComponents();
            }
        }
        
        sliceCanvas.display();
    }

    @Override
    public void mousePressed( MouseEvent e )
    {
        for ( OrthoViewportController orthoViewportController : orthoViewportControllerList )
            if ( orthoViewportController.getOrthoViewport().isVisible() )
                orthoViewportController.mousePressed( e.getButton(), e.getX(), sliceCanvas.getViewportHeight() - e.getY() );
        
        sliceCanvas.display();
    }

    @Override
    public void mouseReleased( MouseEvent e )
    {
        for ( OrthoViewportController orthoViewportController : orthoViewportControllerList )
            if ( orthoViewportController.getOrthoViewport().isVisible() )
                orthoViewportController.mouseReleased( e.getButton(), e.getX(), sliceCanvas.getViewportHeight() - e.getY() );
        
        if ( transferFrame != null )
            transferFrame.updateValues();
        
        volumeView.display(true);
    }

    public void setTransferFrame( TransferFrame transferFrame )
    {
        this.transferFrame = transferFrame;
    }

    public void setVolumeView( GPUVolumeView volumeView )
    {
        this.volumeView = volumeView;
    }

}