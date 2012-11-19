package de.sofd.viskit.glutil.jogl;

import javax.media.opengl.GL2;

import de.sofd.viskit.glutil.Shader;
import de.sofd.viskit.glutil.ShaderFactory;

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
