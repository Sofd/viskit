package de.sofd.viskit.image3D.jogl.minigui.view;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2GL3.*;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.*;

import java.io.*;

import javax.media.opengl.*;

import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.jogl.util.*;
import de.sofd.viskit.image3D.model.*;

public abstract class SlicePlane extends Component
{

    protected Reticle reticle;

    protected VolumeObject volumeObject;

    protected ImagePlaneType type;
    protected ImageAxis axis;

    protected Component texBounds;

    public SlicePlane(    int x,
                        int y,
                        int width,
                        int height,
                        ImageAxis axis,
                        ImagePlaneType type,
                        VolumeObject volumeObject,
                        int texWidth,
                        int texHeight ) throws IOException
    {
        super( x, y, width, height );

        texBounds = new Component( x + ( width - texWidth ) / 2, y + ( height - texHeight ) / 2, texWidth, texHeight );

        reticle = new Reticle( x, y, width, height, x + width / 2, y + height / 2, texBounds );
        reticle.setColor( 0.2f, 0.2f, 0.2f, 0.2f );

        setType( type );
        setVolumeObject( volumeObject );
        setAxis( axis );
        setTexBounds( texBounds );

    }

    public ImageAxis getAxis()
    {
        return axis;
    }

    public abstract int getCurrentSlice();

    public abstract int getHorizontalMaxSlices();

    public abstract int getMaxSlices();

    public Reticle getReticle()
    {
        return reticle;
    }

    public abstract int getSliceHorizontalFromCursor();

    public abstract int getSliceVerticalFromCursor();

    public abstract int getSliceHorizontalFromReticle();
    
    public abstract int getSliceVerticalFromReticle();

    public Component getTexBounds()
    {
        return texBounds;
    }

    public ImagePlaneType getType()
    {
        return type;
    }

    public abstract int getVerticalMaxSlices();

    public VolumeObject getVolumeObject()
    {
        return volumeObject;
    }

    protected void setAxis( ImageAxis axis )
    {
        this.axis = axis;
    }

    public abstract void setCurrentSlice( int currentSlice );

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
    }

    protected void showTexPlane( GL2 gl )
    {
        gl.glMatrixMode( GL_TEXTURE );
        gl.glPushMatrix();

        transformTex( gl );

        float tz = getCurrentSlice() * 1.0f / ( getMaxSlices() - 1 );

        gl.glMatrixMode( GL_MODELVIEW );
        gl.glLoadIdentity();

        gl.glEnable( GL_TEXTURE_3D );
        gl.glBindTexture( GL_TEXTURE_3D, volumeObject.getTexId() );

        gl.glEnable( GL_BLEND );
        gl.glBlendFunc( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA );

        gl.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
        GLUtil.texQuad3DCentered(    gl,
                                    texBounds.getX(),
                                    texBounds.getY(),
                                    texBounds.getWidth(),
                                    texBounds.getHeight(),
                                    1,
                                    1,
                                    tz );
    
        gl.glDisable( GL_BLEND );
        gl.glDisable( GL_TEXTURE_3D );

        gl.glMatrixMode( GL_TEXTURE );
        gl.glPopMatrix();

        gl.glMatrixMode( GL_MODELVIEW );
        gl.glLoadIdentity();

    }

    protected abstract void transformTex( GL2 gl );

    public void updateReticle()
    {
        reticle.setRelativePosX( getSliceHorizontalFromCursor() * 1.0f / ( getHorizontalMaxSlices() - 1 ) );
        reticle.setRelativePosY( getSliceVerticalFromCursor() * 1.0f / ( getVerticalMaxSlices() - 1 ) );
    }

    public abstract void updateSliceCursor();

}