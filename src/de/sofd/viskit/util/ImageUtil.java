package de.sofd.viskit.util;

import java.awt.Color;
import java.nio.*;
import java.util.*;

import com.sun.opengl.util.*;

import de.sofd.viskit.model.*;

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

    public static FloatBuffer getTranferredData(    ShortBuffer dataBuf,
                                                Collection<ITransferFunction> transferFunctionList,
                                                int imageWidth,
                                                int imageHeight )
    {
        FloatBuffer floatbuf = BufferUtil.newFloatBuffer( dataBuf.capacity() );
        int index = 0;

        for ( ITransferFunction transferFunction : transferFunctionList )
        {
            for ( int y = 0; y < imageHeight; ++y )
            {
                for ( int x = 0; x < imageWidth; ++x )
                {
                    float value = transferFunction.getY(dataBuf.get( index ));
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