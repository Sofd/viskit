package de.sofd.viskit.image3D.jogl.minigui.controller;

import de.sofd.viskit.image3D.jogl.minigui.view.*;

public class TransferComponentController
{
    protected TransferComponent transferComponent;

    protected DragController pinController;

    public TransferComponentController( TransferComponent transferComponent )
    {
        this.transferComponent = transferComponent;

        SliderPin pin = transferComponent.getPin();
        pinController = new DragController( pin, pin.getX(), pin.getX(), pin.getY(), pin.getY()
                + transferComponent.getHeight() - pin.getHeight() );
    }

    public TransferComponent getTransferComponent()
    {
        return transferComponent;
    }
}