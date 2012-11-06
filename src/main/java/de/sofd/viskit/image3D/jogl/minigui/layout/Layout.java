package de.sofd.viskit.image3D.jogl.minigui.layout;

import java.util.*;

import de.sofd.util.*;
import de.sofd.viskit.image3D.jogl.minigui.view.*;

public abstract class Layout
{
    protected ArrayList<Component> components;

    public Layout()
    {
        components = new ArrayList<Component>();
    }

    public Layout( int nrOfComponents )
    {
        components = new ArrayList<Component>( nrOfComponents );
        for ( int i = 0; i < nrOfComponents; ++i )
            components.add( null );
    }

    public void add( Component component )
    {
        this.components.add( component );
    }

    public void checkMaximized()
    {
        boolean oneIsMaximized = false;
        
        for ( Component component : components )
        {
            if ( component != null && component.isVisible() && component.isMaximized() )
            {
                oneIsMaximized = true;
                
                for ( Component component2 : components )
                {
                    if ( component2 != null && component != component2 )
                    {
                        component2.setVisible( false );
                    }
                }
            }
        }
        
        if ( ! oneIsMaximized )
        {
            for ( Component component : components )
            {
                if ( component != null )
                    component.setVisible( true );
            }
        }
    }
    
    public ArrayList<Component> getComponents()
    {
        return components;
    }

    public abstract Size getPreferredSize(    int width, int height );

    public abstract void pack( int x, int y, int width, int height );

    public abstract void resize(    int x,
                                    int y,
                                    int width,
                                    int height );
}