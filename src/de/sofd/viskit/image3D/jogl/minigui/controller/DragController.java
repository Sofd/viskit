package de.sofd.viskit.image3D.jogl.minigui.controller;

import java.awt.*;
import java.awt.event.*;

import de.sofd.viskit.image3D.jogl.minigui.view.*;

public class DragController extends ComponentController
{
    protected int oldMouseX;
    protected int oldMouseY;
    
    protected boolean isActive;
    
    protected int oldX;
    protected int oldY;
    
    protected Robot robot;

    public DragController( DragComponent component, Robot robot ) {
        super(component);
        
        setLastX(0);
        setLastY(0);
        setActive(false);
        this.robot = robot;
    }
    
    public void dragged( MouseEvent e, int mouseX, int mouseY )
    {
        if ( isActive() )
        {
            int nx = oldX + ( mouseX - oldMouseX ); 
            int ny = oldY + ( mouseY - oldMouseY );
            
            component.setX( nx );
            component.setY( ny );
            
//            
//            Point mouseInBounds = component.getInBounds( component.getBounds().getDeltaX() > 0 ? mouseX : oldMouseX, component.getBounds().getDeltaY() > 0 ? mouseY : oldMouseY );
//            int mx = (int)mouseInBounds.getX();
//            int my = (int)mouseInBounds.getY();
//        
//            if ( mx != mouseX || my != mouseY ) {
//                Point mouseOnScreen = e.getLocationOnScreen();
//                    
//                robot.mouseMove((int)mouseOnScreen.getX() - mouseX + mx, (int)mouseOnScreen.getY() - my + mouseY);
//            }
        }
    }

    public DragComponent getDragComponent() {
        return (DragComponent)getComponent();
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
    
    public void setLastX(int lastX) {
        this.oldMouseX = lastX;
    }

    public void setLastY(int lastY) {
        this.oldMouseY = lastY;
    }

    
}