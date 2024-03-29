package de.sofd.viskit.glutil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

public abstract class Shader
{
    protected static final Logger logger = Logger.getLogger(Shader.class);
    

    protected String shaderName;
    
    public Shader( String shaderName ) throws Exception
    {
        this.shaderName = shaderName;
    }
    
    public abstract void bind();
    
    public abstract void cleanUp( ); 
    
    protected String readShader( String fname ) throws IOException
    {
        logger.debug("shader file to read : " + fname);
        
        StringBuffer sbuf = new StringBuffer("");
        ClassLoader cl = getClass().getClassLoader();
        InputStream is = cl.getResourceAsStream(fname);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        
        while ( (line=br.readLine() ) != null ) {
            sbuf.append(line).append("\n");
        }
        
        return sbuf.toString();
    }
    
    protected abstract void setupShader() throws Exception;
    

    public abstract void unbind();
    
    public abstract void addProgramUniform(String name);
    
    public abstract void bindUniform(String name, boolean value);
    
    public abstract void bindUniform(String name, float value);
    
    public abstract void bindUniform(String name, float[] value);
    
    public abstract void bindUniform(String name, int value);
    
    
    public abstract int getProgram();
    
    public abstract void setProgram(int program);
    
}