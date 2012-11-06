package de.sofd.viskit.image3D.lwjgl.util;

import de.sofd.viskit.image3D.util.Shader;
import de.sofd.viskit.image3D.util.ShaderFactory;

public class LWJGLShaderFactory extends ShaderFactory {

    @Override
    public Shader getShader(String shaderName) throws Exception {
        return new LWJGLShader(shaderName);
    }

}
