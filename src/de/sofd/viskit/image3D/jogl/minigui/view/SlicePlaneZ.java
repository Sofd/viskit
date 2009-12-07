package de.sofd.viskit.image3D.jogl.minigui.view;

import static javax.media.opengl.GL.*;

import java.io.*;

import javax.media.opengl.*;

import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.model.*;

public class SlicePlaneZ extends SlicePlane
{

    public SlicePlaneZ(int x, int y, int width, int height,
            ImagePlaneType type, int currentSlice, int maxSlices,
            VolumeObject volumeObject) throws IOException {
        super(x, y, width, height, type, currentSlice, maxSlices, volumeObject);
        
    }
    
    
    
//    float sizeX = (float) (volumeObject.getSizeX() * getPlane().getWidth() / volumeObject
//            .getMaxSize());
//    float sizeY = (float) (volumeObject.getSizeY() * getPlane().getHeight() / volumeObject
//            .getMaxSize());
//    float x = margin[3] + (getPlane().getWidth() - sizeX) / 2;
//    float y = margin[2] + (getPlane().getHeight() - sizeY) / 2;
    
    @Override
    protected void transformTex( GL2 gl )
    {

    }
}