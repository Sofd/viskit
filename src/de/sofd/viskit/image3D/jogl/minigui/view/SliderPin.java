package de.sofd.viskit.image3D.jogl.minigui.view;

import static javax.media.opengl.GL.*;

import javax.media.opengl.*;

import com.sun.opengl.util.texture.*;

import de.sofd.util.*;

public class SliderPin extends DragTexComponent
{
    public SliderPin( int x, int y, TextureData pinTex, float[] color, Bounds bounds )
    {
        super( x, y, pinTex.getWidth(), pinTex.getHeight(), null, color, bounds );

    }
    
    public void glCreate()
    {
        tex.setTexParameteri( GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        tex.setTexParameteri( GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
        tex.setTexParameteri( GL_TEXTURE_MIN_FILTER, GL_LINEAR );
        tex.setTexParameteri( GL_TEXTURE_MAG_FILTER, GL_LINEAR );
    }
    
    public void show( GL2 gl )
    {
        super.show( gl );
    }
}