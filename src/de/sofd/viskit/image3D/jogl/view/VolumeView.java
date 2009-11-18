package de.sofd.viskit.image3D.jogl.view;

import static javax.media.opengl.GL2.*;

import java.nio.*;

import javax.media.opengl.*;
import javax.media.opengl.awt.*;

import vtk.*;

import com.sun.opengl.util.*;

@SuppressWarnings("serial")
public class VolumeView extends GLCanvas implements GLEventListener
{
    protected final int MAX_PLANES = 1000;
    
    protected static Animator animator;
    
    protected vtkImageData imageData;
    
    protected int theTex;
    protected int thePlanes;
    
    protected static GLCapabilities caps;
    
    protected float alpha = 0.0f;
    
    static {
        caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
        caps.setAlphaBits(8); 
    }
    
    public VolumeView(vtkImageData imageData)
    {
        super(caps);
        this.imageData = imageData;
        addGLEventListener(this);
    }
    
    private void init3DTexture(GL2 gl) {
        int[] texId = new int[1];
        
        int[] dim = imageData.GetDimensions();
        double[] range = imageData.GetScalarRange();
        double rangeDist = range[1] - range[0];
        rangeDist = ( rangeDist > 0 ? rangeDist : 1 );
        
        System.out.println("scalar range [" + range[0] + ", " + range[1] + "]");
        
        FloatBuffer dataBuf = BufferUtil.newFloatBuffer(dim[0]*dim[1]*dim[2]);
        for ( int z = 0; z < dim[2]; ++z)
            for ( int y = 0; y < dim[1]; ++y)
                for ( int x = 0; x < dim[0]; ++x)
                    dataBuf.put((float)(imageData.GetScalarComponentAsFloat(x, y, z, 0)/rangeDist));
        
        dataBuf.rewind();
        
        gl.glPixelStorei(GL_UNPACK_ALIGNMENT, 1); 
        
        gl.glGenTextures(1, texId, 0);
        theTex = texId[0];
        gl.glBindTexture(GL_TEXTURE_3D, theTex);
        gl.glTexImage3D(GL_TEXTURE_3D, 0, GL_ALPHA, dim[0], dim[1], dim[2], 0, GL_ALPHA, GL_FLOAT, dataBuf);
        
        gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER );
        gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER );
        gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_BORDER );
              
        gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, 
                         GL_LINEAR );
        gl.glTexParameteri( GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, 
                         GL_LINEAR );  
        
       
         

    }
    
    private void initPlanes(GL2 gl) {
        double[] spacing = imageData.GetSpacing();
        int[] dim = imageData.GetDimensions();
        double width = dim[0] * spacing[0];
        double height = dim[1] * spacing[1];
        double depth = dim[2] * spacing[2];
        
        double maxDim = 0;
        if ( width > maxDim ) maxDim = width;
        if ( height > maxDim ) maxDim = height;
        if ( depth > maxDim ) maxDim = depth;
        
        System.out.println("width : " + width);
        System.out.println("height : " + height);
        System.out.println("depth : " + depth);
         
        thePlanes = gl.glGenLists(1);
        gl.glNewList(thePlanes, GL_COMPILE);
        
        gl.glBegin(GL_QUADS);
            for (int i=0; i<MAX_PLANES; ++i)
            {
                double z = (-depth/maxDim + (depth*2.0f/maxDim)*i/(MAX_PLANES-1))*2.0f;
                double u = (1.0f - i*1.0f/(MAX_PLANES - 1))*2.0f-0.5f;
                System.out.println("z : "+ z + ", u : " + u);
                gl.glColor4f(1.0f, 1.0f, 1.0f, 6.0f/MAX_PLANES);
                //gl.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
                gl.glTexCoord4d(-0.5f, -0.5f, u, 1.0f);
                gl.glVertex3d(-width/maxDim*2.0f, -height/maxDim*2.0f, z);
                gl.glTexCoord4d(1.5f, -0.5f, u, 1.0f);
                gl.glVertex3d( width/maxDim*2.0f, -height/maxDim*2.0f, z);
                gl.glTexCoord4d(1.5f, 1.5f, u, 1.0f);
                gl.glVertex3d( width/maxDim*2.0f, height/maxDim*2.0f, z);
                gl.glTexCoord4d(-0.5f, 1.5f, u, 1.0f);
                gl.glVertex3d(-width/maxDim*2.0f, height/maxDim*2.0f, z);
            }
            
        gl.glEnd();
        
        gl.glEndList();
        
    }
    
    protected void idle()
    {
        alpha += 1.0f;
    }
    
    @Override
    public void display(GLAutoDrawable drawable) {
        idle();
        
        GL2 gl = drawable.getGL().getGL2();
        
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); 
        
        gl.glMatrixMode(GL_TEXTURE);
        gl.glLoadIdentity();
        gl.glTranslatef( 0.5f,  0.5f,  0.5f);
        gl.glRotatef(alpha, 0.0f, 1.0f, 0.0f);
        gl.glTranslatef(-0.5f, -0.5f, -0.5f);
        
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
        
        //gl.glTranslatef(0.0f, 0.0f, 4.0f);
        
        gl.glBindTexture(GL_TEXTURE_3D, theTex);
        gl.glCallList(thePlanes);
        
        
        
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        System.out.println("dispose: "+drawable); 
        
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        drawable.getChosenGLCapabilities().setAlphaBits(8);
        
        System.err.println("INIT GL IS: " + gl.getClass().getName());

        System.err.println("Chosen GLCapabilities: " + drawable.getChosenGLCapabilities());

        gl.setSwapInterval(1); 
        
        gl.glShadeModel(GL_SMOOTH);
        
        
        gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        //gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        //gl.glBlendColor(1.0f, 1.0f, 1.0f, 1.0f/MAX_PLANES);
        
        gl.glEnable(GL_BLEND);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glEnable(GL_TEXTURE_3D);
        
        
        init3DTexture(gl);
        initPlanes(gl);
        
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();

        float h = (float)height / (float)width;
                
        gl.glMatrixMode(GL_PROJECTION);

        System.err.println("GL_VENDOR: " + gl.glGetString(GL_VENDOR));
        System.err.println("GL_RENDERER: " + gl.glGetString(GL_RENDERER));
        System.err.println("GL_VERSION: " + gl.glGetString(GL_VERSION));
        
        gl.glLoadIdentity();
        gl.glOrtho(-2.0f, 2.0f, -h*2.0f, h*2.0f, -2.0f, 2.0f);
        //gl.glFrustum(-1.0f, 1.0f, -h, h, 0.1f, 60.0f);
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
        
        
    }
    
    
}