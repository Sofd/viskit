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
                (int)( volumeObject.getSizeX() * width / volumeObject.getSizeMax() ),
                (int)( volumeObject.getSizeZ() * height / volumeObject.getSizeMax() ) );

    }

    @Override
    public int getCurrentSlice()
    {
        return ( getMaxSlices() - volumeObject.getSliceCursor()[1] - 1 );
    }

    @Override
    public int getHorizontalMaxSlices()
    {
        return volumeObject.getImageWidth();
    }

    @Override
    public int getMaxSlices()
    {
        return volumeObject.getImageHeight();
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
        return volumeObject.getImageDepth();
    }

    @Override
    public void setCurrentSlice( int currentSlice )
    {
        volumeObject.getSliceCursor()[1] = ( getMaxSlices() - currentSlice - 1 );
    }
    
    @Override
    protected void transformTex( GL2 gl )
    {
        gl.glLoadIdentity();
        gl.glTranslatef( 0.5f, 0.5f, 0.5f );
        gl.glRotatef( 90.0f, 1.0f, 0.0f, 0.0f );
        gl.glTranslatef( -0.5f, -0.5f, -0.5f );
    }
    
    @Override
    public void updateReticle()
    {
        reticle.setRelativePosX( getSliceHorizontalFromCursor() * 1.0f / ( getHorizontalMaxSlices() - 1 ) );
        reticle.setRelativePosY( getSliceVerticalFromCursor() * 1.0f / ( getVerticalMaxSlices() - 1 ) );
    }
    
    @Override
    public void updateSliceCursor()
    {
        volumeObject.getSliceCursor()[0] = getSliceHorizontalFromReticle();
        volumeObject.getSliceCursor()[2] = getSliceVerticalFromReticle();
    }
}