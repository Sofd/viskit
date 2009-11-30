package de.sofd.viskit.image3D.jogl.util;

import static de.sofd.viskit.image3D.jogl.util.GLUtil.*;
import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2ES1.*;
import static javax.media.opengl.GL2GL3.*;

import java.nio.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;

import org.apache.log4j.*;

import vtk.*;

import com.sun.opengl.util.*;

public class Vtk2GL
{
    static final Logger logger = Logger.getLogger(Vtk2GL.class);
        
    public static int[] get2DTexturStack(GL2 gl, GLU glu, vtkImageData imageData) throws Exception {
        int colors = imageData.GetNumberOfScalarComponents();
        
        if ( colors != 1 && colors != 3 )
            throw new Exception("unsupported color format : " + colors);
        
        int[] dim = imageData.GetDimensions();
        double[] range = imageData.GetScalarRange();
        double rangeDist = range[1] - range[0];
        rangeDist = ( rangeDist > 0 ? rangeDist : 1 );
        
        int[] texIds = new int[dim[2]];
        
        logger.info("scalar range [" + range[0] + ", " + range[1] + "]");
        logger.info("dims : " + dim[0] + " " + dim[1] + " " + dim[2] );
        
        logi(gl, "GL_UNPACK_ROW_LENGTH", GL_UNPACK_ROW_LENGTH);
        logi(gl, "GL_UNPACK_IMAGE_HEIGHT", GL_UNPACK_IMAGE_HEIGHT);
        logi(gl, "GL_UNPACK_SKIP_IMAGES", GL_UNPACK_SKIP_IMAGES);
        logi(gl, "GL_MAX_TEXTURE_SIZE", GL_MAX_TEXTURE_SIZE);
        logi(gl, "GL_MAX_3D_TEXTURE_SIZE", GL_MAX_3D_TEXTURE_SIZE);
        
        for ( int z = 0; z < dim[2]; ++z )
        {
            FloatBuffer dataBuf = BufferUtil.newFloatBuffer(dim[0]*dim[1]*colors);
            
            for ( int y = 0; y < dim[1]; ++y)
            {
                for ( int x = 0; x < dim[0]; ++x)
                {
                    for ( int k = 0; k < colors; ++k )
                    {
                        if ( colors == 1 )
                            dataBuf.put((float)(imageData.GetScalarComponentAsFloat(x, y, z, k)/rangeDist));
                        else
                            dataBuf.put((float)(imageData.GetScalarComponentAsFloat(x, y, z, k)/255.0f));
                    }
                }
            }
            dataBuf.rewind();
        
            gl.glPixelStorei(GL_UNPACK_ALIGNMENT, 1); 
        
            int[] texid = new int[1];
            gl.glGenTextures(1, texid, 0);
            texIds[z] = texid[0];
            gl.glBindTexture(GL_TEXTURE_2D, texIds[z]);
                        
            if ( colors == 1 )
            {
                //gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, dim[0], dim[1], 0, GL_ALPHA, GL_FLOAT, dataBuf);
                glu.gluBuild2DMipmaps(GL_TEXTURE_2D, GL_ALPHA, dim[0], dim[1], GL_ALPHA, GL_FLOAT, dataBuf);
            }
            else if ( colors == 3 )
            {
                //gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, dim[0], dim[1], 0, GL_RGB, GL_FLOAT, dataBuf);
                glu.gluBuild2DMipmaps(GL_TEXTURE_2D, GL_RGB, dim[0], dim[1], GL_RGB, GL_FLOAT, dataBuf);
            }
                
            
            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER );
            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER );
            
            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );
            gl.glTexEnvf(GL_TEXTURE_ENV , GL_TEXTURE_ENV_MODE, GL_MODULATE);
        }
        
        return texIds;
    }
    
    public static int get3DTexture(GL2 gl, vtkImageData imageData, boolean trilinear) {
        FloatBuffer dataBuf = getFilledFloatBuffer(imageData);
        
        int[] dim = imageData.GetDimensions();
        
        return get3DTexture( gl, dataBuf, dim[0], dim[1], dim[2], trilinear );
    }
    
    public static int get3DTexture(GL2 gl, FloatBuffer dataBuf, int width, int height, int depth, boolean trilinear) {
        int[] texId = new int[1];
        
        gl.glPixelStorei(GL_UNPACK_ALIGNMENT, 1); 
        
        logi(gl, "GL_UNPACK_ROW_LENGTH", GL_UNPACK_ROW_LENGTH);
        logi(gl, "GL_UNPACK_IMAGE_HEIGHT", GL_UNPACK_IMAGE_HEIGHT);
        logi(gl, "GL_UNPACK_SKIP_IMAGES", GL_UNPACK_SKIP_IMAGES);
        logi(gl, "GL_MAX_TEXTURE_SIZE", GL_MAX_TEXTURE_SIZE);
        logi(gl, "GL_MAX_3D_TEXTURE_SIZE", GL_MAX_3D_TEXTURE_SIZE);
        
        gl.glEnable(GL_TEXTURE_3D);
        
        gl.glGenTextures(1, texId, 0);
        gl.glBindTexture(GL_TEXTURE_3D, texId[0]);
        gl.glTexImage3D(GL_TEXTURE_3D, 0, GL_ALPHA, width, height, depth, 0, GL_ALPHA, GL_FLOAT, dataBuf);
        
        gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER );
        gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER );
        gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_BORDER );
           
        if ( trilinear )
        {
            gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
            gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR ); 
        }
        else
        {
            gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_NEAREST );
            gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_NEAREST ); 
        }
        
        gl.glTexEnvf(GL_TEXTURE_ENV , GL_TEXTURE_ENV_MODE, GL_MODULATE);
        
        gl.glDisable(GL_TEXTURE_3D);
        
        return texId[0];
    }
    
    public static FloatBuffer getFilledFloatBuffer(vtkImageData imageData) {
        int[] dim = imageData.GetDimensions();
        double[] range = imageData.GetScalarRange();
        double rangeDist = range[1] - range[0];
        rangeDist = ( rangeDist > 0 ? rangeDist : 1 );
        
        FloatBuffer dataBuf = BufferUtil.newFloatBuffer(dim[0]*dim[1]*dim[2]);
        
        //this is slow
        float oldRate=0;
        float newRate=0;
        for ( int z = 0; z < dim[2]; ++z)
            for ( int y = 0; y < dim[1]; ++y)
                for ( int x = 0; x < dim[0]; ++x)
                {
                    newRate = ( z * dim[1] * dim[0] + y * dim[0] + x ) * 100.0f / ( dim[0] * dim[1] * dim[2] );
                    
                    if ((int)(oldRate) % 10 != 0 && (int)(newRate) % 10 == 0 )
                        System.out.println( "" + (int)newRate + " % of 3d texture loaded" );
                    
                    dataBuf.put((float)((imageData.GetScalarComponentAsFloat(x, y, z, 0)-range[0])/rangeDist));
                    oldRate = newRate;
                }
        
        dataBuf.rewind();
        
        return dataBuf;
    }
}