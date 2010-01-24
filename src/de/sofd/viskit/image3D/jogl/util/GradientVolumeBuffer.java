package de.sofd.viskit.image3D.jogl.util;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2GL3.*;

import javax.media.opengl.*;

import de.sofd.util.*;
import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.model.*;

public class GradientVolumeBuffer extends VolumeBuffer
{

    protected GLShader shader;

    protected VolumeObject volumeObject;
    
    public GradientVolumeBuffer( IntDimension3D size, GLShader shader, VolumeObject volumeObject )
    {
        super( size );
        this.shader = shader;
        this.volumeObject = volumeObject;
        
        shader.addProgramUniform( "volTex" );
        shader.addProgramUniform( "winTex" );
        shader.addProgramUniform( "xStep" );
        shader.addProgramUniform( "yStep" );
        shader.addProgramUniform( "zStep" );
        shader.addProgramUniform( "xMin" );
        shader.addProgramUniform( "xMax" );
        shader.addProgramUniform( "yMin" );
        shader.addProgramUniform( "yMax" );
        shader.addProgramUniform( "zMin" );
        shader.addProgramUniform( "zMax" );
        
        
        
    }

    public void run( GL2 gl )
    {
        super.begin( gl );

        shader.bind();

        gl.glActiveTexture( GL_TEXTURE1 );
        gl.glBindTexture( GL_TEXTURE_3D, volumeObject.getTexId2() );
        
        shader.bindUniform( "volTex", 1 );
        
        gl.glActiveTexture( GL_TEXTURE2 );
        volumeObject.bindWindowingTexture(gl);
        
        shader.bindUniform( "winTex", 2 );
        
        float gradientLength = volumeObject.getVolumeConfig().getLightingConfig().getGradientLength();
        shader.bindUniform( "xStep", gradientLength / getSize().getWidth() );
        shader.bindUniform( "yStep", gradientLength / getSize().getHeight() );
        shader.bindUniform( "zStep", gradientLength / getSize().getDepth() );
        
        VolumeConstraint constraint = volumeObject.getConstraint();
        shader.bindUniform( "xMin", constraint.getX().getMin());
        shader.bindUniform( "xMax", constraint.getX().getMax());
        shader.bindUniform( "yMin", constraint.getY().getMin());
        shader.bindUniform( "yMax", constraint.getY().getMax());
        shader.bindUniform( "zMin", constraint.getZ().getMin());
        shader.bindUniform( "zMax", constraint.getZ().getMax());
        
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
