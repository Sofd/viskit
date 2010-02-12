package de.sofd.viskit.image3D.jogl.control;

import java.awt.*;
import java.awt.event.*;

import de.sofd.viskit.image3D.jogl.minigui.controller.*;
import de.sofd.viskit.image3D.jogl.view.*;

public class SliceViewportController extends OrthoViewportController
{
    protected SlicePlaneController slicePlaneController;

    protected DragController sliderPinController;

    public SliceViewportController(SliceViewport sliceViewport, java.awt.Component awtParent, Robot robot) {
        super(sliceViewport, robot);

        slicePlaneController = new SlicePlaneController(sliceViewport.getPlane(), awtParent, robot);

        sliderPinController = new SliderController(sliceViewport.getSlider(), robot);
    }

    public SlicePlaneController getSlicePlaneController() {
        return slicePlaneController;
    }

    protected SliceViewport getSliceViewport()
    {
        return (SliceViewport)getOrthoViewport();
    }

    public boolean isDragging()
    {
        return sliderPinController.isActive();
    }

    @Override
    public void mouseClicked(    int button,
                                int mX,
                                int mY,
                                int clickCount )
    {
        super.mouseClicked( button, mX, mY, clickCount );
        
        if ( orthoViewport.isInBounds( mX, mY ) )
        {
            int mouseX = orthoViewport.getRelativeMouseX( mX );
            int mouseY = orthoViewport.getRelativeMouseY( mY );

            slicePlaneController.mouseClicked( button, mouseX, mouseY );
        }
    }

    @Override
    public void mouseDragged(    MouseEvent e,
                                int mX,
                                int mY )
    {
        int mouseX = orthoViewport.getRelativeMouseX( mX );
        int mouseY = orthoViewport.getRelativeMouseY( mY );

        sliderPinController.dragged( e, mouseX, mouseY );
        slicePlaneController.mouseDragged( e, mouseX, mouseY );
        
        getSliceViewport().getPlane().setCurrentSlice( getSliceViewport().getSlider().getValue() - 1 );

    }

    @Override
    public void mouseMoved( MouseEvent e,
                            int mX,
                            int mY )
    {
        // if (orthoViewport.isInBounds(mX, mY)) {
        int mouseX = orthoViewport.getRelativeMouseX( mX );
        int mouseY = orthoViewport.getRelativeMouseY( mY );

        slicePlaneController.mouseMoved( e, mouseX, mouseY );
        // }
    }

    @Override
    public void mousePressed(    int button,
                                int mX,
                                int mY )
    {
        if ( orthoViewport.isInBounds( mX, mY ) )
        {
            int mouseX = orthoViewport.getRelativeMouseX( mX );
            int mouseY = orthoViewport.getRelativeMouseY( mY );

            sliderPinController.pressed( button, mouseX, mouseY );
            
            slicePlaneController.mousePressed( button, mouseX, mouseY );
        }
    }

    @Override
    public void mouseReleased(    int button,
                                int mX,
                                int mY )
    {
        sliderPinController.released( button );
        
        slicePlaneController.mouseReleased( button, mX, mY );
    }

    public synchronized void updateComponents()
    {
        slicePlaneController.updateComponents();
        
        getSliceViewport().updateSlider();
        
        
    }

    

}