package de.sofd.viskit.image3D.jogl.minigui.view;

import static javax.media.opengl.GL.*;

import de.sofd.viskit.image3D.jogl.minigui.util.*;

public class ReticleCross extends TexComponent
{

    protected int posX;
    protected int posY;

    public ReticleCross( float[] color, int posX, int posY )
    {
        super(    posX - 4,
                posY - 4,
                9,
                9,
                null,
                color );
        setPosX( posX );
        setPosY( posY );
        setColor( color );

        
    }
    
    public int getPosX()
    {
        return posX;
    }

    public int getPosY()
    {
        return posY;
    }

    @Override
    public void glCreate() throws Exception
    {
        this.tex = ResourceLoader.getImageTex( "minigui.reticle.center" );
        
        this.setSize( posX - tex.getImageWidth() / 2, posY - tex.getImageHeight() / 2, tex.getImageWidth(), tex.getImageHeight() );
        
        this.tex.setTexParameteri( GL_TEXTURE_MIN_FILTER, GL_NEAREST );
        this.tex.setTexParameteri( GL_TEXTURE_MAG_FILTER, GL_NEAREST );
        this.tex.setTexParameteri( GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        this.tex.setTexParameteri( GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
    }

    public void setPosX( int posX )
    {
        this.posX = posX;
        setX( posX - getWidth() / 2 );
    }

    public void setPosY( int posY )
    {
        this.posY = posY;
        setY( posY - getHeight() / 2 );
    }

}