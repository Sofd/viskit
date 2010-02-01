package de.sofd.viskit.image3D.jogl.util;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2GL3.*;

import javax.media.opengl.*;

import de.sofd.util.*;
import de.sofd.viskit.image3D.jogl.model.*;

public class ConvolutionVolumeBuffer extends VolumeBuffer
{

    protected GLShader shader;

    protected VolumeObject volumeObject;

    public ConvolutionVolumeBuffer( IntDimension3D size, GLShader shader, VolumeObject volumeObject )
    {
        super( size );
        this.shader = shader;
        this.volumeObject = volumeObject;
        
        shader.addProgramUniform( "volTex" );
        shader.addProgramUniform( "winTex" );
        shader.addProgramUniform( "xStep" );
        shader.addProgramUniform( "yStep" );
        shader.addProgramUniform( "zStep" );
    }
    
    @Override
    public void cleanUp(GL2 gl) {
        int[] fbo = new int[] { theFBO };

        gl.glDeleteFramebuffers(1, fbo, 0);

        theFBO = -1;

        int[] tex = new int[] { theTex };

        gl.glDeleteTextures(1, tex, 0);
        
        theTex = -1;
        
    }

    @Override
    public void run( GL2 gl )
    {
        super.begin( gl );

        shader.bind();

        gl.glActiveTexture( GL_TEXTURE1 );
        gl.glBindTexture( GL_TEXTURE_3D, volumeObject.getTexId() );
        
        shader.bindUniform( "volTex", 1 );
        
        gl.glActiveTexture( GL_TEXTURE2 );
        volumeObject.bindWindowingTexture(gl);

        shader.bindUniform( "winTex", 2 );
        
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
