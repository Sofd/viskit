package de.sofd.viskit.image3D.jogl.minigui.view;

import java.io.*;

import javax.media.opengl.*;

import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.model.*;

public class SlicePlaneY extends SlicePlane
{

    public SlicePlaneY( int x, int y, int width, int height, ImagePlaneType type, VolumeObject volumeObject )
            throws IOException
    {
        super(    x,
                y,
                width,
                height,
                ImageAxis.AXIS_Y,
                type,
                volumeObject,
                (int)( volumeObject.getSizeX() * width / volumeObject.getMaxSize() ),
                (int)( volumeObject.getSizeZ() * height / volumeObject.getMaxSize() ) );

    }

    @Override
    public int getCurrentSlice()
    {
        return volumeObject.getSliceCursor()[1];
    }

    @Override
    public int getHorizontalMaxSlices()
    {
        return volumeObject.getWidth();
    }

    @Override
    public int getMaxSlices()
    {
        return volumeObject.getHeight();
    }

    @Override
    public int getSliceHorizontalFromCursor()
    {
        return volumeObject.getSliceCursor()[0];
    }

    @Override
    public int getSliceHorizontalFromReticle()
    {
        return (int)( reticle.getRelativeXPosition() * ( getHorizontalMaxSlices() - 1 ) );
    }

    @Override
    public int getSliceVerticalFromCursor()
    {
        return volumeObject.getSliceCursor()[2];
    }

    @Override
    public int getSliceVerticalFromReticle()
    {
        return (int)( reticle.getRelativeYPosition() * ( getVerticalMaxSlices() - 1 ) );
    }
    
    @Override
    public int getVerticalMaxSlices()
    {
        return volumeObject.getDepth();
    }

    @Override
    public void setCurrentSlice( int currentSlice )
    {
        volumeObject.getSliceCursor()[1] = currentSlice;
    }
    
    @Override
    protected void transformTex( GL2 gl )
    {
        gl.glLoadIdentity();
        gl.glTranslatef( 0.5f, 0.5f, 0.5f );
        gl.glScalef( 1.0f, -1.0f, 1.0f );
        gl.glRotatef( -90.0f, 1.0f, 0.0f, 0.0f );
        gl.glScalef( 1.0f, 1.0f, -1.0f );
        gl.glTranslatef( -0.5f, -0.5f, -0.5f );
    }
    
    @Override
    public void updateSliceCursor()
    {
        volumeObject.getSliceCursor()[0] = getSliceHorizontalFromReticle();
        volumeObject.getSliceCursor()[2] = getSliceVerticalFromReticle();
    }
}