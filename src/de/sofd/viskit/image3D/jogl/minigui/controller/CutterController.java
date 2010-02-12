package de.sofd.viskit.image3D.jogl.minigui.controller;

import java.awt.*;
import java.awt.Component;
import java.awt.event.*;
import java.util.*;

import de.sofd.viskit.image3D.jogl.minigui.view.*;

public class CutterController extends DragController
{
    protected final ArrayList<Integer> cursorList = new ArrayList<Integer>();
    
    protected Component awtParent;
    
    protected int cursorType;

    public CutterController( CutterPlane cutterPlane, Component awtParent, Robot robot )
    {
        super( cutterPlane, robot );
        
        this.awtParent = awtParent;
        
        cursorList.add( Cursor.SW_RESIZE_CURSOR );
        cursorList.add( Cursor.NW_RESIZE_CURSOR );
        cursorList.add( Cursor.SE_RESIZE_CURSOR );
        cursorList.add( Cursor.NE_RESIZE_CURSOR );
        cursorList.add( Cursor.N_RESIZE_CURSOR );
        cursorList.add( Cursor.S_RESIZE_CURSOR );
        cursorList.add( Cursor.W_RESIZE_CURSOR );
        cursorList.add( Cursor.E_RESIZE_CURSOR );
        
    }
    
    @Override
    public void dragged( MouseEvent e, int mouseX, int mouseY )
    {
        CutterPlane cutter = getCutterPlane();
        
        if ( isActive() )
        {
            switch ( cursorType )
            {
                case Cursor.N_RESIZE_CURSOR :
                    cutter.setVertiMax( oldY + ( mouseY - oldMouseY ) );
                    break;
                case Cursor.NE_RESIZE_CURSOR :
                    cutter.setVertiMax( oldY + ( mouseY - oldMouseY ) );
                    cutter.setHoriMax( oldX + ( mouseX - oldMouseX ) );
                    break;    
                case Cursor.E_RESIZE_CURSOR :
                    cutter.setHoriMax( oldX + ( mouseX - oldMouseX ) );
                    break;    
                case Cursor.SE_RESIZE_CURSOR :
                    cutter.setVertiMin( oldY + ( mouseY - oldMouseY ) );
                    cutter.setHoriMax( oldX + ( mouseX - oldMouseX ) );
                    break;    
                case Cursor.S_RESIZE_CURSOR :
                    cutter.setVertiMin( oldY + ( mouseY - oldMouseY ) );
                    break;
                case Cursor.SW_RESIZE_CURSOR :
                    cutter.setVertiMin( oldY + ( mouseY - oldMouseY ) );
                    cutter.setHoriMin( oldX + ( mouseX - oldMouseX ) );
                    break;    
                case Cursor.W_RESIZE_CURSOR :
                    cutter.setHoriMin( oldX + ( mouseX - oldMouseX ) );
                    break;    
                case Cursor.NW_RESIZE_CURSOR :
                    cutter.setVertiMax( oldY + ( mouseY - oldMouseY ) );
                    cutter.setHoriMin( oldX + ( mouseX - oldMouseX ) );
                    break;
            }
        }
    }
    
    public CutterPlane getCutterPlane()
    {
        return (CutterPlane)getComponent();
    }
    
    public void mousePressed(    int button,
                                int mouseX,
                                int mouseY )
    {
        CutterPlane cutter = getCutterPlane();
        
        if ( cursorList.contains( awtParent.getCursor().getType() ) )
        {
            
            if ( component.isInBounds( mouseX, mouseY ) && button == MouseEvent.BUTTON1 )
            {
                cursorType = awtParent.getCursor().getType();
                
                oldMouseX = mouseX;
                oldMouseY = mouseY;
                
                switch ( cursorType )
                {
                    case Cursor.N_RESIZE_CURSOR :
                        oldY = (int)cutter.getTopBoundY();
                        break;
                    case Cursor.NE_RESIZE_CURSOR :
                        oldY = (int)cutter.getTopBoundY();
                        oldX = (int)cutter.getRightBoundX();
                        break;    
                    case Cursor.E_RESIZE_CURSOR :
                        oldX = (int)cutter.getRightBoundX();
                        break;    
                    case Cursor.SE_RESIZE_CURSOR :
                        oldY = (int)cutter.getBottomBoundY();
                        oldX = (int)cutter.getRightBoundX();
                        break;    
                    case Cursor.S_RESIZE_CURSOR :
                        oldY = (int)cutter.getBottomBoundY();
                        break;
                    case Cursor.SW_RESIZE_CURSOR :
                        oldY = (int)cutter.getBottomBoundY();
                        oldX = (int)cutter.getLeftBoundX();
                        break;    
                    case Cursor.W_RESIZE_CURSOR :
                        oldX = (int)cutter.getLeftBoundX();
                        break;    
                    case Cursor.NW_RESIZE_CURSOR :
                        oldY = (int)cutter.getTopBoundY();
                        oldX = (int)cutter.getLeftBoundX();
                        break;
                }
                
                setActive(true);
            }
                
        }
    }
    
    public void mouseReleased(    int button,
                                int mouseX,
                                int mouseY )
    {
        if ( cursorList.contains( awtParent.getCursor().getType() ) )
        {
            super.released( button );
        }
    }
    
    public void mouseMoved( MouseEvent e,
                            int mouseX,
                            int mouseY )
    {
        CutterPlane cutter = getCutterPlane();
        
        if ( cutter.touchesLeftBound( mouseX ) && cutter.touchesBottomBound( mouseY ) )
            awtParent.setCursor( Cursor.getPredefinedCursor( Cursor.SW_RESIZE_CURSOR ) );
        else if ( cutter.touchesLeftBound( mouseX ) && cutter.touchesTopBound( mouseY ) )
            awtParent.setCursor( Cursor.getPredefinedCursor( Cursor.NW_RESIZE_CURSOR ) );
        else if ( cutter.touchesRightBound( mouseX ) && cutter.touchesBottomBound( mouseY ) )
            awtParent.setCursor( Cursor.getPredefinedCursor( Cursor.SE_RESIZE_CURSOR ) );
        else if ( cutter.touchesRightBound( mouseX ) && cutter.touchesTopBound( mouseY ) )
            awtParent.setCursor( Cursor.getPredefinedCursor( Cursor.NE_RESIZE_CURSOR ) );
        else if ( cutter.touchesLeftBound( mouseX ) )
            awtParent.setCursor( Cursor.getPredefinedCursor( Cursor.W_RESIZE_CURSOR ) );
        else if ( cutter.touchesRightBound( mouseX ) )
            awtParent.setCursor( Cursor.getPredefinedCursor( Cursor.E_RESIZE_CURSOR ) );
        else if ( cutter.touchesBottomBound( mouseY ) )
            awtParent.setCursor( Cursor.getPredefinedCursor( Cursor.S_RESIZE_CURSOR ) );
        else if ( cutter.touchesTopBound( mouseY ) )
            awtParent.setCursor( Cursor.getPredefinedCursor( Cursor.N_RESIZE_CURSOR ) );
        else if ( cursorList.contains( awtParent.getCursor().getType() ) ) 
            awtParent.setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );

    }
}