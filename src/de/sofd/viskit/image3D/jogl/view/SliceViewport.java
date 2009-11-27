package de.sofd.viskit.image3D.jogl.view;

import javax.media.opengl.*;

import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.model.*;

public abstract class SliceViewport extends OrthoViewport
{
    protected int borderSize;
    
    protected ImagePlaneType planeType;
    
    protected float zoom;
    
    /**
     * Number of current slice ( begins with 1 )
     */
    protected int currentSlice;
    
    protected int maxSlices;
    
    protected VolumeObject volumeObject;

    public SliceViewport( int x, int y, int width, int height, ImagePlaneType planeType, int maxSlices, VolumeObject volumeObject )
    {
        super(x, y, width, height);
        setPlaneType(planeType);
        setBorderSize(0);
        setZoom(0);
        setCurrentSlice(1);
        setMaxSlices(maxSlices);
        setVolumeObject(volumeObject);
    }
    
    public void display(GL2 gl)
    {
        beginViewport(gl);
        
        showTexPlane(gl);
        
        endViewport(gl);
    }
    
    protected abstract void showTexPlane(GL2 gl);
    
    public int getBorderSize() {
        return borderSize;
    }

    public void setBorderSize(int borderSize) {
        this.borderSize = borderSize;
    }

    public ImagePlaneType getPlaneType() {
        return planeType;
    }

    public void setPlaneType(ImagePlaneType planeType) {
        this.planeType = planeType;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public int getCurrentSlice() {
        return currentSlice;
    }

    public void setCurrentSlice(int currentSlice) {
        this.currentSlice = currentSlice;
    }

    public int getMaxSlices() {
        return maxSlices;
    }

    public void setMaxSlices(int maxSlices) {
        this.maxSlices = maxSlices;
    }

    public VolumeObject getVolumeObject() {
        return volumeObject;
    }

    public void setVolumeObject(VolumeObject volumeObject) {
        this.volumeObject = volumeObject;
    }
}