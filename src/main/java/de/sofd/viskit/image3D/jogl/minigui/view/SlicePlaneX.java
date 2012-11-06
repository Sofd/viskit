package de.sofd.viskit.image3D.jogl.minigui.view;

import java.io.*;

import javax.media.opengl.*;

import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.model.*;

public class SlicePlaneX extends SlicePlane
{

    public SlicePlaneX( int x, int y, int width, int height, ImagePlaneType type, VolumeObject volumeObject )
            throws IOException
    {
        super( x, y, width, height, ImageAxis.AXIS_X, type, volumeObject, new CutterPlane( x, y, width, height,
                volumeObject.getConstraint().getZ(), volumeObject.getConstraint().getY() ) );

    }

    @Override
    public double getCurrentSlice()
    {
        return volumeObject.getSliceCursor()[ 0 ];
    }

    @Override
    public int getHorizontalMaxSlices()
    {
        return volumeObject.getImageDim().getDepth();
    }

    @Override
    public int getMaxSlices()
    {
        return volumeObject.getImageDim().getHeight();
    }

    @Override
    public double getSliceHorizontalFromCursor()
    {
        return volumeObject.getSliceCursor()[ 2 ];
    }

    @Override
    public double getSliceHorizontalFromReticle()
    {
        return ( reticle.getRelativeXPosition() * ( getHorizontalMaxSlices() - 1 ) );
    }

    @Override
    public double getSliceVerticalFromCursor()
    {
        return volumeObject.getSliceCursor()[ 1 ];
    }

    @Override
    public double getSliceVerticalFromReticle()
    {
        return ( ( 1 - reticle.getRelativeYPosition() ) * ( getVerticalMaxSlices() - 1 ) );
    }

    @Override
    protected int getTexHeight()
    {
        return (int)( volumeObject.getSizeY() * height / volumeObject.getSizeRange().getMax() );
    }

    @Override
    protected int getTexWidth()
    {
        return (int)( volumeObject.getSizeZ() * width / volumeObject.getSizeRange().getMax() );
    }

    @Override
    public int getVerticalMaxSlices()
    {
        return volumeObject.getImageDim().getHeight();
    }

    @Override
    public void setCurrentSlice( double currentSlice )
    {
        volumeObject.getSliceCursor()[ 0 ] = currentSlice;

    }

    @Override
    protected void transformTex( GL2 gl )
    {
        gl.glLoadIdentity();
        gl.glTranslatef( 0.5f, 0.5f, 0.5f );
        gl.glScalef( -1, -1, 1 );
        gl.glRotatef( -90.0f, 0.0f, 1.0f, 0.0f );
        gl.glTranslatef( -0.5f, -0.5f, -0.5f );
    }

    @Override
    public void updateReticle()
    {
        reticle.setRelativePosX( (float)getSliceHorizontalFromCursor() * 1.0f / ( getHorizontalMaxSlices() - 1 ) );
        reticle.setRelativePosY( 1.0f - (float)getSliceVerticalFromCursor() * 1.0f / ( getVerticalMaxSlices() - 1 ) );
    }

    @Override
    public void updateSliceCursor()
    {
        volumeObject.getSliceCursor()[ 2 ] = getSliceHorizontalFromReticle();
        volumeObject.getSliceCursor()[ 1 ] = getSliceVerticalFromReticle();
    }

}