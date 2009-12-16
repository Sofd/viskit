package de.sofd.viskit.image3D.jogl.view;

import java.util.*;

import de.sofd.viskit.image3D.jogl.minigui.layout.*;
import de.sofd.viskit.image3D.jogl.minigui.view.*;

public class SliceView extends Component
{

    public SliceView( int x, int y, int width, int height )
    {
        super( x, y, width, height );
        this.setLayout( new GridLayout( 2, 2 ) );
    }
    
    public GridLayout getLayout()
    {
        return (GridLayout)layout;
    }
    
    public ArrayList<Component> getViewports()
    {
        return (ArrayList<Component>)layout.getComponents();
    }
}