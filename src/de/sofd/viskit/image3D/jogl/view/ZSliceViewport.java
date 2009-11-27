package de.sofd.viskit.image3D.jogl.view;

import static javax.media.opengl.GL2.*;

import javax.media.opengl.*;

import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.model.*;

public abstract class ZSliceViewport extends SliceViewport
{
    public ZSliceViewport( int x, int y, int width, int height, ImagePlaneType planeType, int maxSlices, VolumeObject volumeObject )
    {
        super(x, y, width, height, planeType, maxSlices, volumeObject);
    }
    
    @Override
    protected void showTexPlane(GL2 gl)
    {
        gl.glEnable(GL_TEXTURE_3D);
        gl.glBindTexture(GL_TEXTURE_3D, volumeObject.getTexId());
        
        gl.glBegin(GL_QUADS);
            
        gl.glEnd();
        
        gl.glDisable(GL_TEXTURE_3D);
    }
}
