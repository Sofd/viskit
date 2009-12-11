package de.sofd.viskit.image3D.jogl.control;

import de.sofd.viskit.image3D.jogl.minigui.controller.*;
import de.sofd.viskit.image3D.jogl.view.*;

public class SliceViewportController extends OrthoViewportController {
    protected SlicePlaneController slicePlaneController;

    protected DragController sliderPinController;

    protected TransferComponentController transferComponentController;

    public SliceViewportController(SliceViewport sliceViewport,
            java.awt.Component awtParent) {
        super(sliceViewport);

        slicePlaneController = new SlicePlaneController(sliceViewport
                .getPlane(), awtParent);

        sliderPinController = new SliderController(sliceViewport.getSlider());

        transferComponentController = new TransferComponentController(
                sliceViewport.getTransferComp());
    }

    protected SliceViewport getSliceViewport() {
        return (SliceViewport) getOrthoViewport();
    }
    
    public boolean isDragging()
    {
        return sliderPinController.isActive();
    }

    @Override
    public void mouseClicked(int button, int mX, int mY) {
        if (orthoViewport.isInBounds(mX, mY)) {
            int mouseX = orthoViewport.getRelativeMouseX(mX);
            int mouseY = orthoViewport.getRelativeMouseY(mY);

            slicePlaneController.mouseClicked(button, mouseX, mouseY);
        }
    }

    @Override
    public void mouseDragged(int button, int mX, int mY) {
        if (orthoViewport.isInBounds(mX, mY)) {
            int mouseX = orthoViewport.getRelativeMouseX(mX);
            int mouseY = orthoViewport.getRelativeMouseY(mY);

            sliderPinController.dragged(button, mouseX, mouseY);

            getSliceViewport().getPlane().setCurrentSlice(
                    getSliceViewport().getSlider().getValue() - 1);
        }
    }

    @Override
    public void mouseMoved(int button, int mX, int mY) {
        // if (orthoViewport.isInBounds(mX, mY)) {
        int mouseX = orthoViewport.getRelativeMouseX(mX);
        int mouseY = orthoViewport.getRelativeMouseY(mY);

        slicePlaneController.mouseMoved(button, mouseX, mouseY);
        // }
    }

    @Override
    public void mousePressed(int button, int mX, int mY) {
        if (orthoViewport.isInBounds(mX, mY)) {
            int mouseX = orthoViewport.getRelativeMouseX(mX);
            int mouseY = orthoViewport.getRelativeMouseY(mY);

            sliderPinController.pressed(button, mouseX, mouseY);
        }
    }

    @Override
    public void mouseReleased(int button, int mX, int mY) {
        if (orthoViewport.isInBounds(mX, mY)) {
            sliderPinController.released(button);
        }

    }

    public void updateComponents() {
        slicePlaneController.updateComponents();
        
        getSliceViewport().getSlider().setValue(
                getSliceViewport().getPlane().getCurrentSlice() + 1);
        
        transferComponentController.getTransferComponent().setRelativeValue(
                getSliceViewport().getVolumeObject().getRelativeCursorValue());
    }

}