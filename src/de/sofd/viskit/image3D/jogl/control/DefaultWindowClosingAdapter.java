package de.sofd.viskit.image3D.jogl.control;

import java.awt.event.*;

import javax.swing.*;

public class DefaultWindowClosingAdapter extends WindowAdapter
{
    JFrame frame;
    
    public DefaultWindowClosingAdapter(JFrame frame)
    {
        super();
        this.frame = frame;
    }

    @Override
    public void windowClosing( WindowEvent e )
    {
        SwingUtilities.invokeLater( new Runnable()
        {
            public void run()
            {
                //frame.dispose();
                
                System.exit( -1 );
            }
        });
        
    }
}