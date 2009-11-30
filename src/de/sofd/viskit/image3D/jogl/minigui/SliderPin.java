package de.sofd.viskit.image3D.jogl.minigui;

import static javax.media.opengl.GL.*;

import javax.media.opengl.*;

import com.sun.opengl.util.texture.*;

import de.sofd.viskit.image3D.jogl.util.*;

public class SliderPin extends DragComponent
{
    protected Texture pinTex;
    
    public SliderPin( int x, int y, Texture pinTex, int minX, int maxX ) {
        super( x, y, pinTex.getImageWidth(), pinTex.getImageHeight(), minX, maxX, y, y );
        
        this.pinTex = pinTex;
        
        pinTex.setTexParameteri( GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        pinTex.setTexParameteri( GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
        pinTex.setTexParameteri( GL_TEXTURE_MIN_FILTER, GL_LINEAR );
        pinTex.setTexParameteri( GL_TEXTURE_MAG_FILTER, GL_LINEAR );
        
    }
    
    public void show( GL2 gl )
    {
        pinTex.bind();
        gl.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
        GLUtil.texQuad2D( gl, x, y, width, height, 0, 1.0f, 1.0f, -1.0f );
        
        
    }

    public float getRelativePosition() {
        float r = ( x - minX ) * 1.0f / ( maxX - minX );
        return r;
    }
    
    public void setRelativePosition( float r ) {
        this.x = (int)(minX + r * ( maxX - minX ));
        
    }
}