package de.sofd.viskit.image3D.jogl.control;

import java.awt.event.*;

public class SliceViewWindowAdapter extends WindowAdapter
{
    public SliceViewWindowAdapter()
    {
        super();
    }

    @Override
    public void windowClosing( WindowEvent e )
    {
        System.exit( 0 );
    }
}