package de.sofd.viskit.glutil.lwjgl;

import de.sofd.viskit.glutil.Shader;
import de.sofd.viskit.glutil.ShaderFactory;

public class LWJGLShaderFactory extends ShaderFactory {

    @Override
    public Shader getShader(String shaderName) throws Exception {
        return new LWJGLShader(shaderName);
    }

}
