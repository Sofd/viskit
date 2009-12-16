package de.sofd.viskit.image3D.jogl.minigui.controller;

import de.sofd.viskit.image3D.jogl.minigui.view.*;

public class SliderController extends DragController
{
    protected Slider slider;
    
    public SliderController( Slider slider )
    {
        super( slider.getPin() );
        
        this.slider = slider;
    }
    
    @Override
    public void dragged( int button, int mouseX, int mouseY )
    {
        super.dragged( button, mouseX, mouseY );
    }
}