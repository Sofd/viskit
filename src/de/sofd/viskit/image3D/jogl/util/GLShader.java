package de.sofd.viskit.image3D.jogl.util;

import static javax.media.opengl.GL2.*;

import java.nio.*;
import java.util.*;

import javax.media.opengl.*;

import com.sun.opengl.util.*;

public class GLShader extends Shader
{

    protected boolean useGeomShader;
    protected int inputGeom;
    protected int outputGeom;
    protected int vertOut;
    
    protected int shader, program;
    
    protected IntBuffer compiled, linked;
    
    protected HashMap<String, Integer> uniformMap = new HashMap<String, Integer>();

    public GLShader(GL2 gl, String fname) throws Exception
    {
        this(gl, fname, false, -1, -1, -1 );
    }

    public GLShader(GL2 gl, String shaderName, boolean useGeomShader,
            int inputGeom, int outputGeom, int vertOut) throws Exception
    {
        super(gl, shaderName);

        this.useGeomShader = useGeomShader;
        this.inputGeom = inputGeom;
        this.outputGeom = outputGeom;
        this.vertOut = vertOut;
        
        this.shader = -1;
        this.program = -1;
        this.compiled = BufferUtil.newIntBuffer(1);
        
        this.linked = BufferUtil.newIntBuffer(1);
        
        setupShader();
        
    }
    
    public void addProgramUniform(String name)
    {
        int loc = gl.glGetUniformLocation(getProgram(), name);
        uniformMap.put(name, loc);
    }
    
    @Override
    public void bind()
    {
        gl.glUseProgram(this.program);
    }
    
    public void bindUniform(String name, boolean value)
    {
        gl.glUniform1i(uniformMap.get(name), ( value ? 1 : 0 ));
    }
    
    public void bindUniform(String name, float value)
    {
        gl.glUniform1f(uniformMap.get(name), value);
    }
    
    public void bindUniform(String name, float[] value) {
        switch ( value.length )
        {
            case 1 :
                gl.glUniform1f(uniformMap.get(name), value[0]);
                break;
            case 2 :
                gl.glUniform2f(uniformMap.get(name), value[0], value[1]);
                break;
            case 3 :
                gl.glUniform3f(uniformMap.get(name), value[0], value[1], value[2]);
                break;
            case 4 :
                gl.glUniform4f(uniformMap.get(name), value[0], value[1], value[2], value[3]);
                break;
        }
        
    }
    
    public void bindUniform(String name, int value)
    {
        gl.glUniform1i(uniformMap.get(name), value);
    }

    public int getProgram()
    {
        return program;
    }
    
    public void setProgram(int program)
    {
        this.program = program;
    }
    
    @Override
    protected void setupShader() throws Exception
    {
        
        String[] shString = new String[1];
        
        this.program = gl.glCreateProgram();
        
        String shExt[] = { "vert", "frag", "geom" };
        int shType[] = { GL_VERTEX_SHADER, GL_FRAGMENT_SHADER, GL_GEOMETRY_SHADER };
        
        for ( int i = 0; i < ( useGeomShader ? 3 : 2 ); ++i )
        {
                        
            //read in shader program
            String fname = shaderName + "." + shExt[i];
            shString[0] = readShader( fname );
            shader = gl.glCreateShader( shType[i] );
            gl.glShaderSource( shader, 1, shString, null );
            gl.glCompileShader( shader );
            gl.glGetShaderiv( shader, GL_COMPILE_STATUS, compiled );
            compiled.rewind();
            if ( compiled.get(0) != GL_TRUE )
            {
                String shlog = shaderLog( shader );
                throw new Exception(shExt[i] + " " + shlog);
            }
            
            gl.glAttachShader( program, shader );
            
            if ( i == 2 )
            {
                gl.glProgramParameteri( program, GL_GEOMETRY_INPUT_TYPE, inputGeom );
                gl.glProgramParameteri( program, GL_GEOMETRY_OUTPUT_TYPE, outputGeom );
                gl.glProgramParameteri( program, GL_GEOMETRY_VERTICES_OUT, vertOut );
                
                GLUtil.logi( gl, "GL_MAX_GEOMETRY_OUTPUT_VERTICES", GL_MAX_GEOMETRY_OUTPUT_VERTICES );
                GLUtil.logi( gl, "GL_MAX_GEOMETRY_TOTAL_OUTPUT_COMPONENTS", GL_MAX_GEOMETRY_TOTAL_OUTPUT_COMPONENTS );
            }
        }    
               
        gl.glLinkProgram( program );
        
        gl.glGetProgramiv( program, GL_LINK_STATUS, linked );
        linked.rewind();
        
        if ( linked.get(0) != GL_TRUE )
        {
            String shlog = shaderPrLog( program );
            throw new Exception("linking : " +shlog);
        }
    }
    
    protected String shaderLog( int shader )
    {
        IntBuffer length = BufferUtil.newIntBuffer(1);
        ByteBuffer log = BufferUtil.newByteBuffer(4096);
        
        gl.glGetShaderiv( shader, GL_INFO_LOG_LENGTH, length );
        //length.rewind();
        gl.glGetShaderInfoLog( shader, length.get(0), length, log );
        log.rewind();
        
        byte[] bytearr = new byte[log.remaining()];
        log.get(bytearr);
        String s = new String(bytearr);
       
        return s;
    }
    
    protected String shaderPrLog( int shader )
    {
        IntBuffer length = BufferUtil.newIntBuffer(1);
        ByteBuffer log = BufferUtil.newByteBuffer(4096);
        
        gl.glGetProgramiv( shader, GL_INFO_LOG_LENGTH, length );
        //length.rewind();
        gl.glGetProgramInfoLog( shader, length.get(0), length, log );
        log.rewind();
        
        byte[] bytearr = new byte[log.remaining()];
        log.get(bytearr);
        String s = new String(bytearr);
        
        return s;
    }

    @Override
    public void unbind() {
        gl.glUseProgram(0);

    }

    @Override
    public void cleanUp( GL2 gl ) {
        gl.glDeleteShader(shader);
        gl.glDeleteProgram(program);
    }
    
}