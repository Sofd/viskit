package de.sofd.viskit.image3D.jogl.view;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

import java.io.*;

import javax.media.opengl.*;

import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.jogl.util.*;
import de.sofd.viskit.image3D.model.*;

public class YSliceViewport extends SliceViewport
{
    public YSliceViewport( int x, int y, int width, int height, ImagePlaneType planeType, VolumeObject volumeObject ) throws IOException
    {
        super(x, y, width, height, planeType, volumeObject.getHeight(), volumeObject);
    }
    
    @Override
    protected void showTexPlane(GL2 gl)
    {
        float tSizeX = 1;
        float tSizeY = 1;
        float tz = ( currentSlice - 1 ) * 1.0f / ( maxSlices - 1 );
        float sizeX = (float)( volumeObject.getSizeX() * getPlaneWidth() / volumeObject.getMaxSize() );
        float sizeY = (float)( volumeObject.getSizeZ() * getPlaneHeight() / volumeObject.getMaxSize() );
        float x = margin[3] + ( getPlaneWidth() - sizeX ) / 2;
        float y = margin[2] + ( getPlaneHeight() - sizeY ) / 2;
        
        gl.glMatrixMode( GL_TEXTURE );
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glTranslatef(  0.5f,  0.5f,  0.5f );
        gl.glRotatef( 90.0f, 1.0f, 0.0f, 0.0f );
        gl.glTranslatef( -0.5f, -0.5f, -0.5f );
        
        gl.glMatrixMode( GL_MODELVIEW );
        gl.glLoadIdentity();
        
        gl.glEnable(GL_TEXTURE_3D);
        gl.glBindTexture(GL_TEXTURE_3D, volumeObject.getTexId());
        
        gl.glEnable(GL_BLEND);
        gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        gl.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
        GLUtil.texQuad3DCentered( gl, x, y, sizeX, sizeY, tSizeX, tSizeY, tz );
        
        gl.glDisable(GL_BLEND);
        gl.glDisable(GL_TEXTURE_3D);
        
        gl.glColor4f( 1.0f, 0.0f, 0.0f, 1.0f );
        GLUtil.lineQuad( gl, x, y, sizeX, sizeY );
        
        gl.glMatrixMode( GL_TEXTURE );
        gl.glPopMatrix();
        
        gl.glMatrixMode( GL_MODELVIEW );
        gl.glLoadIdentity();
    }
}
