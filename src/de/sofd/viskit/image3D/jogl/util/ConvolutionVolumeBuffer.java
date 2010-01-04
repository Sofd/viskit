package de.sofd.viskit.image3D.jogl.util;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2GL3.*;

import javax.media.opengl.*;

import de.sofd.util.*;

public class ConvolutionVolumeBuffer extends VolumeBuffer
{

    protected GLShader shader;

    protected int orgVolumeTex;

    public ConvolutionVolumeBuffer( IntDimension3D size, GLShader shader, int orgVolumeTex )
    {
        super( size );
        this.shader = shader;
        this.orgVolumeTex = orgVolumeTex;
        
        shader.addProgramUniform( "volTex" );
        shader.addProgramUniform( "xStep" );
        shader.addProgramUniform( "yStep" );
        shader.addProgramUniform( "zStep" );
    }

    @Override
    public void run( GL2 gl )
    {
        super.begin( gl );

        shader.bind();

        gl.glActiveTexture( GL_TEXTURE1 );
        gl.glBindTexture( GL_TEXTURE_3D, orgVolumeTex );
        
        shader.bindUniform( "volTex", 1 );

        shader.bindUniform( "xStep", 1.0f / getSize().getWidth() );
        shader.bindUniform( "yStep", 1.0f / getSize().getHeight() );
        shader.bindUniform( "zStep", 1.0f / getSize().getDepth() );
        
        for ( int z = 0; z < getSize().getDepth(); z++ )
        {
            attachTexture( gl, z );

            drawSlice( gl, ( z + 0.5f ) / getSize().getDepth() );
        }

        shader.unbind();

        super.end( gl );
        
        gl.glActiveTexture(GL_TEXTURE0);
    }

}
