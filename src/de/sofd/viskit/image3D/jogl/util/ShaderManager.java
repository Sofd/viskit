package de.sofd.viskit.image3D.jogl.util;

import java.util.*;

import javax.media.opengl.*;

public class ShaderManager
{
    protected static Map<String, Shader> shMap = new HashMap<String, Shader>();
    
    protected static final String ARB = "_ARB";
    
    protected static String shaderDir = ".";
    
    public static void init(String shaderDir)
    {
        ShaderManager.shaderDir = shaderDir;
    }
                
    public static void bind( String shName )
    {
        get( shName ).bind();
    }
    
    public static void bindARB( String shName )
    {
        getARB( shName ).bind();
    }
    
    public static GLShader get( String shName )
    {
        return (GLShader)shMap.get( shName );
    }
    
    public static ARBShader getARB( String shName )
    {
        return (ARBShader)shMap.get( shName + ARB );
    }
    
    public static void read( GL2 gl, String fname ) throws Exception
    {
        Shader shader = new GLShader( gl, shaderDir + "/" + fname + "/" + fname );
        shMap.put(fname, shader);
    }
    
    public static void readARB( GL2 gl, String fname ) throws Exception
    {
        Shader shader = new ARBShader( gl, shaderDir + "/" + fname + "/" + fname );
        shMap.put(fname + ARB, shader);
    }
    
    public static void unbind( String shName )
    {
        get( shName ).unbind();
    }
    
    public static void unbindARB( String shName )
    {
        getARB( shName ).unbind();
    }
}