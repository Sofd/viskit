package de.sofd.viskit.image3D.jogl.view;

import java.io.*;

import javax.media.opengl.*;

import com.sun.opengl.util.texture.*;

import de.sofd.viskit.image3D.jogl.minigui.*;
import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.model.*;

public abstract class SliceViewport extends OrthoViewport 
{
    //top, right, bottom, left
    protected int[] margin = new int[4];
    
    protected ImagePlaneType planeType;
    
    /**
     * Number of current slice ( begins with 1 )
     */
    protected int currentSlice;
    
    protected int maxSlices;
    
    protected VolumeObject volumeObject;
    
    protected Slider slider;
    
    public SliceViewport( int x, int y, int width, int height, ImagePlaneType planeType, int maxSlices, VolumeObject volumeObject ) throws IOException
    {
        super(x, y, width, height);
        setPlaneType(planeType);
        setMargin(10, 30, 30, 10);
        setCurrentSlice(maxSlices/2);
        setMaxSlices(maxSlices);
        setVolumeObject(volumeObject);
        
        Texture sliderBgTex = ResourceLoader.getImageTex("minigui.slider.bg");
        Texture sliderPinTex = ResourceLoader.getImageTex("minigui.slider.pin");
        slider = new Slider( margin[3], 10, getPlaneWidth(), sliderBgTex, sliderPinTex, 1, maxSlices, currentSlice );
    }
    
    public void show(GL2 gl)
    {
        beginViewport(gl);
        
        showTexPlane(gl);
        slider.show(gl);
        
        endViewport(gl);
    }
    
    public int getCurrentSlice() {
        return currentSlice;
    }

    public int getMaxSlices() {
        return maxSlices;
    }
    
    public int getPlaneHeight()
    {
        return ( height - margin[0] - margin[2] );
    }

    public ImagePlaneType getPlaneType() {
        return planeType;
    }
    
    public int getPlaneWidth()
    {
        return ( width - margin[1] - margin[3] );
    }

    public VolumeObject getVolumeObject() {
        return volumeObject;
    }
    
    public void setCurrentSlice(int currentSlice) {
        this.currentSlice = currentSlice;
    }

    public void setMargin( int top, int right, int bottom, int left ) {
        margin[0] = top;
        margin[1] = right;
        margin[2] = bottom;
        margin[3] = left;
        
    }
    
    public void setMaxSlices(int maxSlices) {
        this.maxSlices = maxSlices;
    }

    public void setPlaneType(ImagePlaneType planeType) {
        this.planeType = planeType;
    }

    public void setVolumeObject(VolumeObject volumeObject) {
        this.volumeObject = volumeObject;
    }

    protected abstract void showTexPlane(GL2 gl);
    
    public void mouseDragged( int mX, int mY ) {
        if ( isInBounds(mX, mY))
        {
            int mouseX = getRelativeMouseX( mX );
            int mouseY = getRelativeMouseY( mY );
            
            slider.mouseDragged( mouseX, mouseY );
            this.currentSlice = slider.getValue();
        }
    }

    public void mousePressed( int mX, int mY ) {
        if ( isInBounds( mX, mY ))
        {
            int mouseX = getRelativeMouseX( mX );
            int mouseY = getRelativeMouseY( mY );
            
            slider.mousePressed( mouseX, mouseY );
        }
    }

    public void mouseReleased( int mX, int mY) {
        if ( isInBounds( mX, mY ))
        {
            int mouseX = getRelativeMouseX( mX );
            int mouseY = getRelativeMouseY( mY );
            
            slider.mouseReleased( mouseX, mouseY );
        }
        
    }
}