package de.sofd.viskit.image3D.util;

public class Image3DUtil
{
    public static int getzStride()
    {
        int zStride = 0;
        
        try
        {
            zStride = Integer.parseInt(System.getProperty("de.sofd.viskit.image3d.sliceStride"));
        }
        catch ( Exception e1 )
        {
            e1.printStackTrace();
        }
        
        if ( zStride == 0 ) zStride = 1;
        
        return zStride;
    }
}