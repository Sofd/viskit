package de.sofd.viskit.image3D.jogl.util;

import static de.sofd.viskit.image3D.jogl.util.GLUtil.*;
import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2ES1.*;
import static javax.media.opengl.GL2GL3.*;

import java.nio.*;
import java.util.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;

import org.dcm4che2.data.*;

import com.sun.opengl.util.*;

public class Dcm4che2GL
{
    public static int[] get2DTexturStack(GL2 gl, GLU glu, Collection<DicomObject> dicomList) throws Exception {
        
        int colors = 1;
        
        if ( colors != 1 && colors != 3 )
            throw new Exception("unsupported color format : " + colors);
        
        //double[] range = new double[3];
        //double rangeDist = range[1] - range[0];
        //rangeDist = ( rangeDist > 0 ? rangeDist : 1 );
        
        int[] texIds = new int[dicomList.size()];
        
        //logger.info("scalar range [" + range[0] + ", " + range[1] + "]");
        //logger.info("dims : " + dim[0] + " " + dim[1] + " " + dim[2] );
        
        logi(gl, "GL_UNPACK_ROW_LENGTH", GL_UNPACK_ROW_LENGTH);
        logi(gl, "GL_UNPACK_IMAGE_HEIGHT", GL_UNPACK_IMAGE_HEIGHT);
        logi(gl, "GL_UNPACK_SKIP_IMAGES", GL_UNPACK_SKIP_IMAGES);
        logi(gl, "GL_MAX_TEXTURE_SIZE", GL_MAX_TEXTURE_SIZE);
        logi(gl, "GL_MAX_3D_TEXTURE_SIZE", GL_MAX_3D_TEXTURE_SIZE);
        
        int z=0;
        for ( DicomObject dicomObject : dicomList )
        {
            int width = dicomObject.getInt(Tag.Columns);
            int height = dicomObject.getInt(Tag.Rows);
            ByteBuffer dataBuf = BufferUtil.newByteBuffer(width*height*dicomObject.size());
                        
            dataBuf.put(dicomObject.getBytes(Tag.PixelData));
            
            dataBuf.rewind();
        
            gl.glPixelStorei(GL_UNPACK_ALIGNMENT, 1); 
        
            int[] texid = new int[1];
            gl.glGenTextures(1, texid, 0);
            texIds[z] = texid[0];
            gl.glBindTexture(GL_TEXTURE_2D, texIds[z]);
                        
            glu.gluBuild2DMipmaps(GL_TEXTURE_2D, GL_BYTE, width, height, GL_ALPHA, GL_BYTE, dataBuf);
            
            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER );
            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER );
            
            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );
            gl.glTexEnvf(GL_TEXTURE_ENV , GL_TEXTURE_ENV_MODE, GL_MODULATE);
            
            z++;
        }
        
        return texIds;
    }
    
}