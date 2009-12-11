package de.sofd.viskit.image3D.jogl.util;

import static com.sun.opengl.util.gl2.GLUT.*;
import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

import java.awt.*;
import java.nio.*;
import java.util.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;

import org.apache.log4j.*;
import org.dcm4che2.data.*;

import com.sun.opengl.util.*;
import com.sun.opengl.util.gl2.*;

import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.model.*;
import de.sofd.viskit.util.*;

public class GLUtil
{
    static final Logger logger = Logger.getLogger( GLUtil.class );

    public static void beginInfoScreen( GL2 gl,
                                        GLU glu,
                                        int winWidth,
                                        int winHeight )
    {
        beginInfoScreen( gl, glu, winWidth, winHeight, true, false, null );
    }
    
    public static int[] get2DTexturStack(    GL2 gl,
            GLU glu,
            ArrayList<DicomObject> dicomList,
            VolumeObject volumeObject ) throws Exception
    {
        
        int[] texIds = new int[ dicomList.size() ];
        
        logi( gl, "GL_UNPACK_ROW_LENGTH", GL_UNPACK_ROW_LENGTH );
        logi( gl, "GL_UNPACK_IMAGE_HEIGHT", GL_UNPACK_IMAGE_HEIGHT );
        logi( gl, "GL_UNPACK_SKIP_IMAGES", GL_UNPACK_SKIP_IMAGES );
        logi( gl, "GL_MAX_TEXTURE_SIZE", GL_MAX_TEXTURE_SIZE );
        logi( gl, "GL_MAX_3D_TEXTURE_SIZE", GL_MAX_3D_TEXTURE_SIZE );
        
        ArrayList<ITransferFunction> windowing = DicomUtil.getWindowing( dicomList );
        
        gl.glPixelStorei( GL_UNPACK_ALIGNMENT, 1 );
        
        int z = 0;
        for ( DicomObject dicomObject : dicomList )
        {
            ShortBuffer dataBuf = DicomUtil.getFilledShortBuffer( dicomObject );
            
            FloatBuffer floatbuf = ImageUtil.getTranferredData(dataBuf, windowing.get(z));
        
            int[] texid = new int[ 1 ];
            gl.glGenTextures( 1, texid, 0 );
            texIds[z] = texid[0];
            gl.glBindTexture( GL_TEXTURE_2D, texIds[z] );
            
            glu.gluBuild2DMipmaps( GL_TEXTURE_2D, GL_ALPHA, volumeObject.getImageWidth(), volumeObject.getImageHeight(), GL_ALPHA, GL_FLOAT, floatbuf );
                    
            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER );
            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER );
            
            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );
            gl.glTexEnvf( GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE );
            
            z++;
        }
        
        return texIds;
    }

    public static int get3DTexture( GL2 gl,
                                    ShortBuffer dataBuf,
                                    int width,
                                    int height,
                                    int depth,
                                    boolean trilinear )
    {
        int[] texId = new int[ 1 ];

        gl.glPixelStorei( GL_UNPACK_ALIGNMENT, 1 );

        logi( gl, "GL_UNPACK_ROW_LENGTH", GL_UNPACK_ROW_LENGTH );
        logi( gl, "GL_UNPACK_IMAGE_HEIGHT", GL_UNPACK_IMAGE_HEIGHT );
        logi( gl, "GL_UNPACK_SKIP_IMAGES", GL_UNPACK_SKIP_IMAGES );
        logi( gl, "GL_MAX_TEXTURE_SIZE", GL_MAX_TEXTURE_SIZE );
        logi( gl, "GL_MAX_3D_TEXTURE_SIZE", GL_MAX_3D_TEXTURE_SIZE );

        gl.glEnable( GL_TEXTURE_3D );

        gl.glGenTextures( 1, texId, 0 );
        gl.glBindTexture( GL_TEXTURE_3D, texId[ 0 ] );
        gl.glTexImage3D( GL_TEXTURE_3D, 0, GL_ALPHA, width, height, depth, 0, GL_ALPHA, GL_SHORT, dataBuf );

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

        gl.glTexEnvf( GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE );

        gl.glDisable( GL_TEXTURE_3D );

        return texId[ 0 ];
    }
    
    public static int get3DTexture( GL2 gl,
                                    FloatBuffer dataBuf,
                                    int width,
                                    int height,
                                    int depth,
                                    boolean trilinear )
    {
        int[] texId = new int[ 1 ];

        gl.glPixelStorei( GL_UNPACK_ALIGNMENT, 1 );

        logi( gl, "GL_UNPACK_ROW_LENGTH", GL_UNPACK_ROW_LENGTH );
        logi( gl, "GL_UNPACK_IMAGE_HEIGHT", GL_UNPACK_IMAGE_HEIGHT );
        logi( gl, "GL_UNPACK_SKIP_IMAGES", GL_UNPACK_SKIP_IMAGES );
        logi( gl, "GL_MAX_TEXTURE_SIZE", GL_MAX_TEXTURE_SIZE );
        logi( gl, "GL_MAX_3D_TEXTURE_SIZE", GL_MAX_3D_TEXTURE_SIZE );

        gl.glEnable( GL_TEXTURE_3D );

        gl.glGenTextures( 1, texId, 0 );
        gl.glBindTexture( GL_TEXTURE_3D, texId[ 0 ] );
        gl.glTexImage3D( GL_TEXTURE_3D, 0, GL_ALPHA, width, height, depth, 0, GL_ALPHA, GL_FLOAT, dataBuf );

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

        gl.glTexEnvf( GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE );

        gl.glDisable( GL_TEXTURE_3D );

        return texId[ 0 ];
    }

    public static void beginInfoScreen( GL2 gl,
                                        GLU glu,
                                        int winWidth,
                                        int winHeight,
                                        boolean upDown,
                                        boolean getP,
                                        double[] P )
    {
        // draw info text
        gl.glMatrixMode( GL_PROJECTION );
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluOrtho2D( 0, winWidth, ( upDown ? 0 : winHeight ), ( upDown ? winHeight : 0 ) );

        if ( getP )
            gl.glGetDoublev( GL_PROJECTION_MATRIX, P, 0 );

        gl.glMatrixMode( GL_MODELVIEW );
        gl.glPushMatrix();
        gl.glLoadIdentity();

        // glColor3f(1.0f, 1.0f, 0.0f);
    }

    public static void endInfoScreen( GL2 gl )
    {
        gl.glPopMatrix();

        gl.glMatrixMode( GL_PROJECTION );

        gl.glPopMatrix();
        gl.glMatrixMode( GL_MODELVIEW );
    }

    public static int getTransferTexture(    GL2 gl,
                                            Color color1,
                                            Color color2 )
    {
        return getTransferTexture( gl, color1, color2, 256 );
    }

    public static int getTransferTexture(    GL2 gl,
                                            Color color1,
                                            Color color2,
                                            int nrOfElements )
    {
        FloatBuffer transferBuf = ImageUtil.getTransferFunction( color1, color2, nrOfElements );

        int transferTex = getTransferTexture( gl, transferBuf );

        return transferTex;
    }

    public static int getTransferTexture(    GL2 gl,
                                            FloatBuffer texBuf )
    {
        gl.glPixelStorei( GL_UNPACK_ALIGNMENT, 1 );

        int theTex[] = new int[ 1 ];
        gl.glGenTextures( 1, theTex, 0 );

        gl.glBindTexture( GL_TEXTURE_1D, theTex[ 0 ] );
        gl.glTexImage1D( GL_TEXTURE_1D, 0, GL_RGB, texBuf.capacity() / 3, 0, GL_RGB, GL_FLOAT, texBuf );

        gl.glTexParameteri( GL_TEXTURE_1D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER );

        gl.glTexParameteri( GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
        gl.glTexParameteri( GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );

        gl.glTexEnvf( GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE );

        return theTex[ 0 ];
    }

    public static void infoText(    GL2 gl,
                                    GLUT glut,
                                    int posX,
                                    int posY,
                                    String message )
    {
        gl.glRasterPos2i( posX, posY );
        glut.glutBitmapString( BITMAP_8_BY_13, message );
    }

    public static int initScreenTex(    GL2 gl,
                                        int width,
                                        int height )
    {
        FloatBuffer texBuf = BufferUtil.newFloatBuffer( width * height * 3 );

        for ( int i = 0; i < width; ++i )
            for ( int j = 0; j < height; ++j )
                for ( int k = 0; k < 3; ++k )
                    texBuf.put( 0 );

        texBuf.rewind();

        gl.glPixelStorei( GL_UNPACK_ALIGNMENT, 1 );

        int theTex[] = new int[ 1 ];
        gl.glGenTextures( 1, theTex, 0 );

        gl.glBindTexture( GL_TEXTURE_2D, theTex[ 0 ] );
        gl.glTexImage2D( GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_FLOAT, texBuf );

        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP );
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP );

        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST );
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST );

        return theTex[ 0 ];

    }

    public static void lineQuad(    GL2 gl,
                                    float xBias,
                                    float yBias,
                                    float sizeX,
                                    float sizeY )
    {
        float vx1 = xBias;
        float vx2 = xBias + sizeX;
        float vy1 = yBias;
        float vy2 = yBias + sizeY;

        gl.glBegin( GL_LINE_STRIP );
        gl.glVertex2f( vx1, vy1 );
        gl.glVertex2f( vx2, vy1 );
        gl.glVertex2f( vx2, vy2 );
        gl.glVertex2f( vx1, vy2 );
        gl.glVertex2f( vx1, vy1 );
        gl.glEnd();

    }

    public static void logi(    GL2 gl,
                                String paramName,
                                int param )
    {
        int value[] = new int[ 1 ];

        gl.glGetIntegerv( param, value, 0 );

        logger.info( paramName + " : " + value[ 0 ] );
    }

    public static void texQuad1D(    GL2 gl,
                                    float biasX,
                                    float biasY,
                                    float sizeX,
                                    float sizeY,
                                    boolean isVertical )
    {
        texQuad1D( gl, biasX, biasY, sizeX, sizeY, 0, 1, isVertical );
    }

    public static void texQuad1D(    GL2 gl,
                                    float biasX,
                                    float biasY,
                                    float sizeX,
                                    float sizeY,
                                    float tBiasX,
                                    float tSizeX,
                                    boolean isVertical )
    {
        float vx1 = biasX;
        float vx2 = biasX + sizeX;
        float vy1 = biasY;
        float vy2 = biasY + sizeY;

        float tx1 = tBiasX;
        float tx2 = tBiasX + tSizeX;

        gl.glBegin( GL_QUADS );
        gl.glNormal3f( 0.0f, 0.0f, 1.0f );

        gl.glTexCoord1f( tx1 );
        gl.glVertex2f( vx1, vy1 );

        if ( isVertical )
            gl.glTexCoord1f( tx1 );
        else
            gl.glTexCoord1f( tx2 );

        gl.glVertex2f( vx2, vy1 );

        gl.glTexCoord1f( tx2 );
        gl.glVertex2f( vx2, vy2 );

        if ( isVertical )
            gl.glTexCoord1f( tx2 );
        else
            gl.glTexCoord1f( tx1 );

        gl.glVertex2f( vx1, vy2 );
        gl.glEnd();
    }

    public static void texQuad2D(    GL2 gl,
                                    float biasX,
                                    float biasY,
                                    float sizeX,
                                    float sizeY,
                                    float tBiasX,
                                    float tBiasY,
                                    float tSizeX,
                                    float tSizeY )
    {
        float vx1 = biasX;
        float vx2 = biasX + sizeX;
        float vy1 = biasY;
        float vy2 = biasY + sizeY;

        float tx1 = tBiasX;
        float tx2 = tBiasX + tSizeX;
        float ty1 = tBiasY;
        float ty2 = tBiasY + tSizeY;

        gl.glBegin( GL_QUADS );
        gl.glNormal3f( 0.0f, 0.0f, 1.0f );
        gl.glTexCoord2f( tx1, ty1 );
        gl.glVertex2f( vx1, vy1 );
        gl.glTexCoord2f( tx2, ty1 );
        gl.glVertex2f( vx2, vy1 );
        gl.glTexCoord2f( tx2, ty2 );
        gl.glVertex2f( vx2, vy2 );
        gl.glTexCoord2f( tx1, ty2 );
        gl.glVertex2f( vx1, vy2 );
        gl.glEnd();
    }

    public static void texQuad2DCentered(    GL2 gl,
                                            float xSize,
                                            float ySize )
    {
        gl.glBegin( GL_QUADS );
        gl.glTexCoord2f( 0.0f, 0.0f );
        gl.glVertex3f( -xSize / 2.0f, -ySize / 2.0f, 0.0f );
        gl.glTexCoord2f( 1.0f, 0.0f );
        gl.glVertex3f( +xSize / 2.0f, -ySize / 2.0f, 0.0f );
        gl.glTexCoord2f( 1.0f, 1.0f );
        gl.glVertex3f( +xSize / 2.0f, +ySize / 2.0f, 0.0f );
        gl.glTexCoord2f( 0.0f, 1.0f );
        gl.glVertex3f( -xSize / 2.0f, +ySize / 2.0f, 0.0f );
        gl.glEnd();
    }

    public static void texQuad3DCentered(    GL2 gl,
                                            float sizeX,
                                            float sizeY,
                                            float tz )
    {
        texQuad3DCentered( gl, -sizeX / 2, -sizeY / 2, sizeX, sizeY, 1, 1, tz );
    }

    public static void texQuad3DCentered(    GL2 gl,
                                            float xBias,
                                            float yBias,
                                            float sizeX,
                                            float sizeY,
                                            float tSizeX,
                                            float tSizeY,
                                            float tz )
    {
        float vx1 = xBias;
        float vx2 = xBias + sizeX;
        float vy1 = yBias;
        float vy2 = yBias + sizeY;

        float tx1 = 0.5f - tSizeX / 2.0f;
        float tx2 = 0.5f + tSizeX / 2.0f;
        float ty1 = 0.5f - tSizeY / 2.0f;
        float ty2 = 0.5f + tSizeY / 2.0f;

        gl.glBegin( GL_QUADS );
        gl.glNormal3f( 0.0f, 0.0f, 1.0f );
        gl.glTexCoord3f( tx1, ty1, tz );
        gl.glVertex2f( vx1, vy1 );
        gl.glTexCoord3f( tx2, ty1, tz );
        gl.glVertex2f( vx2, vy1 );
        gl.glTexCoord3f( tx2, ty2, tz );
        gl.glVertex2f( vx2, vy2 );
        gl.glTexCoord3f( tx1, ty2, tz );
        gl.glVertex2f( vx1, vy2 );
        gl.glEnd();
    }

}