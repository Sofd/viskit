package de.sofd.viskit.image3D.filter;

import java.nio.*;

public class FilterUtil
{
    public static FilterKernel3D getGaussFilter( int dim )
    {
        int center = dim / 2;

        float[] values = new float[ dim * dim * dim ];

        double div = ( dim - 1 ) * 3.0 / 4.0;

        for ( int x = 0; x < dim; ++x )
            for ( int y = 0; y < dim; ++y )
                for ( int z = 0; z < dim; ++z )
                {
                    double arg = Math.sqrt( Math.abs( x - center ) + Math.abs( y - center ) + Math.abs( z - center ) );
                    values[ z * dim * dim + y * dim + x ] = (float)Math.exp( -arg * arg / div );
                }

        FilterKernel3D kernel = new FilterKernel3D( values, dim );
        kernel.normalize();

        return kernel;
    }
    
    protected static int clamp(    int value,
                                   int min,
                                   int max )
    {
        return Math.min( Math.max( value, min ), max );
    }

    public static ShortBuffer getFiltered(    ShortBuffer buffer,
                                            int width,
                                            int height,
                                            int depth,
                                            FilterKernel3D kernel )
    {
        
        
        int bufferSize = buffer.capacity();
        
        short[] values = new short[bufferSize];
        
        buffer.get( values );
        
        int kernelDim = kernel.getDim();
        int kernelCenter = kernelDim / 2;

        float[] kernelValues = kernel.getValues();
        
        short[] filteredBuffer = new short[bufferSize];
        
        long time1 = System.currentTimeMillis();
        
        for ( int z = kernelCenter; z < depth - kernelCenter; ++z )
        {
            int zMin = z - kernelCenter;
            int zIndex = z * width * height;
            
            for ( int y = kernelCenter; y < height - kernelCenter; ++y )
            {
                int yMin = y - kernelCenter;
                int zyIndex = zIndex + y * width;
                
                for ( int x = kernelCenter; x < width - kernelCenter; ++x )
                {
                    int xMin = x - kernelCenter;
                    
                    double newValue = 0;
                    int kIndex = 0;

                    for ( int kz = 0; kz < kernelDim; ++kz )
                    {
                        int vz = zMin + kz;
                        int vzIndex = vz * width * height;
                        
                        for ( int ky = 0; ky < kernelDim; ++ky )
                        {
                            int vy = yMin + ky;
                            int vzyIndex = vzIndex + vy * width;
                            
                            for ( int kx = 0; kx < kernelDim; ++kx )
                            {
                                int vx = xMin + kx;
//                                float q = kernelValues[ kIndex++ ];
//                                int h = values[ vzyIndex + vx ];
                                
                                newValue += kernelValues[ kIndex++ ] * values[ vzyIndex + vx ];
                            }
                        }
                    }

                    //filteredBuffer[ zyIndex + x ] = (short)newValue;
                }
            }
        }
        
        long time2 = System.currentTimeMillis();
        
        
        ShortBuffer retBuffer = ShortBuffer.allocate( filteredBuffer.length );
        retBuffer.put( filteredBuffer );
        retBuffer.rewind();
        
        System.out.println("gauss filtering in " + (time2-time1) + " ms");
        
        return retBuffer;
    }
}