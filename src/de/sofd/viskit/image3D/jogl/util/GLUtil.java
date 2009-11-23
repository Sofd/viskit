package de.sofd.viskit.image3D.jogl.util;

import static com.sun.opengl.util.gl2.GLUT.*;
import static javax.media.opengl.GL2.*;

import java.nio.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;

import org.apache.log4j.*;

import com.sun.opengl.util.*;
import com.sun.opengl.util.gl2.*;

public class GLUtil
{
    static final Logger logger = Logger.getLogger(GLUtil.class);
    
    public static void beginInfoScreen( GL2 gl, GLU glu, int winWidth, int winHeight )
    {
        beginInfoScreen(gl, glu, winWidth, winHeight, true, false, null);
    }
    
    public static void beginInfoScreen( GL2 gl, GLU glu, int winWidth, int winHeight, boolean upDown, boolean getP, double[] P )
    {
        //draw info text
        gl.glMatrixMode(GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluOrtho2D(0, winWidth, ( upDown ? 0 : winHeight ), ( upDown ? winHeight : 0 ));
            
            if ( getP )
                gl.glGetDoublev( GL_PROJECTION_MATRIX, P, 0 );
                            
            gl.glMatrixMode(GL_MODELVIEW);
            gl.glPushMatrix();
            gl.glLoadIdentity();
                        
                //glColor3f(1.0f, 1.0f, 0.0f);
    }

    public static void endInfoScreen( GL2 gl )
    {
        gl.glPopMatrix();
                
        gl.glMatrixMode(GL_PROJECTION);
                
        gl.glPopMatrix();
        gl.glMatrixMode(GL_MODELVIEW); 
    }
    
    public static void infoText( GL2 gl, GLUT glut, int posX, int posY, String message )
    {
        gl.glRasterPos2i( posX, posY );
        glut.glutBitmapString( BITMAP_8_BY_13, message );
    }

    public static int initScreenTex(GL2 gl, int width, int height)
    {
        FloatBuffer texBuf = BufferUtil.newFloatBuffer(width*height*3);
            
        for ( int i = 0; i < width; ++i )
            for ( int j = 0; j < height; ++j )
                for ( int k = 0; k < 3; ++k )
                    texBuf.put(0);
            
        texBuf.rewind();
                    
        gl.glPixelStorei(GL_UNPACK_ALIGNMENT, 1); 
        
        int theTex[] = new int[1];
        gl.glGenTextures( 1, theTex, 0 );
            
        gl.glBindTexture( GL_TEXTURE_2D, theTex[0] );    
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_FLOAT, texBuf);
            
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP );
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP );
           
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, 
                             GL_NEAREST );
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, 
                             GL_NEAREST );  
        
        return theTex[0];
        

    }
    
    public static void logi( GL2 gl, String paramName, int param )
    {
        int value[] = new int[1];
        
        gl.glGetIntegerv( param, value, 0 );
                
        logger.info( paramName + " : " + value[0] );
    }
    
    public static void texQuad2D( GL2 gl, float xSize, float ySize )
    {
        gl.glBegin(GL_QUADS);
            gl.glTexCoord2f( 0.0f, 0.0f );
            gl.glVertex3f( -xSize/2.0f, -ySize/2.0f, 0.0f );
            gl.glTexCoord2f( 1.0f, 0.0f );
            gl.glVertex3f( +xSize/2.0f, -ySize/2.0f, 0.0f );
            gl.glTexCoord2f( 1.0f, 1.0f );
            gl.glVertex3f( +xSize/2.0f, +ySize/2.0f, 0.0f );
            gl.glTexCoord2f( 0.0f, 1.0f );
            gl.glVertex3f( -xSize/2.0f, +ySize/2.0f, 0.0f );
        gl.glEnd();
    }
    
    public static void texQuad3D( GL2 gl, float xSize, float ySize, float tz )
    {
        gl.glBegin(GL_QUADS);
            gl.glTexCoord3f( 0.0f, 1.0f, tz );
            gl.glVertex3f( -xSize/2.0f, -ySize/2.0f, 0.0f );
            gl.glTexCoord3f( 1.0f, 1.0f, tz );
            gl.glVertex3f( +xSize/2.0f, -ySize/2.0f, 0.0f );
            gl.glTexCoord3f( 1.0f, 0.0f, tz );
            gl.glVertex3f( +xSize/2.0f, +ySize/2.0f, 0.0f );
            gl.glTexCoord3f( 0.0f, 0.0f, tz );
            gl.glVertex3f( -xSize/2.0f, +ySize/2.0f, 0.0f );
        gl.glEnd();
    }


}