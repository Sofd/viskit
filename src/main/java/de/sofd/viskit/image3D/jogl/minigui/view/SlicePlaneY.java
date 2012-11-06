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
        super( x, y, width, height, ImageAxis.AXIS_Y, type, volumeObject,  new CutterPlane( x, y, width, height, volumeObject.getConstraint().getX(), volumeObject.getConstraint().getZ() )  );

    }

    @Override
    public double getCurrentSlice()
    {
        return ( getMaxSlices() - volumeObject.getSliceCursor()[ 1 ] - 1 );
    }

    @Override
    public int getHorizontalMaxSlices()
    {
        return volumeObject.getImageDim().getWidth();
    }

    @Override
    public int getMaxSlices()
    {
        return volumeObject.getImageDim().getHeight();
    }

    @Override
    public double getSliceHorizontalFromCursor()
    {
        return volumeObject.getSliceCursor()[ 0 ];
    }

    @Override
    public double getSliceHorizontalFromReticle()
    {
        return ( reticle.getRelativeXPosition() * ( getHorizontalMaxSlices() - 1 ) );
    }

    @Override
    public double getSliceVerticalFromCursor()
    {
        return volumeObject.getSliceCursor()[ 2 ];
    }

    @Override
    public double getSliceVerticalFromReticle()
    {
        return ( reticle.getRelativeYPosition() * ( getVerticalMaxSlices() - 1 ) );
    }

    @Override
    protected int getTexHeight()
    {
        return (int)( volumeObject.getSizeZ() * height / volumeObject.getSizeRange().getMax() );
    }

    @Override
    protected int getTexWidth()
    {
        return (int)( volumeObject.getSizeX() * width / volumeObject.getSizeRange().getMax() );
    }

    @Override
    public int getVerticalMaxSlices()
    {
        return volumeObject.getImageDim().getDepth();
    }

    @Override
    public void setCurrentSlice( double currentSlice )
    {
        volumeObject.getSliceCursor()[ 1 ] = ( getMaxSlices() - currentSlice - 1 );
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
        reticle.setRelativePosX( (float)getSliceHorizontalFromCursor() * 1.0f / ( getHorizontalMaxSlices() - 1 ) );
        reticle.setRelativePosY( (float)getSliceVerticalFromCursor() * 1.0f / ( getVerticalMaxSlices() - 1 ) );
    }

    @Override
    public void updateSliceCursor()
    {
        volumeObject.getSliceCursor()[ 0 ] = getSliceHorizontalFromReticle();
        volumeObject.getSliceCursor()[ 2 ] = getSliceVerticalFromReticle();
    }
}