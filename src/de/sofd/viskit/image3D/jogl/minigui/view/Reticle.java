package de.sofd.viskit.image3D.jogl.minigui.view;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

import java.io.*;

import javax.media.opengl.*;

import com.sun.opengl.util.texture.*;

import de.sofd.viskit.image3D.jogl.minigui.util.*;
import de.sofd.viskit.image3D.jogl.util.*;

public class Reticle extends Component
{

    protected Texture[] retTex = new Texture[ 4 ];

    protected float[] color =
    {
            1.0f, 1.0f, 1.0f, 1.0f
    };

    protected ReticleCross cross;

    protected Component moveBounds;

    public Reticle( int x, int y, int width, int height, int posX, int posY, Component moveBounds ) throws IOException
    {
        super( x, y, width, height );

        cross = new ReticleCross( color, posX, posY );

        setMoveBounds( moveBounds );
    }
    
    public ReticleCross getCross()
    {
        return cross;
    }

    public Component getMoveBounds()
    {
        return moveBounds;
    }

    public int getPosX()
    {
        return cross.getPosX();
    }

    public int getPosY()
    {
        return cross.getPosY();
    }

    public float getRelativeXPosition()
    {
        float rx = ( getPosX() - moveBounds.getX() ) * 1.0f / moveBounds.getWidth();
        return rx;
    }

    public float getRelativeYPosition()
    {
        float ry = ( getPosY() - moveBounds.getY() ) * 1.0f / moveBounds.getHeight();
        return ry;
    }

    @Override
    public void glCreate() throws Exception
    {
        retTex[0] = ResourceLoader.getImageTex( "minigui.reticle.top" );
        retTex[1] = ResourceLoader.getImageTex( "minigui.reticle.right" );
        retTex[2] = ResourceLoader.getImageTex( "minigui.reticle.bottom" );
        retTex[3] = ResourceLoader.getImageTex( "minigui.reticle.left" );

        for ( int i = 0; i < retTex.length; ++i )
        {
            retTex[i].setTexParameteri( GL_TEXTURE_MIN_FILTER, GL_NEAREST );
            retTex[i].setTexParameteri( GL_TEXTURE_MAG_FILTER, GL_NEAREST );
        }

        retTex[0].setTexParameteri( GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        retTex[0].setTexParameteri( GL_TEXTURE_WRAP_T, GL_REPEAT );
        retTex[1].setTexParameteri( GL_TEXTURE_WRAP_S, GL_REPEAT );
        retTex[1].setTexParameteri( GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
        retTex[2].setTexParameteri( GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        retTex[2].setTexParameteri( GL_TEXTURE_WRAP_T, GL_REPEAT );
        retTex[3].setTexParameteri( GL_TEXTURE_WRAP_S, GL_REPEAT );
        retTex[3].setTexParameteri( GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
        
        cross.glCreate();
    }
    
    public void setColor(    float r,
                            float g,
                            float b,
                            float a )
    {
        color[0] = r;
        color[1] = g;
        color[2] = b;
        color[3] = a;
    }

    protected void setMoveBounds( Component moveBounds )
    {
        this.moveBounds = moveBounds;
    }

    public void setPosX( int posX )
    {
        cross.setPosX( posX );
    }

    public void setPosY( int posY )
    {
        cross.setPosY( posY );
    }

    public void setRelativePosX( float rx )
    {
        setPosX( (int)( rx * moveBounds.getWidth() + moveBounds.getX() ) );
    }

    public void setRelativePosY( float ry )
    {
        setPosY( (int)( ry * moveBounds.getHeight() + moveBounds.getY() ) );
    }

    public void show( GL2 gl )
    {
        gl.glEnable( GL_TEXTURE_2D );
        gl.glEnable( GL_BLEND );
        gl.glBlendFunc( GL_ONE, GL_ONE_MINUS_SRC_ALPHA );

        showLineTop( gl );
        showLineRight( gl );
        showLineBottom( gl );
        showLineLeft( gl );

        cross.show( gl );

        gl.glDisable( GL_BLEND );
        gl.glDisable( GL_TEXTURE_2D );

    }

    public void showLineBottom( GL2 gl )
    {
        float lineWidth = retTex[2].getImageWidth();
        float lineHeight = getPosY() - y - cross.getHeight() / 2 - 1;
        float texWidth = 1.0f;
        float texHeight = lineHeight / retTex[2].getImageHeight();

        retTex[2].bind();
        gl.glTexEnvf( GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE );
        gl.glColor4fv( color, 0 );

        GLUtil.texQuad2D( gl, getPosX() - lineWidth / 2, y, lineWidth, lineHeight, 0, texHeight, texWidth, -texHeight );
    }

    public void showLineLeft( GL2 gl )
    {
        float lineWidth = getPosX() - x - cross.getWidth() / 2;
        float lineHeight = retTex[3].getImageHeight();
        float texWidth = lineWidth / retTex[3].getImageWidth();
        float texHeight = 1.0f;

        retTex[3].bind();
        gl.glTexEnvf( GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE );
        gl.glColor4fv( color, 0 );

        GLUtil.texQuad2D(    gl,
                            x,
                            getPosY() - lineHeight / 2,
                            lineWidth,
                            lineHeight,
                            -texWidth,
                            texHeight,
                            texWidth,
                            -texHeight );
    }

    public void showLineRight( GL2 gl )
    {
        float lineWidth = x + width - getPosX() - cross.getWidth() / 2 - 1;
        float lineHeight = retTex[1].getImageHeight();
        float texWidth = lineWidth / retTex[1].getImageWidth();
        float texHeight = 1.0f;

        retTex[1].bind();
        gl.glTexEnvf( GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE );
        gl.glColor4fv( color, 0 );

        GLUtil.texQuad2D(    gl,
                            getPosX() + cross.getWidth() / 2 + 1,
                            getPosY() - lineHeight / 2,
                            lineWidth,
                            lineHeight,
                            0,
                            texHeight,
                            texWidth,
                            -texHeight );
    }
    
    public void showLineTop( GL2 gl )
    {
        float lineWidth = retTex[0].getImageWidth();
        float lineHeight = y + height - getPosY() - cross.getHeight() / 2;
        float texWidth = 1.0f;
        float texHeight = lineHeight / retTex[0].getImageHeight();

        retTex[0].bind();
        gl.glTexEnvf( GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE );
        gl.glColor4fv( color, 0 );

        GLUtil.texQuad2D(    gl,
                            getPosX() - lineWidth / 2,
                            getPosY() + cross.getHeight() / 2,
                            lineWidth,
                            lineHeight,
                            0,
                            0,
                            texWidth,
                            -texHeight );
    }

}