package de.sofd.viskit.image3D.jogl.minigui.controller;

import java.awt.event.*;

import de.sofd.viskit.image3D.jogl.minigui.view.*;

public class DragController 
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
    
    protected Component component;

    public DragController( Component component, int minX, int maxX, int minY, int maxY ) {
        setComponent(component);
        setLastX(0);
        setLastY(0);
        setActive(false);
        setMinX(minX);
        setMaxX(maxX);
        setMinY(minY);
        setMaxY(maxY);
        
    }

    public void dragged( int button, int mouseX, int mouseY )
    {
        if ( isActive() )
        {
            component.setX( Math.min( Math.max( minX, oldX + ( mouseX - oldMouseX ) ), maxX ) );
            component.setY( Math.min( Math.max( minY, oldY + ( mouseY - oldMouseY ) ), maxY ) );
        }
    }

    public Component getComponent() {
        return component;
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
    public void pressed( int button, int mouseX, int mouseY )
    {
        if ( component.isInBounds( mouseX, mouseY ) && button == MouseEvent.BUTTON1 )
        {
            oldMouseX = mouseX;
            oldMouseY = mouseY;
            oldX = component.getX();
            oldY = component.getY();
            setActive(true);
        }
    }
    public void released( int button )
    {
        if ( button == MouseEvent.BUTTON1 )
        {
            setActive(false);
        }
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    protected void setComponent(Component component) {
        this.component = component;
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