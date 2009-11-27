package de.sofd.viskit.image3D.jogl.view;

import static javax.media.opengl.fixedfunc.GLMatrixFunc.*;

import javax.media.opengl.*;

public class OrthoViewport
{
    protected int x;
    protected int y;
    
    protected int width;
    protected int height;

    public OrthoViewport( int x, int y, int width, int height )
    {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
    }
    
    protected void beginViewport(GL2 gl)
    {
        gl.glViewport( x, y, x + width, y + height ); 
        
        gl.glMatrixMode(GL_PROJECTION);
        gl.glPushMatrix();
        
        gl.glLoadIdentity();
        gl.glOrtho(-width/2, width/2, -height/2, height/2, -1.0, 1.0);
        
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
    }
    
    protected void endViewport(GL2 gl)
    {
        gl.glViewport( x, y, x + width, y + height ); 
        
        gl.glMatrixMode(GL_PROJECTION);
        gl.glPopMatrix();
        
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
    }
    
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}