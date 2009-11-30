package de.sofd.viskit.image3D.jogl.minigui;

public class DragComponent extends Component
{
    protected int oldMouseX;
    protected int oldMouseY;
    
    protected boolean isActive;
    
    protected int oldX;
    protected int oldY;
    
    protected int minX;
    protected int maxX;
    protected int minY;
    protected int maxY;

    public DragComponent( int x, int y, int width, int height, int minX, int maxX, int minY, int maxY ) {
        super( x, y, width, height );
        setLastX(0);
        setLastY(0);
        setActive(false);
        setMinX(minX);
        setMaxX(maxX);
        setMinY(minY);
        setMaxY(maxY);
        
    }

    public void dragged( int mouseX, int mouseY )
    {
        if ( isActive() )
        {
            this.x = Math.min( Math.max( minX, oldX + ( mouseX - oldMouseX ) ), maxX );
            this.y = Math.min( Math.max( minY, oldY + ( mouseY - oldMouseY ) ), maxY );
        }
    }

    public int getLastX() {
        return oldMouseX;
    }

    public int getLastY() {
        return oldMouseY;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }
    public int getMinX() {
        return minX;
    }
    public int getMinY() {
        return minY;
    }

    public boolean isActive() {
        return isActive;
    }

    public void pressed( int mouseX, int mouseY )
    {
        if ( isInBounds( mouseX, mouseY ) )
        {
            oldMouseX = mouseX;
            oldMouseY = mouseY;
            oldX = this.x;
            oldY = this.y;
            setActive(true);
        }
    }

    public void released()
    {
        setActive(false);
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    
    public void setLastX(int lastX) {
        this.oldMouseX = lastX;
    }

    

    public void setLastY(int lastY) {
        this.oldMouseY = lastY;
    }
    
    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }
    
    public void setMinX(int minX) {
        this.minX = minX;
    }
    
    public void setMinY(int minY) {
        this.minY = minY;
    }

    
    
    
    
    
}