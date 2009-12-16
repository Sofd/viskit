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
    
    protected Component component;

    public DragController( Component component ) {
        setComponent(component);
        setLastX(0);
        setLastY(0);
        setActive(false);
    }
    
    public void dragged( int button, int mouseX, int mouseY )
    {
        if ( isActive() )
        {
            component.setX( oldX + ( mouseX - oldMouseX ) );
            component.setY( oldY + ( mouseY - oldMouseY ) );
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
}