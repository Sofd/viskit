package de.sofd.viskit.image3D.jogl.model;

import static de.sofd.viskit.image3D.jogl.util.Vtk2GL.*;

import java.nio.*;

import javax.media.opengl.*;

import vtk.*;

public class VolumeObject
{
    /**
     * OpenGL-Id of 3D-Texture.
     */
    protected int texId;

    /**
     * Databuffer for 3D-Texture.
     */
    protected FloatBuffer dataBuf;

    /**
     * OpenGL-Id fuer Transferfunktion.
     */
    protected int transferTexId;

    protected int width;

    protected int height;

    protected int depth;
    protected int maxDim;
    protected int minDim;
    protected double spacingX;
    protected double spacingY;

    protected double spacingZ;
    /**
     * Maximum of width*spacingX, height*spacingY, depth*spacingZ
     */
    protected double maxSize;
    /**
     * Minimum of width*spacingX, height*spacingY, depth*spacingZ
     */
    protected double minSize;

    /**
     * Value range.
     */
    protected double rangeMin;

    protected double rangeMax;

    /**
     * Point in volume object where x-slice, y-slice and z-slice intersect
     */
    protected int[] sliceCursor;

    public VolumeObject( vtkImageData imageData, FloatBuffer dataBuf, int[] sliceCursor )
    {
        int[] dim = imageData.GetDimensions();
        double[] spacing = imageData.GetSpacing();
        double[] range = imageData.GetScalarRange();
        
        setWidth( dim[0] );
        setHeight( dim[1] );
        setDepth( dim[2] );

        setSpacingX( spacing[0] );
        setSpacingY( spacing[1] );
        setSpacingZ( spacing[2] );

        setMaxDim( Math.max( Math.max( dim[0], dim[1] ), dim[2] ) );
        setMinDim( Math.min( Math.max( dim[0], dim[1] ), dim[2] ) );

        setMaxSize( Math.max( Math.max( dim[0] * spacing[0], dim[1] * spacing[1] ), dim[2] * spacing[2] ) );
        setMinSize( Math.min( Math.max( dim[0] * spacing[0], dim[1] * spacing[1] ), dim[2] * spacing[2] ) );

        setRangeMin( range[0] );
        setRangeMax( range[1] );

        setDataBuf( dataBuf );
        setSliceCursor( sliceCursor );

    }

    public float getCursorRelativeValue()
    {
        int[] pos = getSliceCursor();
        return dataBuf.get( pos[2] * getWidth() * getHeight() + pos[1] * getWidth() + pos[0] );
    }

    public float getCursorValue()
    {
        return (float)( getRangeMin() + getCursorRelativeValue() * ( getRangeMax() - getRangeMin() ) );
    }

    public FloatBuffer getDataBuf()
    {
        return dataBuf;
    }

    public int getDepth()
    {
        return depth;
    }

    public int getHeight()
    {
        return height;
    }

    public int getMaxDim()
    {
        return maxDim;
    }

    public double getMaxSize()
    {
        return maxSize;
    }

    public int getMinDim()
    {
        return minDim;
    }

    public double getMinSize()
    {
        return minSize;
    }

    public double getRangeMax()
    {
        return rangeMax;
    }

    public double getRangeMin()
    {
        return rangeMin;
    }

    protected double getRangeSize()
    {
        return ( rangeMax - rangeMin );
    }

    public double getSizeX()
    {
        return ( width * spacingX );
    }

    public double getSizeY()
    {
        return ( height * spacingY );
    }

    public double getSizeZ()
    {
        return ( depth * spacingZ );
    }

    public int[] getSliceCursor()
    {
        return sliceCursor;
    }

    public double getSpacingX()
    {
        return spacingX;
    }

    public double getSpacingY()
    {
        return spacingY;
    }

    public double getSpacingZ()
    {
        return spacingZ;
    }

    public int getTexId()
    {
        return texId;
    }

    public int getTransferTexId()
    {
        return transferTexId;
    }

    public int getWidth()
    {
        return width;
    }

    public void loadTexture( GL2 gl )
    {
        setTexId( get3DTexture( gl, dataBuf, getWidth(), getHeight(), getDepth(), true ) );
    }

    protected void setDataBuf( FloatBuffer dataBuf )
    {
        this.dataBuf = dataBuf;
    }

    protected void setDepth( int depth )
    {
        this.depth = depth;
    }

    protected void setHeight( int height )
    {
        this.height = height;
    }

    protected void setMaxDim( int maxDim )
    {
        this.maxDim = maxDim;
    }

    protected void setMaxSize( double maxSize )
    {
        this.maxSize = maxSize;
    }

    protected void setMinDim( int minDim )
    {
        this.minDim = minDim;
    }

    protected void setMinSize( double minSize )
    {
        this.minSize = minSize;
    }

    protected void setRangeMax( double rangeMax )
    {
        this.rangeMax = rangeMax;
    }

    protected void setRangeMin( double rangeMin )
    {
        this.rangeMin = rangeMin;
    }

    protected void setSliceCursor( int[] sliceCursor )
    {
        this.sliceCursor = sliceCursor;
    }

    protected void setSpacingX( double spacingX )
    {
        this.spacingX = spacingX;
    }

    protected void setSpacingY( double spacingY )
    {
        this.spacingY = spacingY;
    }

    protected void setSpacingZ( double spacingZ )
    {
        this.spacingZ = spacingZ;
    }

    public void setTexId( int texId )
    {
        this.texId = texId;
    }

    public void setTransferTexId( int transferTexId )
    {
        this.transferTexId = transferTexId;
    }

    protected void setWidth( int width )
    {
        this.width = width;
    }
}