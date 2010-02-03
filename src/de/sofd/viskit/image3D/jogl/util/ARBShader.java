package de.sofd.viskit.image3D.jogl.util;

import static javax.media.opengl.GL2.*;

import javax.media.opengl.*;

import org.apache.log4j.*;

public class ARBShader extends Shader
{

    static final Logger logger = Logger.getLogger(ARBShader.class);
    
    protected int ids[] = new int[2];
               
    public ARBShader(GL2 gl, String shaderName) throws Exception
    {
        super(gl, shaderName);
        
        setupShader();
    }

    @Override
    public void bind()
    {
        gl.glEnable( GL_VERTEX_PROGRAM_ARB );
        gl.glEnable( GL_FRAGMENT_PROGRAM_ARB );
        gl.glBindProgramARB( GL_VERTEX_PROGRAM_ARB, ids[0] );
        gl.glBindProgramARB( GL_FRAGMENT_PROGRAM_ARB, ids[1] );
    }
    
    @Override
    public void cleanUp( GL2 gl ) {
        gl.glDeleteProgramsARB(2, ids, 0);
    }

    @Override
    protected void setupShader() throws Exception
    {
        
        //error position in shader text
        int errorPos[] = new int[1];
            
        String vpString;
        String fpString;
            
        if ( ! this.gl.isExtensionAvailable("GL_ARB_vertex_program") )
        {
            throw new Exception( "GL_ARB_vertex_program not supported!\n" );
        } 
            
        if ( ! this.gl.isExtensionAvailable("GL_ARB_fragment_program") )
        {
            throw new Exception( "GL_ARB_fragment_program not supported!\n" );
        } 
            
        //create shader object
        gl.glGenProgramsARB( 2, ids, 0 );
        
        //set vertex shader
        gl.glBindProgramARB( GL_VERTEX_PROGRAM_ARB, ids[0] );
        
        //read in vertex shader program
        vpString = readShader( shaderName + ".vp" );
        
        //load vertex shader
        gl.glProgramStringARB( GL_VERTEX_PROGRAM_ARB, GL_PROGRAM_FORMAT_ASCII_ARB,
                            vpString.length(), vpString );
        
        //error detection for vertex shader
        gl.glGetIntegerv( GL_PROGRAM_ERROR_POSITION_ARB, errorPos, 0 );
        if ( errorPos[0] != -1 )
        {
            logger.error("Error in vertex shader at position " + errorPos[0] );
            throw new Exception( gl.glGetString( GL_PROGRAM_ERROR_STRING_ARB ) );
        } 
        
        //set fragment shader
        gl.glBindProgramARB( GL_FRAGMENT_PROGRAM_ARB, ids[1] );
        
        fpString = readShader( shaderName + ".fp" );
        
        //load fragment shader
        gl.glProgramStringARB( GL_FRAGMENT_PROGRAM_ARB, GL_PROGRAM_FORMAT_ASCII_ARB,
                            fpString.length(), fpString );
        
        //error detection for fragment shader
        gl.glGetIntegerv( GL_PROGRAM_ERROR_POSITION_ARB, errorPos, 0 );
        if ( errorPos[0] != -1 )
        {
            logger.error( "Error in fragment shader at position " +  errorPos[0] );
            throw new Exception( gl.glGetString( GL_PROGRAM_ERROR_STRING_ARB ) );
        }
        
    } 

    @Override
    public void unbind()
    {
        gl.glDisable( GL_VERTEX_PROGRAM_ARB );
        gl.glDisable( GL_FRAGMENT_PROGRAM_ARB );
        gl.glBindProgramARB( GL_VERTEX_PROGRAM_ARB, 0 );
        gl.glBindProgramARB( GL_FRAGMENT_PROGRAM_ARB, 0 );
    }
}