package de.sofd.viskit.image3D.jogl.minigui.controller;

import java.io.*;

import de.sofd.viskit.image3D.jogl.minigui.view.*;
import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.model.*;

public class SlicePlaneFactory
{
    protected static SlicePlaneFactory instance;

    public static SlicePlaneFactory getInstance()
    {
        if ( instance == null )
            instance = new SlicePlaneFactory();

        return instance;
    }

    protected SlicePlaneFactory()
    {
    }

    public SlicePlane getPlane( int x,
                                int y,
                                int width,
                                int height,
                                ImageAxis axis,
                                ImagePlaneType planeType,
                                VolumeObject volumeObject ) throws IOException
    {
        switch ( axis )
        {
            case AXIS_X:
                return new SlicePlaneX( x, y, width, height, planeType, volumeObject );
            case AXIS_Y:
                return new SlicePlaneY( x, y, width, height, planeType, volumeObject );
            case AXIS_Z:
                return new SlicePlaneZ( x, y, width, height, planeType, volumeObject );
        }

        return null;
    }

}