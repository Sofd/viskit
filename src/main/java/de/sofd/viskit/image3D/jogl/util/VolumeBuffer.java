package de.sofd.viskit.image3D.jogl.util;

import javax.media.opengl.*;

import de.sofd.util.*;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

public class VolumeBuffer extends FrameBuffer
{
    public VolumeBuffer( IntDimension3D size )
    {
        super(size);
    }
    
    public IntDimension3D getSize()
    {
        return (IntDimension3D)size;
    }

    @Override
    public void createTexture( GL2 gl, int internalFormat, int format )
    {
        gl.glEnable( GL_TEXTURE_3D );
        
        int[] tex = new int[ 1 ];
        gl.glGenTextures( 1, tex, 0 );

        this.theTex = tex[ 0 ];

        gl.glBindTexture( GL_TEXTURE_3D, this.theTex );
        gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR );
        gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR );

        int mode = GL_CLAMP_TO_BORDER;

        gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, mode );
        gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, mode );
        gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, mode );
        gl.glTexImage3D( GL_TEXTURE_3D, 0, internalFormat, getSize().getWidth(), getSize().getHeight(), getSize().getDepth(), 0, format,
                GL_FLOAT, null );
        
        gl.glDisable( GL_TEXTURE_3D );
    }

    @Override
    protected void attachTexture( GL2 gl, int layer )
    {
        gl.glFramebufferTexture3D( GL_DRAW_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_3D, theTex, 0, layer );
    }

    protected void drawSlice( GL2 gl, float z )
    {
        GLUtil.texQuad3D( gl, size.getWidth(), size.getHeight(), z );
    }

    @Override
    public void run( GL2 gl )
    {
        begin( gl );

        for ( int z = 0; z < getSize().getDepth(); z++ )
        {
            attachTexture( gl, z );
            drawSlice( gl, ( z + 0.5f ) / getSize().getDepth() );
        }

        end( gl );
    }
}