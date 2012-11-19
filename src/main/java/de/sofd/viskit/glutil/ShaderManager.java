package de.sofd.viskit.glutil;

import java.util.*;

import javax.media.opengl.*;

public class ShaderManager
{
    protected Map<String, Shader> shMap = new HashMap<String, Shader>();
    
    protected final String ARB = "_ARB";
    
    protected String shaderDir = ".";
    
    protected static ShaderManager shaderManager = new ShaderManager();
    
    protected static ShaderFactory factory;
    
    private ShaderManager() {
    }

    /**
     * Initializes the shader manager and sets a Shader factory for shader
     * object creation. This method has to be called before usage of the
     * shader manager.
     * 
     * @param factory
     *            the factory that should be used for shader object creation
     */
    public static void initializeManager(ShaderFactory factory) {
        ShaderManager.factory = factory;
    }
    
    public static ShaderManager getInstance() {
        return shaderManager;
    }
    
    public void init(String shaderDir)
    {
        this.shaderDir = shaderDir;
    }
                
    public void bind( String shName )
    {
        get( shName ).bind();
    }
    
    public Shader get( String shName )
    {
        return (Shader)shMap.get( shName );
    }
    
    public void read( String fname ) throws Exception
    {        
        if(factory == null) {
            throw new IllegalStateException("No Shader factory is set for the shader manager! Ensure the shader manager is initalized");
        }
        Shader shader = factory.getShader(shaderDir + "/" + fname + "/" + fname );
        shMap.put(fname, shader);
    }
    
    public void unbind( String shName )
    {
        get( shName ).unbind();
    }
    
    public void cleanUp( ) {
        for ( Shader shader : shMap.values() )
            shader.cleanUp( );
    }
    
}