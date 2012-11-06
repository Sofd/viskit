package de.sofd.viskit.image3D.jogl.util;

import javax.media.opengl.GL2;

import de.sofd.viskit.image3D.util.Shader;
import de.sofd.viskit.image3D.util.ShaderFactory;

public class JGLShaderFactory extends ShaderFactory {

    protected GL2 gl;

    
    public JGLShaderFactory(GL2 gl) {
        this.gl = gl;
    }
    
    @Override
    public Shader getShader(String shaderName) throws Exception {
        return new JGLShader(gl, shaderName);
    }

}
