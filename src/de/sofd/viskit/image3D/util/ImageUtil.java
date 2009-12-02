package de.sofd.viskit.image3D.util;

import java.awt.*;
import java.nio.*;

public class ImageUtil
{
    public static FloatBuffer getTransferFunction( Color color1, Color color2 )
    {
        return getTransferFunction( color1, color2, 256 );
    }
    
    public static FloatBuffer getTransferFunction( Color color1, Color color2, int nrOfElements )
    {
        FloatBuffer buf = FloatBuffer.allocate( nrOfElements * 3 );
        float[] col1 = new float[4];
        float[] col2 = new float[4];
        
        color1.getComponents(col1);
        color2.getComponents(col2);
        
        for ( int i = 0; i < nrOfElements; ++i )
        {
            for ( int j = 0; j < 3; ++j )
            {
                buf.put( col1[j] + i * ( col2[j] - col1[j] ) / ( nrOfElements - 1 ) );
            }
        }
        
        buf.rewind();
        
        return buf;
    }
}