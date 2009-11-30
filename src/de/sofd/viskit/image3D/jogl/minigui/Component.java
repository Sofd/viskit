package de.sofd.viskit.image3D.jogl.minigui;

public class Component
{
    protected int x;
    protected int y;

    protected int width;
    protected int height;
    
    public Component( int x, int y, int width, int height ) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public int getHeight() {
        return height;
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
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public void setX(int x) {
        this.x = x;
    }
    
    public void setY(int y) {
        this.y = y;
    }
    
    public String toString()
    {
        return "[" + x + "," + y + "," + width + "," + height + "]";
    }
    
    
}