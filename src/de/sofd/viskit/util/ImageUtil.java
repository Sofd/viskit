package de.sofd.viskit.util;

import java.awt.Color;
import java.nio.*;
import java.util.*;

import com.sun.opengl.util.*;

import de.sofd.viskit.model.Windowing;

public class ImageUtil
{
    public static FloatBuffer getTransferFunction(    Color color1,
                                                    Color color2 )
    {
        return getTransferFunction( color1, color2, 256 );
    }

    public static FloatBuffer getTransferFunction(    Color color1,
                                                    Color color2,
                                                    int nrOfElements )
    {
        FloatBuffer buf = FloatBuffer.allocate( nrOfElements * 3 );
        float[] col1 = new float[ 4 ];
        float[] col2 = new float[ 4 ];

        color1.getComponents( col1 );
        color2.getComponents( col2 );

        for ( int i = 0; i < nrOfElements; ++i )
        {
            for ( int j = 0; j < 3; ++j )
            {
                buf.put( col1[ j ] + i * ( col2[ j ] - col1[ j ] ) / ( nrOfElements - 1 ) );
            }
        }

        buf.rewind();

        return buf;
    }

    /**
     * Applies windowing to original value and maps result to [0, 1].
     * 
     * @param originalValue
     * @param windowCenter
     * @param windowWidth
     * @return windowed value.
     */
    public static float getWindowed(    short originalValue,
                                        float windowCenter,
                                        float windowWidth )
    {
        if ( originalValue < windowCenter - windowWidth / 2 )
            return 0;

        if ( originalValue > windowCenter + windowWidth / 2 )
            return 1;

        return ( originalValue - windowCenter + windowWidth / 2 ) / ( windowWidth );
    }

    /**
     * Applies windowing to original value and maps result to [0, 1].
     * 
     * @param originalValue
     * @param windowing
     *            windowing parameters.
     * @return windowed value.
     */
    public static float getWindowed(    short originalValue,
                                        Windowing windowing )
    {
        return getWindowed( originalValue, windowing.getWindowCenter(), windowing.getWindowWidth() );
    }

    public static FloatBuffer getWindowedData(    ShortBuffer dataBuf,
                                                Collection<Windowing> windowingList,
                                                int imageWidth,
                                                int imageHeight )
    {
        FloatBuffer floatbuf = BufferUtil.newFloatBuffer( dataBuf.capacity() );
        int index = 0;

        for ( Windowing windowing : windowingList )
        {
            for ( int y = 0; y < imageHeight; ++y )
            {
                for ( int x = 0; x < imageWidth; ++x )
                {
                    float value = getWindowed( dataBuf.get( index ), windowing );
                    floatbuf.put( value );
                    index++;
                }

            }
        }

        dataBuf.rewind();
        floatbuf.rewind();

        return floatbuf;
    }
}