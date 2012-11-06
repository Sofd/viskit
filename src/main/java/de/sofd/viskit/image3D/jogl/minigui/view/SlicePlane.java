package de.sofd.viskit.image3D.jogl.minigui.view;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2GL3.*;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.*;

import java.io.*;

import javax.media.opengl.*;

import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.jogl.util.*;
import de.sofd.viskit.image3D.model.*;
import de.sofd.viskit.image3D.util.ShaderManager;

public abstract class SlicePlane extends Component
{

    protected ImageAxis axis;

    protected CutterPlane cutter;
    
    protected Reticle reticle;

    protected Component texBounds;

    protected ImagePlaneType type;

    protected VolumeObject volumeObject;
    
    protected ShaderManager shaderManager;
    
    public SlicePlane( int x, int y, int width, int height, ImageAxis axis, ImagePlaneType type,
            VolumeObject volumeObject, CutterPlane cutter ) throws IOException
    {
        super( x, y, width, height );

        setType( type );
        setVolumeObject( volumeObject );
        setAxis( axis );
        
        setPreferredAspectRatio( 1.0f );
        setCutter( cutter );
        
        texBounds = new Component( x + ( width - getTexWidth() ) / 2, y + ( height - getTexHeight() ) / 2,
                getTexWidth(), getTexHeight() );

        reticle = new Reticle( x, y, width, height, x + width / 2, y + height / 2, texBounds );
        reticle.setColor( 1.0f, 1.0f, 1.0f, 1.0f );
        
        cutter.resize( texBounds.getX(), texBounds.getY(), texBounds.getWidth(), texBounds.getHeight() );
        
        this.shaderManager = ShaderManager.getInstance();
    }

    public ImageAxis getAxis()
    {
        return axis;
    }

    public abstract double getCurrentSlice();
    
    public CutterPlane getCutter()
    {
        return cutter;
    }

    public abstract int getHorizontalMaxSlices();

    public abstract int getMaxSlices();

    public Reticle getReticle()
    {
        return reticle;
    }

    public abstract double getSliceHorizontalFromCursor();

    public abstract double getSliceHorizontalFromReticle();

    public abstract double getSliceVerticalFromCursor();

    public abstract double getSliceVerticalFromReticle();

    public Component getTexBounds()
    {
        return texBounds;
    }

    protected abstract int getTexHeight();

    protected abstract int getTexWidth();

    public ImagePlaneType getType()
    {
        return type;
    }

    public abstract int getVerticalMaxSlices();

    public VolumeObject getVolumeObject()
    {
        return volumeObject;
    }

    @Override
    public void glCreate() throws Exception
    {
        reticle.glCreate();
        cutter.glCreate();
    }

    @Override
    public void resize( int x,
                        int y,
                        int width,
                        int height )
    {
        super.resize( x, y, width, height );
        
        texBounds.resize( x + ( width - getTexWidth() ) / 2, y + ( height - getTexHeight() ) / 2, getTexWidth(),
                getTexHeight() );
        
        reticle.resize( x, y, width, height );
        
        cutter.resize( texBounds.getX(), texBounds.getY(), texBounds.getWidth(), texBounds.getHeight() );
        
        updateReticle();
        
    }
    
    protected void setAxis( ImageAxis axis )
    {
        this.axis = axis;
    }

    public abstract void setCurrentSlice( double currentSlice );

    public void setCutter( CutterPlane cutter )
    {
        this.cutter = cutter;
    }

    protected void setTexBounds( Component texBounds )
    {
        this.texBounds = texBounds;
    }

    public void setType( ImagePlaneType type )
    {
        this.type = type;
    }

    public void setVolumeObject( VolumeObject volumeObject )
    {
        this.volumeObject = volumeObject;
    }

    public void show( GL2 gl )
    {
        showTexPlane( gl );
        reticle.show( gl );
        cutter.show( gl );
    }

    protected void showTexPlane( GL2 gl )
    {
        gl.glMatrixMode( GL_TEXTURE );
        gl.glPushMatrix();

        transformTex( gl );

        float tz = (float)getCurrentSlice() * 1.0f / ( getMaxSlices() - 1 );

        gl.glMatrixMode( GL_MODELVIEW );
        gl.glLoadIdentity();
        
        shaderManager.bind("sliceView");

        gl.glActiveTexture(GL_TEXTURE2);
        gl.glBindTexture( GL_TEXTURE_3D, volumeObject.getTexId() );
        shaderManager.get("sliceView").bindUniform("volTex", 2);
        
        gl.glActiveTexture(GL_TEXTURE1);
        volumeObject.getWindowing().bindTexture( gl );
        shaderManager.get("sliceView").bindUniform("winTex", 1);
        
        gl.glActiveTexture( GL_TEXTURE3 );
        volumeObject.getTransferFunction().bindTexture( gl );
        shaderManager.get( "sliceView" ).bindUniform( "transferTex",3 );
        
        gl.glEnable( GL_BLEND );
        gl.glBlendFunc( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA );

        gl.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
        GLUtil.texQuad3DCentered( gl, texBounds.getX(), texBounds.getY(), texBounds.getWidth(), texBounds.getHeight(),
                1, 1, tz );
        
        shaderManager.unbind("sliceView");
        
        gl.glActiveTexture(GL_TEXTURE0);
        
        gl.glDisable( GL_BLEND );
                
        gl.glMatrixMode( GL_TEXTURE );
        gl.glPopMatrix();

        gl.glMatrixMode( GL_MODELVIEW );
        gl.glLoadIdentity();

    }

    protected abstract void transformTex( GL2 gl );

    public abstract void updateReticle();

    public abstract void updateSliceCursor();

    

}