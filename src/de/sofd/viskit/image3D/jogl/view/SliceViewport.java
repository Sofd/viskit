package de.sofd.viskit.image3D.jogl.view;

import java.io.*;

import javax.media.opengl.*;

import com.sun.opengl.util.texture.*;

import de.sofd.viskit.image3D.jogl.minigui.controller.*;
import de.sofd.viskit.image3D.jogl.minigui.layout.*;
import de.sofd.viskit.image3D.jogl.minigui.util.*;
import de.sofd.viskit.image3D.jogl.minigui.view.*;
import de.sofd.viskit.image3D.jogl.model.*;
import de.sofd.viskit.image3D.model.*;

public class SliceViewport extends OrthoViewport
{
    protected SlicePlane plane;

    protected Slider slider;

    protected VolumeObject volumeObject;
    
    protected TextureData sliderBgTexData;
    
    protected TextureData sliderPinTexData;
    
    //protected TextureData transferTexData;

    public SliceViewport( int x, int y, int width, int height, ImageAxis axis, ImagePlaneType planeType,
            VolumeObject volumeObject ) throws IOException
    {
        super( x, y, width, height );

        this.volumeObject = volumeObject;

        setLayout( new BorderLayout( 5, 27, 5, 5 ) );
        int[] margin = getLayout().getMargin();

        plane = SlicePlaneFactory.getInstance().getPlane( margin[ 3 ], margin[ 2 ], width - margin[ 1 ] - margin[ 3 ],
                height - margin[ 0 ] - margin[ 2 ], axis, planeType, volumeObject );

        sliderBgTexData = ResourceLoader.getImageTexData( "minigui.slider.bg" );
        sliderPinTexData = ResourceLoader.getImageTexData( "minigui.slider.pin" );

//        slider = new SliderHorizontal( margin[ 3 ], margin[ 2 ] - sliderBgTexData.getHeight() - 2, plane.getWidth(),
//                sliderBgTexData.getHeight(), sliderPinTexData, 1, plane.getMaxSlices(),
//                (float)plane.getCurrentSlice() + 1, new float[] { 1.0f, 1.0f, 1.0f, 1.0f } );
        
        slider = new SliderVertical( margin[ 3 ] + plane.getWidth(), margin[ 2 ] - sliderBgTexData.getHeight() - 2, sliderBgTexData.getWidth(), 
                height, sliderPinTexData, 1, plane.getMaxSlices(),
                (float)plane.getCurrentSlice() + 1, new float[] { 1.0f, 1.0f, 1.0f, 1.0f } );

        getLayout().add( plane, BorderLayout.CENTER );
        getLayout().add( slider, BorderLayout.EAST );

    }
    
    @Override
    public BorderLayout getLayout()
    {
        return (BorderLayout)layout;
    }

    public SlicePlane getPlane()
    {
        return plane;
    }

    public Slider getSlider()
    {
        return slider;
    }

    public VolumeObject getVolumeObject()
    {
        return volumeObject;
    }

    @Override
    public void glCreate() throws Exception
    {
        plane.glCreate();
        
        Texture sliderBgTex = ResourceLoader.getImageTex( sliderBgTexData, "minigui.slider.bg" );
        Texture sliderPinTex = ResourceLoader.getImageTex( sliderPinTexData, "minigui.slider.pin" );
        
        slider.setTex( sliderBgTex );
        slider.getPin().setTex( sliderPinTex );
        slider.glCreate();
        
        
    }
    
    public synchronized void pack( int x, int y, int width, int height )
    {
        super.pack( x, y, width, height );
        updateSlider();
    }

    public synchronized void resize(    int x,
                                        int y,
                                        int width,
                                        int height )
    {
        super.resize( x, y, width, height );
        updateSlider();
    }

    @Override
    public void show( GL2 gl )
    {
        beginViewport( gl );

        super.show( gl );

        endViewport( gl );
    }

    public void updateSlider()
    {
        slider.setValue( (float)plane.getCurrentSlice() + 1 );
    }


}