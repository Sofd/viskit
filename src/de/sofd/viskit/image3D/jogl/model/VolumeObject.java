package de.sofd.viskit.image3D.jogl.model;

import java.nio.*;
import java.util.ArrayList;

import javax.media.opengl.*;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import de.sofd.viskit.image3D.jogl.util.GLUtil;
import de.sofd.viskit.model.Windowing;
import de.sofd.viskit.util.*;

import vtk.*;

public class VolumeObject
{
    /**
     * Databuffer for 3D-Texture.
     */
    protected ShortBuffer dataBuf;

    protected int depth;

    protected int height;

    protected int maxDim;

    /**
     * Maximum of width*spacingX, height*spacingY, depth*spacingZ
     */
    protected double maxSize;

    protected int minDim;
    /**
     * Minimum of width*spacingX, height*spacingY, depth*spacingZ
     */
    protected double minSize;
    protected double rangeMax;
    /**
     * Value range.
     */
    protected double rangeMin;
    /**
     * Point in volume object where x-slice, y-slice and z-slice intersect
     */
    protected int[] sliceCursor;

    protected double spacingX;
    protected double spacingY;
    protected double spacingZ;

    /**
     * OpenGL-Id of 3D-Texture.
     */
    protected int texId;

    /**
     * OpenGL-Id fuer Transferfunktion.
     */
    protected int transferTexId;

    protected int width;

    protected ArrayList<Windowing> windowing;

    public VolumeObject( ArrayList<DicomObject> dicomList, ArrayList<Windowing> windowing, ShortBuffer dataBuf )
    {
        DicomObject refDicom = dicomList.get( 0 );

        int[] dim = new int[ 3 ];
        dim[ 0 ] = refDicom.getInt( Tag.Columns );
        dim[ 1 ] = refDicom.getInt( Tag.Rows );
        dim[ 2 ] = dicomList.size();

        double[] spacing = new double[ 3 ];
        spacing[ 0 ] = refDicom.getDoubles( Tag.PixelSpacing )[ 0 ];
        spacing[ 1 ] = refDicom.getDoubles( Tag.PixelSpacing )[ 1 ];
        spacing[ 2 ] = refDicom.getDouble( Tag.SliceThickness );

        System.out.println( "spacing : " + spacing[ 0 ] + ", " + spacing[ 1 ] + ", " + spacing[ 2 ] );
        System.out.println( "dimensions : " + dim[ 0 ] + ", " + dim[ 1 ] + ", " + dim[ 2 ] );
        
        setWidth( dim[ 0 ] );
        setHeight( dim[ 1 ] );
        setDepth( dim[ 2 ] );

        setSpacingX( spacing[ 0 ] );
        setSpacingY( spacing[ 1 ] );
        setSpacingZ( spacing[ 2 ] );

        setMaxDim( Math.max( Math.max( dim[ 0 ], dim[ 1 ] ), dim[ 2 ] ) );
        setMinDim( Math.min( Math.max( dim[ 0 ], dim[ 1 ] ), dim[ 2 ] ) );

        setMaxSize( Math.max( Math.max( getSizeX(), getSizeY() ), getSizeZ() ) );
        setMinSize( Math.min( Math.max( getSizeX(), getSizeY() ), getSizeZ() ) );

        setDataBuf( dataBuf );
        
        setSliceCursor( new int[]
        {
                dim[ 0 ] / 2, dim[ 1 ] / 2, dim[ 2 ] / 2
        } );
        
        setWindowing( windowing );

    }

    public VolumeObject( vtkImageData imageData, ShortBuffer dataBuf, int[] sliceCursor )
    {
        int[] dim = imageData.GetDimensions();
        double[] spacing = imageData.GetSpacing();
        double[] range = imageData.GetScalarRange();
        System.out.println( "spacing : " + spacing[ 0 ] + ", " + spacing[ 1 ] + ", " + spacing[ 2 ] );

        setWidth( dim[ 0 ] );
        setHeight( dim[ 1 ] );
        setDepth( dim[ 2 ] );

        setSpacingX( spacing[ 0 ] );
        setSpacingY( spacing[ 1 ] );
        setSpacingZ( spacing[ 2 ] );

        setMaxDim( Math.max( Math.max( dim[ 0 ], dim[ 1 ] ), dim[ 2 ] ) );
        setMinDim( Math.min( Math.max( dim[ 0 ], dim[ 1 ] ), dim[ 2 ] ) );

        setMaxSize( Math.max( Math.max( getSizeX(), getSizeY() ), getSizeZ() ) );
        setMinSize( Math.min( Math.max( getSizeX(), getSizeY() ), getSizeZ() ) );

        setRangeMin( range[ 0 ] );
        setRangeMax( range[ 1 ] );

        setDataBuf( dataBuf );
        setSliceCursor( sliceCursor );

    }

    public short getCursorValue()
    {
        int[] pos = getSliceCursor();
        return dataBuf.get( pos[ 2 ] * getWidth() * getHeight() + pos[ 1 ] * getWidth() + pos[ 0 ] );
    }

    public ShortBuffer getDataBuf()
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
        if ( spacingX == 0 )
            return width;

        return ( width * spacingX );
    }

    public double getSizeY()
    {
        if ( spacingY == 0 )
            return height;

        return ( height * spacingY );
    }

    public double getSizeZ()
    {
        if ( spacingZ == 0 )
            return depth;

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

    public ArrayList<Windowing> getWindowing()
    {
        return windowing;
    }

    public void loadTexture( GL2 gl )
    {
        FloatBuffer floatbuf = ImageUtil.getWindowedData( dataBuf, windowing, getWidth(), getHeight() );

        setTexId( GLUtil.get3DTexture( gl, floatbuf, getWidth(), getHeight(), getDepth(), true ) );
    }

    protected void setDataBuf( ShortBuffer dataBuf )
    {
        this.dataBuf = dataBuf;
    }

    protected void setDepth( int depth )
    {
        System.out.println( "depth " + depth );
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

    public void setWindowing( ArrayList<Windowing> windowing )
    {
        this.windowing = windowing;
    }
}