package de.sofd.viskit.image3D.jogl.util;

import javax.media.opengl.*;

import de.sofd.util.*;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

public class FrameBuffer
{
    protected Size size;

    protected int theFBO;
    protected int theTex;
    
    protected int internalFormat;
    protected int format;

    public FrameBuffer( Size size )
    {
        this.size = size;
    }

    protected void attachTexture( GL2 gl, int layer )
    {
        gl.glFramebufferTexture2D( GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, theTex, 0 );
    }

    public void begin( GL2 gl )
    {
        bind( gl );

        gl.glViewport( 0, 0, size.getWidth(), size.getHeight() );
        
        gl.glMatrixMode( GL_PROJECTION );
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glOrtho( 0, size.getWidth(), 0, size.getHeight(), -1.0, 1.0 );
        
        gl.glMatrixMode( GL_MODELVIEW );
        gl.glPushMatrix();
        gl.glLoadIdentity();
    }

    public void bind( GL2 gl )
    {
        gl.glBindFramebuffer( GL_DRAW_FRAMEBUFFER, theFBO );
    }

    protected void checkFBO( GL2 gl ) throws Exception
    {
        int error = gl.glCheckFramebufferStatus( GL_DRAW_FRAMEBUFFER );

        switch ( error )
        {
            case GL_FRAMEBUFFER_COMPLETE:
                return;
            case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                throw new Exception( "Incomplete attachment" );
            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                throw new Exception( "Missing attachment" );
            case GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
                throw new Exception( "Incomplete dimensions" );
            case GL_FRAMEBUFFER_INCOMPLETE_FORMATS:
                throw new Exception( "Incomplete formats" );
            case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
                throw new Exception( "Incomplete draw buffer" );
            case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
                throw new Exception( "Incomplete read buffer" );
            case GL_FRAMEBUFFER_UNSUPPORTED:
                throw new Exception( "Framebufferobjects unsupported" );
            default:
                return;
        }
    }

    public void cleanUp( GL2 gl )
    {
        int[] fbo = new int[]
        {
            theFBO
        };

        gl.glDeleteFramebuffers( 1, fbo, 0 );
        
        theFBO = -1;
    }

    public void createFBO( GL2 gl ) throws Exception
    {
        int[] fbo = new int[ 1 ];

        gl.glGenFramebuffers( 1, fbo, 0 );

        theFBO = fbo[ 0 ];

        bind( gl );
        attachTexture( gl, 0 );

        checkFBO( gl );
        
        unbind( gl );
    }

    public void createTexture( GL2 gl, int internalFormat, int format )
    {
        createTexture(gl, internalFormat, format, false);
    }
    
    public void createTexture( GL2 gl, int internalFormat, int format, boolean mipmap )
    {
        this.internalFormat = internalFormat;
        this.format = format;
        
        gl.glEnable( GL_TEXTURE_2D );
        
        int[] tex = new int[ 1 ];
        gl.glGenTextures( 1, tex, 0 );

        this.theTex = tex[ 0 ];

        gl.glBindTexture( GL_TEXTURE_2D, this.theTex );
        
        if ( mipmap ) {
            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR );
        } else {
            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
        }
        
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );

        int mode = GL_CLAMP_TO_BORDER;

        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, mode );
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, mode );
        
        gl.glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, mipmap ? 1 : 0);
        gl.glTexImage2D( GL_TEXTURE_2D, 0, internalFormat, size.getWidth(), size.getHeight(), 0, format, GL_FLOAT, null );
        
        gl.glDisable( GL_TEXTURE_2D );
    }
    
    protected void drawSlice( GL2 gl )
    {
        GLUtil.texQuad2D( gl, size.getWidth(), size.getHeight() );
    }

    public void end( GL2 gl )
    {
        gl.glMatrixMode( GL_PROJECTION );
        gl.glPopMatrix();

        gl.glMatrixMode( GL_MODELVIEW );
        gl.glPopMatrix();

        unbind( gl );
    }

    public int getTex()
    {
        return theTex;
    }

    public void resize( GL2 gl, int internalFormat, int format, Size size ) throws Exception
    {
        gl.glEnable( GL_TEXTURE_2D );
        
        this.size = size;
        
        gl.glBindTexture( GL_TEXTURE_2D, this.theTex );
        gl.glTexImage2D( GL_TEXTURE_2D, 0, internalFormat, size.getWidth(), size.getHeight(), 0, format, GL_FLOAT, null );
        
        bind( gl );
        attachTexture( gl, 0 );

        checkFBO( gl );
        
        unbind( gl );
        
        gl.glDisable( GL_TEXTURE_2D );
    }

    public void run( GL2 gl )
    {
        begin( gl );
        
        attachTexture( gl, 0 );
        drawSlice( gl );
        
        end( gl );
    }

    protected void unbind( GL2 gl )
    {
        gl.glBindFramebuffer( GL_DRAW_FRAMEBUFFER, 0 );
        
    }
}