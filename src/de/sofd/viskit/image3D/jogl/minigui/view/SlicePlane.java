package de.sofd.viskit.image3D.jogl.minigui.view;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2GL3.*;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.*;

import java.io.*;

import javax.media.opengl.*;

import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.jogl.util.*;
import de.sofd.viskit.image3D.model.*;

public abstract class SlicePlane extends Component
{

    protected Reticle reticle;
        
    /**
     * Number of current slice ( begins with 1 )
     */
    protected int currentSlice;

    protected int maxSlices;

    protected VolumeObject volumeObject;

    protected ImagePlaneType type;

    public SlicePlane(int x, int y, int width, int height, ImagePlaneType type, int currentSlice, int maxSlices, VolumeObject volumeObject) throws IOException {
        super(x, y, width, height);
    
        reticle = new Reticle(x, y, width, height, x + width / 2, y + height/ 2);
        reticle.setColor(0.2f, 0.2f, 0.2f, 0.2f);
        
        setType( type );
        setCurrentSlice(currentSlice);
        setMaxSlices(maxSlices);
        setVolumeObject(volumeObject);
    }
    
    public int getCurrentSlice() {
        return currentSlice;
    }
    
    public int getMaxSlices() {
        return maxSlices;
    }
    
    public Reticle getReticle() {
        return reticle;
    }

    public ImagePlaneType getType() {
        return type;
    }

    public VolumeObject getVolumeObject() {
        return volumeObject;
    }
    
    public void setCurrentSlice(int currentSlice) {
        this.currentSlice = currentSlice;
    }
    
    public void setMaxSlices(int maxSlices) {
        this.maxSlices = maxSlices;
    }

    public void setType(ImagePlaneType type) {
        this.type = type;
    }

    public void setVolumeObject(VolumeObject volumeObject) {
        this.volumeObject = volumeObject;
    }

    public void show(GL2 gl) {
        showTexPlane( gl );
        reticle.show(gl);
    }
    
    protected void showTexPlane( GL2 gl )
    {
        transformTex( gl );
        
        float tz = (currentSlice - 1) * 1.0f / (maxSlices - 1);
        
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glEnable(GL_TEXTURE_3D);
        gl.glBindTexture(GL_TEXTURE_3D, volumeObject.getTexId());

        gl.glEnable(GL_BLEND);
        gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GLUtil.texQuad3DCentered(gl, x, y, width, height, 1, 1, tz);

        gl.glDisable(GL_BLEND);
        gl.glDisable(GL_TEXTURE_3D);
        
        gl.glMatrixMode(GL_TEXTURE);
        gl.glPopMatrix();

        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
        
    }
    
    protected abstract void transformTex( GL2 gl );

    
}