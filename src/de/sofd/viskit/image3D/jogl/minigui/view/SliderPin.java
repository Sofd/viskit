package de.sofd.viskit.image3D.jogl.minigui.view;

import static javax.media.opengl.GL.*;

import javax.media.opengl.*;

import com.sun.opengl.util.texture.*;

public class SliderPin extends TexComponent
{
    public SliderPin( int x, int y, Texture pinTex, float[] color ) {
        super( x, y, pinTex.getImageWidth(), pinTex.getImageHeight(), pinTex, color );
        
        pinTex.setTexParameteri( GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        pinTex.setTexParameteri( GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
        pinTex.setTexParameteri( GL_TEXTURE_MIN_FILTER, GL_LINEAR );
        pinTex.setTexParameteri( GL_TEXTURE_MAG_FILTER, GL_LINEAR );
        
    }
    
    public void show( GL2 gl )
    {
        super.show(gl);
    }
}