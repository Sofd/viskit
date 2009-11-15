package de.sofd.viskit.image3D.vtk.controller;

import static de.sofd.viskit.image3D.vtk.model.InteractionMode.*;

import de.sofd.viskit.image3D.vtk.model.*;
import de.sofd.viskit.image3D.vtk.util.*;
import de.sofd.viskit.image3D.vtk.view.*;

import java.awt.event.*;

import vtk.*;

public class DefaultVtkSceneInputController implements MouseListener, MouseMotionListener, KeyListener {
    protected InteractionMode interactionMode = MODE_ROTATE;
    
    protected int lastX;
    protected int lastY;

    protected VtkScenePanel panel;
    protected vtkRenderer renderer;
    protected vtkRenderWindow renderWindow;

    public DefaultVtkSceneInputController(
            vtkRenderer renderer,
            vtkRenderWindow renderWindow,
            VtkScenePanel panel) 
    {
        this.renderer = renderer;
        this.renderWindow = renderWindow;
        this.panel = panel;
    }
    
    public void keyPressed(KeyEvent e) {
        if (renderer.VisibleActorCount() == 0)
            return;
        
        switch ( e.getKeyChar() )
        {
            case 'i' :
                panel.infoLog(System.out);
                break;
            case 'r' :
                panel.resetCamera();
                panel.update();
                break;
            case 's' :
                //solid mode
                RenderUtil.setSolidMode(renderer);
                panel.update();
                break;
            case 'u' :
                panel.pickActor(lastX, lastY);
                break;
            case 'w' :    
                //wireframe mode
                RenderUtil.setWireframeMode(renderer);
                panel.update();
                break;
        }
        
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    public void mouseDragged(MouseEvent e) {
        if (renderer.VisibleActorCount() == 0)
            return;
        
        int x = e.getX();
        int y = e.getY();
        
        switch ( interactionMode )
        {
            case MODE_ROTATE :
                panel.rotateCamera(x, y, lastX, lastY);
                break;
            case MODE_TRANSLATE :
                panel.translateCamera(x, y, lastX, lastY);
                break;
            case MODE_ZOOM :    
                panel.zoomCam(x, y, lastX, lastY);
                break;
        }

        lastX = x;
        lastY = y;
        
        panel.update();
    }

    public void mouseEntered(MouseEvent e) {
        panel.requestFocus();
    }

    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }
    
    public void mouseMoved(MouseEvent e) {
        lastX = e.getX();
        lastY = e.getY();
        //panel.render();
    }
    
    public void mousePressed(MouseEvent e) {

        if (renderer.VisibleActorCount() == 0)
            return;

        renderWindow.SetDesiredUpdateRate(5.0);
        lastX = e.getX();
        lastY = e.getY();

        if ((e.getModifiers() == InputEvent.BUTTON2_MASK)
                || (e.getModifiers() == (InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK))) {
            interactionMode = MODE_TRANSLATE;
        } else if (e.getModifiers() == InputEvent.BUTTON3_MASK) {
            interactionMode = MODE_ZOOM;
        } else {
            interactionMode = MODE_ROTATE;
        }
    }

    public void mouseReleased(MouseEvent e) {
        renderWindow.SetDesiredUpdateRate(0.01);
    }
}
