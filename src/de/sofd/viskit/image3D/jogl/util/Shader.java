package de.sofd.viskit.image3D.jogl.util;

import java.io.*;
import javax.media.opengl.*;

import org.apache.log4j.*;

public abstract class Shader
{
    protected static final Logger logger = Logger.getLogger(Shader.class);
    
    protected GL2 gl;
    protected String shaderName;
    
    public Shader( GL2 gl,
                   String shaderName ) throws Exception
    {
        this.gl = gl;
        this.shaderName = shaderName;
    }
    
    public abstract void bind();
    
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
    
    
    
}