package de.sofd.viskit.image3D.util;

/**
 * Abstract factory class to abstract the creation of shader objects. The
 * factory class is used in the shader manager {@link ShaderManager} to create
 * shader objects independent from the Open GL like JOGL or LWJGL.
 * 
 * @author honglinh
 * 
 */
public abstract class ShaderFactory {
    
    public abstract Shader getShader(String shaderName) throws Exception;

}