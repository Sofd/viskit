package de.sofd.viskit.image3D.jogl.minigui.view;

import static javax.media.opengl.GL.*;

import java.io.*;

import javax.media.opengl.*;

import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.model.*;

public class SlicePlaneX extends SlicePlane
{

    public SlicePlaneX(int x, int y, int width, int height,
            ImagePlaneType type, int currentSlice, int maxSlices,
            VolumeObject volumeObject) throws IOException {
        super(x, y, width, height, type, currentSlice, maxSlices, volumeObject);
        
    }
    
    
    
//    @Override
//    public void show(GL2 gl) {
//        float tz = (currentSlice - 1) * 1.0f / (maxSlices - 1);
//        /*float sizeX = (float) (volumeObject.getSizeZ() * getPlaneWidth() / volumeObject
//                .getMaxSize());
//        float sizeY = (float) (volumeObject.getSizeY() * getPlaneHeight() / volumeObject
//                .getMaxSize());
//        float x = margin[3] + (getPlaneWidth() - sizeX) / 2;
//        float y = margin[2] + (getPlaneHeight() - sizeY) / 2;*/
//    }
    
    @Override
    protected void transformTex( GL2 gl )
    {
        gl.glMatrixMode(GL_TEXTURE);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glTranslatef(0.5f, 0.5f, 0.5f);
        gl.glRotatef(-90.0f, 0.0f, 1.0f, 0.0f);
        gl.glTranslatef(-0.5f, -0.5f, -0.5f);
    }
}