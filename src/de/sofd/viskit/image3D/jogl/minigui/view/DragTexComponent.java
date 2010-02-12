package de.sofd.viskit.image3D.jogl.minigui.view;

import javax.media.opengl.*;

import com.sun.opengl.util.texture.*;

import de.sofd.util.*;
import de.sofd.viskit.image3D.jogl.util.*;

public class DragTexComponent extends DragComponent
{
    protected Bounds bounds;
    
    protected Texture tex;
    protected float[] color;

    public DragTexComponent( int x, int y, int width, int height, Texture tex, float[] color, Bounds bounds )
    {
        super( x, y, width, height, bounds );
        setTex(tex);
        setColor(color);
    }
    
    public float[] getColor() {
        return color;
    }

    public Texture getTex() {
        return tex;
    }

    public void setColor(float[] color) {
        this.color = color;
    }
    
    public void setTex(Texture tex) {
        this.tex = tex;
    }
    
    public void show( GL2 gl )
    {
        tex.bind();
        gl.glColor4fv( color, 0 );
        
        GLUtil.texQuad2D( gl, x, y - 1, width, height, 0, 1, 1, -1 );
    }

}