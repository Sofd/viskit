package de.sofd.viskit.image3D.jogl.model;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

import java.awt.*;
import java.nio.*;
import java.util.ArrayList;

import javax.media.opengl.*;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import com.sun.opengl.util.*;

import de.sofd.util.*;
import de.sofd.viskit.image3D.jogl.util.*;
import de.sofd.viskit.image3D.model.*;
import de.sofd.viskit.model.*;
import de.sofd.viskit.util.*;

import vtk.*;

public class VolumeObject
{
    /**
     * Databuffer for 3D-Texture.
     */
    protected ShortBuffer dataBuf;

    protected IntRange dimRange;

    protected IntDimension3D imageDim;

    protected FloatBuffer transferFunction;
    protected int transferTexId;

    protected int transferTexPreIntegratedId;

    protected int transferSize;
    /**
     * Value range.
     */
    protected ShortRange range;

    /**
     * Minimum and maximum of width*spacingX, height*spacingY, depth*spacingZ
     */
    protected DoubleRange sizeRange;

    /**
     * Point in volume object where x-slice, y-slice and z-slice intersect
     */
    protected double[] sliceCursor;

    protected DoubleDimension3D spacing;

    protected IntDimension3D spacingDim;

    /**
     * OpenGL-Id of 3D-Texture.
     */
    protected int texId;

    /**
     * Weitere 3D-Textur (z.B. gefiltert mit Convolution-Filter ).
     */
    protected int texId2;
    
    /**
     * gradient texture
     */
    protected int gradientTex;

    protected ShortBuffer orgWindowing;
    
    protected VolumeConfig volumeConfig;

    protected ShortBuffer windowing;

    /**
     * OpenGL-Id fuer Windowingfunktion.
     */
    protected int windowingTexId;

    protected VolumeConstraint constraint;

    protected boolean loadTransferFunctionPreIntegrated = true;
    
    protected TransferIntegrationFrameBuffer tfFbo;
    
    public VolumeObject( ArrayList<DicomObject> dicomList, ShortBuffer windowing, ShortBuffer dataBuf, VolumeConfig volumeConfig,
            ShortRange range )
    {
        DicomObject refDicom = dicomList.get( 0 );

        this.volumeConfig = volumeConfig;
        VolumeBasicConfig basicConfig = volumeConfig.getBasicConfig();
        
        setImageDim( new IntDimension3D( basicConfig.getPixelWidth(), basicConfig.getPixelHeight(), basicConfig.getSlices() ));

        setSpacing( new DoubleDimension3D( refDicom.getDoubles( Tag.PixelSpacing )[ 0 ], refDicom
                .getDoubles( Tag.PixelSpacing )[ 1 ], refDicom.getDouble( Tag.SliceThickness ) ) );

        System.out.println( "spacing : " + spacing.toString() );
        System.out.println( "dimensions : " + imageDim.toString() );

        setDimRange( new IntRange( imageDim.getMin(), imageDim.getMax() ) );

        setSizeRange( new DoubleRange( Math.min( Math.min( getSizeX(), getSizeY() ), getSizeZ() ), Math.max( Math.max(
                getSizeX(), getSizeY() ), getSizeZ() ) ) );

        setDataBuf( dataBuf );

        setSliceCursor( new double[]
        {
                imageDim.getWidth() / 2, imageDim.getHeight() / 2, imageDim.getDepth() / 2
        } );

        this.orgWindowing = windowing;

        this.windowing = BufferUtil.copyShortBuffer( windowing );
        this.windowing.rewind();

        setRange( range );
        System.out.println( "range : " + range.toString() );

        this.constraint = new VolumeConstraint();

        setTransferFunction( ImageUtil.getRGBATransferFunction( Color.BLACK, Color.WHITE, 0.0f, 1.0f ) );

    }

    public VolumeObject( vtkImageData imageData, ArrayList<ShortBuffer> dataBufList, double[] sliceCursor )
    {
        int[] dim = imageData.GetDimensions();
        double[] spacing = imageData.GetSpacing();
        double[] range = imageData.GetScalarRange();

        setImageDim( new IntDimension3D( dim[ 0 ], dim[ 1 ], dim[ 2 ] ) );

        setSpacing( new DoubleDimension3D( spacing[ 0 ], spacing[ 1 ], spacing[ 2 ] ) );

        System.out.println( "spacing : " + getSpacing().toString() );

        setDimRange( new IntRange( imageDim.getMin(), imageDim.getMax() ) );

        setSizeRange( new DoubleRange( Math.min( Math.min( getSizeX(), getSizeY() ), getSizeZ() ), Math.max( Math.max(
                getSizeX(), getSizeY() ), getSizeZ() ) ) );

        setRange( new ShortRange( (short)range[ 0 ], (short)range[ 1 ] ) );

        setDataBuf( dataBuf );
        setSliceCursor( sliceCursor );

    }

    public void bindTransferTexture( GL2 gl )
    {
        gl.glBindTexture( GL_TEXTURE_1D, transferTexId );
        gl.glTexImage1D( GL_TEXTURE_1D, 0, GL_RGBA32F, transferSize, 0, GL_RGBA, GL_FLOAT, transferFunction );

    }

    public void bindTransferTexturePreIntegrated( GL2 gl ) 
    {
        gl.glBindTexture( GL_TEXTURE_2D, transferTexPreIntegratedId );
    }

    public void bindWindowingTexture( GL2 gl )
    {
        // gl.glPixelStorei( GL_UNPACK_ALIGNMENT, 1 );

        gl.glBindTexture( GL_TEXTURE_2D, windowingTexId );
        gl.glTexImage2D( GL_TEXTURE_2D, 0, GL_ALPHA16F, 2, windowing.capacity() / 2, 0, GL_ALPHA, GL_SHORT, windowing );
    }
    
    public void createTransferFbo( GL2 gl ) throws Exception
    {
        tfFbo = new TransferIntegrationFrameBuffer( ShaderManager.get( "transferIntegration" ), transferFunction, transferTexId );
        tfFbo.createTexture( gl, GL_RGBA32F, GL_RGBA );
        tfFbo.createFBO( gl );
    }

    public void createTransferTexture( GL2 gl ) throws Exception
    {
        gl.glEnable( GL_TEXTURE_1D );

        int[] texId = new int[ 1 ];
        gl.glGenTextures( 1, texId, 0 );

        transferTexId = texId[ 0 ];

        gl.glBindTexture( GL_TEXTURE_1D, transferTexId );
        gl.glTexImage1D( GL_TEXTURE_1D, 0, GL_RGBA32F, transferSize, 0, GL_RGBA, GL_FLOAT,
                transferFunction );

        gl.glTexParameteri( GL_TEXTURE_1D, GL_TEXTURE_WRAP_S, GL_CLAMP );

        gl.glTexParameteri( GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
        gl.glTexParameteri( GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );

        gl.glTexEnvf( GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE );

        gl.glDisable( GL_TEXTURE_1D );
    }
    
    public void createWindowingTexture( GL2 gl )
    {
        gl.glEnable( GL_TEXTURE_2D );

        int[] texId = new int[ 1 ];
        gl.glGenTextures( 1, texId, 0 );

        windowingTexId = texId[ 0 ];

        for ( int i = 0; i < windowing.capacity(); ++i )
            System.out.println( "win : " + windowing.get( i ) );

        // gl.glPixelStorei( GL_UNPACK_ALIGNMENT, 1 );

        gl.glBindTexture( GL_TEXTURE_2D, windowingTexId );
        gl.glTexImage2D( GL_TEXTURE_2D, 0, GL_ALPHA16F, 2, windowing.capacity() / 2, 0, GL_ALPHA, GL_SHORT, windowing );

        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP );
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP );

        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );

        // gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST
        // );
        // gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST
        // );

        gl.glTexEnvf( GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE );

        gl.glDisable( GL_TEXTURE_2D );

    }

    public VolumeConstraint getConstraint()
    {
        return constraint;
    }

    public int getCurrentImage()
    {
        return ( getCurrentSlice() / volumeConfig.getBasicConfig().getImageStride() );
    }

    public int getCurrentSlice()
    {
        return (int)getSliceCursor()[ 2 ];
    }

    public short getCurrentWindowCenter()
    {
        int z = getCurrentImage();

        return windowing.get( z * 2 );
    }

    public void getCurrentWindowing( short[] currentWindowing )
    {
        currentWindowing[ 0 ] = getCurrentWindowCenter();
        currentWindowing[ 1 ] = getCurrentWindowWidth();
    }

    public short getCurrentWindowWidth()
    {
        int z = getCurrentImage();

        return windowing.get( z * 2 + 1 );
    }

    public short getCursorValue()
    {
        double[] pos = getSliceCursor();
        
        return dataBuf.get( getCurrentImage() * imageDim.getWidth() * imageDim.getHeight() +  (int)pos[ 1 ] * imageDim.getWidth() + (int)pos[ 0 ] );
    }

    public ShortBuffer getDataBuf()
    {
        return dataBuf;
    }

    public IntRange getDimRange()
    {
        return dimRange;
    }

    public int getGradientTex()
    {
        return gradientTex;
    }

    public IntDimension3D getImageDim()
    {
        return imageDim;
    }

    public int getNrOfImages()
    {
        return ( getImageDim().getDepth() / volumeConfig.getBasicConfig().getImageStride() );
    }

    public ShortRange getRange()
    {
        return range;
    }

    public float getRelativeCursorValue()
    {
        int z = getCurrentImage();

        return Windowing.getY( getCursorValue(), windowing.get( z * 2 + 0 ), windowing.get( z * 2 + 1 ) );
    }

    public DoubleRange getSizeRange()
    {
        return sizeRange;
    }

    public double getSizeX()
    {
        if ( spacing.getWidth() == 0 )
            return imageDim.getWidth();

        return ( imageDim.getWidth() * spacing.getWidth() );
    }

    public double getSizeY()
    {
        if ( spacing.getHeight() == 0 )
            return imageDim.getHeight();

        return ( imageDim.getHeight() * spacing.getHeight() );
    }

    public double getSizeZ()
    {
        if ( spacing.getDepth() == 0 )
            return imageDim.getDepth();

        return ( imageDim.getDepth() * spacing.getDepth() );
    }

    public double[] getSliceCursor()
    {
        return sliceCursor;
    }

    public DoubleDimension3D getSpacing()
    {
        return spacing;
    }

    public int getTexId()
    {
        return texId;
    }

    public int getTexId2()
    {
        return texId2;
    }

    public FloatBuffer getTransferFunction()
    {
        return transferFunction;
    }

    public int getTransferSize()
    {
        return transferSize;
    }

    public int getTransferTexId()
    {
        return transferTexId;
    }

    public int getTransferTexPreIntegratedId()
    {
        return transferTexPreIntegratedId;
    }

    public ShortBuffer getWindowing()
    {
        return windowing;
    }

    public void loadFilteredTexture( GL2 gl, GLShader shader ) throws Exception
    {
        
        long time1 = System.currentTimeMillis();

        ConvolutionVolumeBuffer volumeBuffer = new ConvolutionVolumeBuffer( new IntDimension3D(
                imageDim.getWidth(), imageDim.getHeight(), getNrOfImages() ), shader, getTexId() );
        volumeBuffer.createTexture( gl, GL_LUMINANCE16F, GL_LUMINANCE );
        volumeBuffer.createFBO( gl );

        volumeBuffer.run( gl );

        texId2 = volumeBuffer.getTex();

        volumeBuffer.cleanUp( gl );

        long time2 = System.currentTimeMillis();

        System.out.println( "offscreen gauss filtering in " + ( time2 - time1 ) + " ms" );
        
    }

    public void loadGradientTexture( GL2 gl, GLShader shader ) throws Exception
    {
        
        long time1 = System.currentTimeMillis();

        GradientVolumeBuffer volumeBuffer = new GradientVolumeBuffer( new IntDimension3D(
                imageDim.getWidth(), imageDim.getHeight(), getNrOfImages() ), shader, getTexId2() );
        volumeBuffer.createTexture( gl, GL_RGBA32F, GL_RGBA );
        volumeBuffer.createFBO( gl );

        volumeBuffer.run( gl );

        gradientTex = volumeBuffer.getTex();

        volumeBuffer.cleanUp( gl );

        long time2 = System.currentTimeMillis();

        System.out.println( "gradient computed in " + ( time2 - time1 ) + " ms" );
        
    }
    
    public void loadTexture( GL2 gl ) 
    {
        setTexId( GLUtil.get3DTexture( gl, dataBuf, imageDim.getWidth(), imageDim.getHeight(), getNrOfImages(), true ) );
    }
    
    public void loadTransferTexturePreIntegrated( GL2 gl ) throws Exception
    {
        if ( loadTransferFunctionPreIntegrated )
        {
            loadTransferFunctionPreIntegrated = false;
            
            long time1 = System.currentTimeMillis();
            
            //bind current transferFunction to transferTexId
            bindTransferTexture( gl );
            
            tfFbo.setTransferFunction( gl, transferFunction, transferTexId );
            tfFbo.run( gl );
            transferTexPreIntegratedId = tfFbo.getTex();
            
            long time2 = System.currentTimeMillis();

            System.out.println( "transfer integration in " + ( time2 - time1 ) + " ms" );
        }
    }
    

    public synchronized void reloadOriginalWindowing()
    {
        for ( int i = 0; i < windowing.capacity(); ++i )
            windowing.put( i, orgWindowing.get( i ) );
    }

    protected void setDataBuf( ShortBuffer dataBuf )
    {
        this.dataBuf = dataBuf;
    }

    public void setDimRange( IntRange dimRange )
    {
        this.dimRange = dimRange;
    }

    public void setImageDim( IntDimension3D imageDim )
    {
        this.imageDim = imageDim;
    }

    protected void setRange( ShortRange range )
    {
        this.range = range;
    }

    public void setSizeRange( DoubleRange sizeRange )
    {
        this.sizeRange = sizeRange;
    }

    protected void setSliceCursor( double[] sliceCursor )
    {
        this.sliceCursor = sliceCursor;
    }

    public void setSpacing( DoubleDimension3D spacing )
    {
        this.spacing = spacing;
    }

    public void setTexId( int texId )
    {
        this.texId = texId;
    }

    public void setTexId2( int texId2 )
    {
        this.texId2 = texId2;
    }

    public void setTransferFunction( FloatBuffer transferFunction )
    {
        this.transferFunction = transferFunction;
        
        setTransferSize( transferFunction.capacity() / 4 );
        loadTransferFunctionPreIntegrated = true;
    }

    public void setTransferSize( int transferSize )
    {
        this.transferSize = transferSize;
    }

    public void setTransferTexId( int transferTexId )
    {
        this.transferTexId = transferTexId;
    }

    public void setTransferTexPreIntegratedId( int transferTexPreIntegratedId )
    {
        this.transferTexPreIntegratedId = transferTexPreIntegratedId;
    }

    public void setWindowing( ShortBuffer windowing )
    {
        this.windowing = windowing;
    }

    public void updateWindowCenter( short value,
                                    WindowingMode windowingMode )
    {
        int z = getCurrentImage();

        switch ( windowingMode )
        {
            case WINDOWING_MODE_LOCAL:
                windowing.put( z * 2, value );

                break;
            case WINDOWING_MODE_GLOBAL_RELATIVE:
                short delta = (short)( value - windowing.get( z * 2 ) );

                for ( int i = 0; i < getNrOfImages(); ++i )
                {
                    short orgValue = windowing.get( i * 2 );
                    windowing.put( i * 2, (short)Math
                            .max( Math.min( range.getMax(), orgValue + delta ), range.getMin() ) );
                }

                break;
            case WINDOWING_MODE_GLOBAL_ABSOLUTE:
                for ( int i = 0; i < getNrOfImages(); ++i )
                {
                    windowing.put( i * 2, value );
                }

                break;
        }

    }

    public void updateWindowing( WindowingMode windowingMode )
    {
        updateWindowCenter( getCurrentWindowCenter(), windowingMode );
        updateWindowWidth( getCurrentWindowWidth(), windowingMode );
    }

    public void updateWindowWidth(    short value,
                                    WindowingMode windowingMode )
    {
        int z = getCurrentImage();

        switch ( windowingMode )
        {
            case WINDOWING_MODE_LOCAL:
                windowing.put( z * 2 + 1, value );

                break;
            case WINDOWING_MODE_GLOBAL_RELATIVE:
                short delta = (short)( value - windowing.get( z * 2 + 1 ) );

                for ( int i = 0; i < getNrOfImages(); ++i )
                {
                    short orgValue = windowing.get( i * 2 + 1 );
                    windowing.put( i * 2 + 1, (short)Math.max( Math.min( range.getMax(), orgValue + delta ), range
                            .getMin() ) );
                }

                break;
            case WINDOWING_MODE_GLOBAL_ABSOLUTE:
                for ( int i = 0; i < getNrOfImages(); ++i )
                {
                    windowing.put( i * 2 + 1, value );
                }

                break;
        }

    }
}