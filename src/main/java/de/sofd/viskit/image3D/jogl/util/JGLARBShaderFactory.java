package de.sofd.viskit.image3D.jogl.util;

import javax.media.opengl.GL2;

import de.sofd.viskit.image3D.util.Shader;

public class JGLARBShaderFactory extends JGLShaderFactory {

    public JGLARBShaderFactory(GL2 gl) {
        super(gl);
    }

    @Override
    public Shader getShader(String shaderName) throws Exception {
        return new JGLARBShader(gl, shaderName);
    }
}
