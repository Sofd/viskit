package de.sofd.viskit.image3D.jogl.view;

import java.io.*;

import javax.media.opengl.*;

import com.sun.opengl.util.texture.*;

import de.sofd.viskit.image3D.jogl.minigui.controller.*;
import de.sofd.viskit.image3D.jogl.minigui.util.*;
import de.sofd.viskit.image3D.jogl.minigui.view.*;
import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.model.*;

public class SliceViewport extends OrthoViewport
{
    // top, right, bottom, left
    protected int[] margin = new int[ 4 ];

    protected SlicePlane plane;

    protected Slider slider;

    protected TransferComponent transferComp;

    protected VolumeObject volumeObject;

    public SliceViewport(    int x,
                            int y,
                            int width,
                            int height,
                            ImageAxis axis,
                            ImagePlaneType planeType,
                            VolumeObject volumeObject ) throws IOException
    {
        super( x, y, width, height );

        this.volumeObject = volumeObject;

        setMargin( 10, 30, 30, 10 );

        plane = SlicePlaneFactory.getInstance().getPlane(    margin[3],
                                                            margin[2],
                                                            width - margin[1] - margin[3],
                                                            height - margin[0] - margin[2],
                                                            axis,
                                                            planeType,
                                                            volumeObject );

        Texture sliderBgTex = ResourceLoader.getImageTex( "minigui.slider.bg" );
        Texture sliderPinTex = ResourceLoader.getImageTex( "minigui.slider.pin" );

        slider = new SliderHorizontal(    margin[3],
                                        margin[2] - sliderBgTex.getImageHeight() - 2,
                                        plane.getWidth(),
                                        sliderBgTex.getHeight(),
                                        sliderBgTex,
                                        sliderPinTex,
                                        1,
                                        plane.getMaxSlices(),
                                        plane.getCurrentSlice() + 1,
                                        new float[]
                                        {
                                                1.0f, 1.0f, 1.0f, 1.0f
                                        } );

        Texture transferTex = ResourceLoader.getImageTex( "minigui.transfer.pin" );

        transferComp = new TransferComponent(    margin[3] + plane.getWidth() + 1,
                                                margin[2],
                                                margin[1] - 1,
                                                plane.getHeight(),
                                                (float)volumeObject.getRangeMin(),
                                                (float)volumeObject.getRangeMax(),
                                                volumeObject.getTransferTexId(),
                                                transferTex,
                                                volumeObject.getRelativeCursorValue() );

    }

    public SlicePlane getPlane()
    {
        return plane;
    }

    public Slider getSlider()
    {
        return slider;
    }

    public TransferComponent getTransferComp()
    {
        return transferComp;
    }

    public VolumeObject getVolumeObject()
    {
        return volumeObject;
    }

    public void setMargin(    int top,
                            int right,
                            int bottom,
                            int left )
    {
        margin[0] = top;
        margin[1] = right;
        margin[2] = bottom;
        margin[3] = left;

    }

    @Override
    public void show( GL2 gl )
    {
        beginViewport( gl );

        plane.show( gl );

        slider.show( gl );
        transferComp.show( gl );

        endViewport( gl );
    }

}