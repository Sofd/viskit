package de.sofd.viskit.image3D.jogl.minigui;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;

import java.io.*;

import javax.media.opengl.*;

import com.sun.opengl.util.texture.*;

import de.sofd.viskit.image3D.jogl.util.*;

public class Reticle extends Component
{

    protected Texture[] retTex = new Texture[5];
    
    protected int posX;
    protected int posY;
    
    protected float[] color = { 1.0f, 1.0f, 1.0f, 1.0f };

    public Reticle( int x, int y, int width, int height, int posX, int posY ) throws IOException {
        super( x, y, width, height );
        
        setPosX(posX);
        setPosY(posY);
        retTex[0] = ResourceLoader.getImageTex("minigui.reticle.top");
        retTex[1] = ResourceLoader.getImageTex("minigui.reticle.right");
        retTex[2] = ResourceLoader.getImageTex("minigui.reticle.bottom");
        retTex[3] = ResourceLoader.getImageTex("minigui.reticle.left");
        retTex[4] = ResourceLoader.getImageTex("minigui.reticle.center");
        
        for ( int i = 0; i < 5; ++i )
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
        retTex[4].setTexParameteri( GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        retTex[4].setTexParameteri( GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
        
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }
    
    public void setColor( float r, float g, float b, float a )
    {
        color[0] = r;
        color[1] = g;
        color[2] = b;
        color[3] = a;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }
    
    public void setPosY(int posY) {
        this.posY = posY;
    }
    
    public void show( GL2 gl )
    {
        gl.glEnable( GL_TEXTURE_2D );
        gl.glEnable( GL_BLEND );
        gl.glBlendFunc( GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA );
        
        showLineTop( gl );
        showLineRight( gl );
        showLineBottom( gl );
        showLineLeft( gl );
        showCross( gl );
        
        gl.glDisable( GL_BLEND );
        gl.glDisable( GL_TEXTURE_2D );
        
    }
    
    public void showCross( GL2 gl )
    {
        float lineWidth = retTex[4].getImageWidth();
        float lineHeight = retTex[4].getImageHeight();
        float texWidth = 1.0f;
        float texHeight = 1.0f;
        
        retTex[4].bind();
        gl.glTexEnvf( GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE );
        gl.glColor4fv( color, 0 );
        
        GLUtil.texQuad2D( gl, posX - lineWidth / 2, posY - lineHeight / 2, lineWidth, lineHeight, 0, texHeight, texWidth, -texHeight );
    }
    
    public void showLineTop( GL2 gl )
    {
        float lineWidth = retTex[0].getImageWidth();
        float lineHeight = posY - y - retTex[4].getImageHeight() / 2;
        float texWidth = 1.0f;
        float texHeight = lineHeight/retTex[0].getImageHeight();
        
        retTex[0].bind();
        gl.glTexEnvf( GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE );
        gl.glColor4fv( color, 0 );
        
        GLUtil.texQuad2D( gl, posX - lineWidth / 2, posY + retTex[4].getImageHeight() / 2, lineWidth, lineHeight, 0, 0, texWidth, -texHeight );
    }
    
    public void showLineRight( GL2 gl )
    {
        float lineWidth = posX - x - retTex[4].getImageWidth()/2 - 1;
        float lineHeight = retTex[1].getImageHeight();
        float texWidth = lineWidth/retTex[1].getImageWidth();
        float texHeight = 1.0f;
        
        retTex[1].bind();
        gl.glTexEnvf( GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE );
        gl.glColor4fv( color, 0 );
        
        GLUtil.texQuad2D( gl, posX + retTex[4].getImageWidth() / 2 + 1, posY - lineHeight / 2, lineWidth, lineHeight, 0, texHeight, texWidth, -texHeight );
    }
    
    public void showLineBottom( GL2 gl )
    {
        float lineWidth = retTex[2].getImageWidth();
        float lineHeight = posY - y - retTex[4].getImageHeight() / 2 - 1;
        float texWidth = 1.0f;
        float texHeight = lineHeight/retTex[2].getImageHeight();
        
        retTex[2].bind();
        gl.glTexEnvf( GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE );
        gl.glColor4fv( color, 0 );
        
        GLUtil.texQuad2D( gl, posX - lineWidth / 2, y, lineWidth, lineHeight, 0, texHeight, texWidth, -texHeight );
    }
    
    public void showLineLeft( GL2 gl )
    {
        float lineWidth = posX - x - retTex[4].getImageWidth()/2;
        float lineHeight = retTex[3].getImageHeight();
        float texWidth = lineWidth/retTex[3].getImageWidth();
        float texHeight = 1.0f;
        
        retTex[3].bind();
        gl.glTexEnvf( GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE );
        gl.glColor4fv( color, 0 );
        
        GLUtil.texQuad2D( gl, x, posY - lineHeight / 2, lineWidth, lineHeight, -texWidth, texHeight, texWidth, -texHeight );
    }
}