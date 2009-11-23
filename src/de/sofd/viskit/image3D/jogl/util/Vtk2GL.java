package de.sofd.viskit.image3D.jogl.util;

import static de.sofd.viskit.image3D.jogl.util.GLUtil.*;
import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2ES1.*;
import static javax.media.opengl.GL2GL3.*;

import java.nio.*;

import javax.media.opengl.*;

import org.apache.log4j.*;

import vtk.*;

import com.sun.opengl.util.*;

public class Vtk2GL
{
    static final Logger logger = Logger.getLogger(Vtk2GL.class);
        
    public static int get3DTexture(GL2 gl, vtkImageData imageData) {
        int[] texId = new int[1];
        
        int[] dim = imageData.GetDimensions();
        double[] range = imageData.GetScalarRange();
        double rangeDist = range[1] - range[0];
        rangeDist = ( rangeDist > 0 ? rangeDist : 1 );
        
        logger.info("scalar range [" + range[0] + ", " + range[1] + "]");
        logger.info("dims : " + dim[0] + " " + dim[1] + " " + dim[2] );
        
        //ByteBuffer dataBuf = BufferUtil.newByteBuffer(dim[0]*dim[1]*dim[2]);
        FloatBuffer dataBuf = BufferUtil.newFloatBuffer(128*128*128);
        /*for ( int z = 0; z < dim[2]; ++z)
            for ( int y = 0; y < dim[1]; ++y)
                for ( int x = 0; x < dim[0]; ++x)
                    dataBuf.put((byte)(256*imageData.GetScalarComponentAsFloat(x, y, z, 0)/rangeDist));*/
        
        
        for ( int z = 0; z < 128; ++z)
            for ( int y = 0; y < 128; ++y)
                for ( int x = 0; x < 128; ++x)
                    dataBuf.put((float)(imageData.GetScalarComponentAsFloat(x%dim[0], y%dim[1], z%dim[2], 0)/rangeDist));
        
        dataBuf.rewind();
        
        gl.glPixelStorei(GL_UNPACK_ALIGNMENT, 1); 
        
        logi(gl, "GL_UNPACK_ROW_LENGTH", GL_UNPACK_ROW_LENGTH);
        logi(gl, "GL_UNPACK_IMAGE_HEIGHT", GL_UNPACK_IMAGE_HEIGHT);
        logi(gl, "GL_UNPACK_SKIP_IMAGES", GL_UNPACK_SKIP_IMAGES);
        logi(gl, "GL_MAX_TEXTURE_SIZE", GL_MAX_TEXTURE_SIZE);
        logi(gl, "GL_MAX_3D_TEXTURE_SIZE", GL_MAX_3D_TEXTURE_SIZE);
        
        gl.glGenTextures(1, texId, 0);
        gl.glBindTexture(GL_TEXTURE_3D, texId[0]);
        logger.info("pass1");
        gl.glTexImage3D(GL_TEXTURE_3D, 0, GL_ALPHA, 128, 128, 128, 0, GL_ALPHA, GL_FLOAT, dataBuf);
        logger.info("pass2");
        //gl.glTexImage3D(GL_TEXTURE_3D, 0, GL_ALPHA, dim[0], dim[1], dim[2], 0, GL_ALPHA, GL_FLOAT, dataBuf);
        
        /*gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER );
        gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER );
        gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_BORDER );*/
              
        gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_REPEAT );
        gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_REPEAT );
        gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_REPEAT );
        
        /*gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, 
                         GL_LINEAR );
        gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, 
                         GL_LINEAR );  */
        gl.glTexEnvf(GL_TEXTURE_ENV , GL_TEXTURE_ENV_MODE, GL_MODULATE);
        
        return texId[0];
    }
    
    public static int[] get2DTexturStack(GL2 gl, vtkImageData imageData) {
        int[] dim = imageData.GetDimensions();
        double[] range = imageData.GetScalarRange();
        double rangeDist = range[1] - range[0];
        rangeDist = ( rangeDist > 0 ? rangeDist : 1 );
        
        int[] texIds = new int[dim[2]];
        
        logger.info("scalar range [" + range[0] + ", " + range[1] + "]");
        logger.info("dims : " + dim[0] + " " + dim[1] + " " + dim[2] );
        
        for ( int z = 0; z < dim[2]; ++z )
        {
            FloatBuffer dataBuf = BufferUtil.newFloatBuffer(dim[0]*dim[1]);
            
            for ( int y = 0; y < dim[1]; ++y)
                for ( int x = 0; x < dim[0]; ++x)
                    dataBuf.put((float)(imageData.GetScalarComponentAsFloat(x, y, z, 0)/rangeDist));
        
            dataBuf.rewind();
        
            gl.glPixelStorei(GL_UNPACK_ALIGNMENT, 1); 
        
            int[] texid = new int[1];
            gl.glGenTextures(1, texid, 0);
            texIds[z] = texid[0];
            gl.glBindTexture(GL_TEXTURE_2D, texIds[z]);
            gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, dim[0], dim[1], 0, GL_ALPHA, GL_FLOAT, dataBuf);
        
            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER );
            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER );
            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_BORDER );
              
            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );
            gl.glTexEnvf(GL_TEXTURE_ENV , GL_TEXTURE_ENV_MODE, GL_MODULATE);
        }
        
        return texIds;
    }
}