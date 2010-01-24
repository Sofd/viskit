package de.sofd.viskit.image3D.jogl.util;

import javax.media.opengl.*;

import de.sofd.util.*;
import de.sofd.viskit.image3D.jogl.model.*;

public class VolumeRenderFrameBuffer extends FrameBuffer {
    protected VolumeObject volumeObject;
    
    public VolumeRenderFrameBuffer(Size size, VolumeObject volumeObject) {
        super(size);
        this.volumeObject = volumeObject;
    }
    
    @Override
    public void run( GL2 gl )
    {
        begin( gl );
        
        attachTexture( gl, 0 );
        drawSlice( gl );
        
        end( gl );
    }
}