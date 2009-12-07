package de.sofd.viskit.image3D.jogl.minigui.controller;

import de.sofd.viskit.image3D.jogl.minigui.view.*;

public class SliderController extends DragController
{
    protected Slider slider;
    
    public SliderController( Slider slider )
    {
        super( slider.getPin(), 
                slider.getX(), 
                slider.getX() + slider.getWidth() - slider.getPin().getTex().getImageWidth(), 
                slider.getY() + slider.getHeight() - slider.getPin().getTex().getImageHeight(),
                slider.getY() + slider.getHeight() - slider.getPin().getTex().getImageHeight() );
        
        this.slider = slider;
        
        setRelativeXPosition( slider.getRelativeValue() );
    }
    
    @Override
    public void dragged( int button, int mouseX, int mouseY )
    {
        super.dragged( button, mouseX, mouseY );
        
        //and y?
        slider.setRelativeValue( getRelativeXPosition() );
    }
}