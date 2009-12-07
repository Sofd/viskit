package de.sofd.viskit.image3D.jogl.view;

import static javax.media.opengl.fixedfunc.GLMatrixFunc.*;

import javax.media.opengl.*;

public abstract class OrthoViewport
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
        gl.glViewport( x, y, width, height ); 
        
        gl.glMatrixMode(GL_PROJECTION);
        gl.glPushMatrix();
        
        gl.glLoadIdentity();
        gl.glOrtho(0, width, 0, height, -1.0, 1.0);
        
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
    }
    
    protected void endViewport(GL2 gl)
    {
        gl.glMatrixMode(GL_PROJECTION);
        gl.glPopMatrix();
        
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
    }
    
    public int getHeight() {
        return height;
    }
    
    public int getRelativeMouseX( int x )
    {
        return x - this.x;
    }
    
    public int getRelativeMouseY( int y )
    {
        return y - this.y;
    }

    public int getWidth() {
        return width;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    public boolean isInBounds( int x, int y )
    {
        return ( x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + height );
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public void setLocation( int x, int y )
    {
        setX(x);
        setY(y);
    }
    
    public void setLocationAndSize( int x, int y, int width, int height )
    {
        setLocation( x, y );
        setSize( width, height );
    }
    
    public void setSize( int width, int height )
    {
        setWidth(width);
        setHeight(height);
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public abstract void show(GL2 gl);
}