package de.sofd.viskit.image3D.jogl.minigui.view;

import static javax.media.opengl.GL.*;

import com.sun.opengl.util.texture.*;

public class ReticleCross extends TexComponent
{

    protected int posX;
    protected int posY;
    
    public ReticleCross( Texture tex, float[] color, int posX, int posY ) {
        super( posX - tex.getImageWidth() / 2, posY - tex.getImageHeight() / 2, tex.getImageWidth(), tex.getImageHeight(), tex, color );
        setTex( tex );
        setPosX( posX );
        setPosY( posY );
        setColor( color );
        
        tex.setTexParameteri( GL_TEXTURE_MIN_FILTER, GL_NEAREST );
        tex.setTexParameteri( GL_TEXTURE_MAG_FILTER, GL_NEAREST );
        tex.setTexParameteri( GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        tex.setTexParameteri( GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosX(int posX) {
        this.posX = posX;
        setX( posX - getWidth() / 2 );
    }

    public void setPosY(int posY) {
        this.posY = posY;
        setY( posY - getHeight() / 2 );
    }
    
}