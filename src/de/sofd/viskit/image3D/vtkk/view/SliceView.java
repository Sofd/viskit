package de.sofd.viskit.image3D.vtkk.view;

import vtk.*;

import java.io.*;

import org.apache.log4j.*;

import de.sofd.viskit.image3D.vtkk.model.ImagePlane;

/**
 * This example reads a volume dataset, extracts two isosurfaces that
 * represent the skin and bone, and then displays them.
 */
@SuppressWarnings("serial")
public class SliceView extends vtkPanel {

    static final Logger logger = Logger.getLogger(Dicom3DView.class);
    
    protected vtkTexture tex[];
    protected int currentSliceNr;
    
    protected vtkActor planeActor;
    
    protected vtkTextActor sliceText;
    
    public SliceView(vtkImageData imageData, ImagePlane imagePlane) throws IOException {
        //super(256, 256);
        super(imageData.GetDimensions()[0], imageData.GetDimensions()[1]);
        
        int dim[] =  imageData.GetDimensions();
        
        vtkLookupTable colorMap = new vtkLookupTable();
        colorMap.SetNumberOfColors(256);
        colorMap.SetTableRange(0, 255);
        for ( int i = 0; i < 256; ++i )
        {
            double v = i/255.0;
            colorMap.SetTableValue(i, v, v, v, 1.0);
        }
        
        int xSize=0;
        int ySize=0;
        int zSize=0;
        
        switch (imagePlane)
        {
            case PLANE_TRANSVERSE :
                xSize = dim[0];
                ySize = dim[1];
                zSize = dim[2];
                break;
            case PLANE_CORONAL :
                xSize = dim[0];
                ySize = dim[2];
                zSize = dim[1];
                break;
            case PLANE_SAGITTAL :
                xSize = dim[2];
                ySize = dim[0];
                zSize = dim[1];
                break;    
        }
        
        tex = new vtkTexture[zSize];
        
        for ( int i = 0; i < zSize; ++i )
        {
            vtkExtractVOI voi = new vtkExtractVOI();
            voi.SetInput(imageData);
            switch (imagePlane)
            {
                case PLANE_TRANSVERSE :
                    voi.SetVOI(0, dim[0]-1, 0, dim[1]-1, i, i);
                    break;
                case PLANE_CORONAL :
                    voi.SetVOI(0, dim[0]-1, i, i, 0, dim[2]-1);
                    break;
                case PLANE_SAGITTAL :
                    voi.SetVOI(i, i, 0, dim[1]-1, 0, dim[2]-1);
                    break;    
            }
            
            tex[i] = new vtkTexture();
            tex[i].SetInput(voi.GetOutput());
            tex[i].InterpolateOn();
            tex[i].SetLookupTable(colorMap);
        }
        
        currentSliceNr = 0;
        
        vtkPlaneSource plane = new vtkPlaneSource();
        plane.SetXResolution(1);
        plane.SetYResolution(1);
//        plane.SetOrigin(-0.5, -0.5, 0);
//        plane.SetPoint1(254.5, -0.5, 0);
//        plane.SetPoint2(-0.5, 254.5, 0);
        plane.SetOrigin(0.0, 0.0, 0);
        plane.SetPoint1(xSize-1, 0.0, 0);
        plane.SetPoint2(0.0, ySize-1, 0);
        
        
        vtkPolyDataMapper mapper = new vtkPolyDataMapper();
        mapper.SetInput(plane.GetOutput());
        
        planeActor = new vtkActor();
        planeActor.SetMapper(mapper);
        planeActor.SetTexture(tex[currentSliceNr]);
        //actor.GetProperty().SetRepresentationToWireframe();
        
        renderer.AddActor(planeActor);
        
        vtkCamera aCamera = new vtkCamera();
        aCamera.SetViewUp(0, 1, 0);
        aCamera.SetPosition(0, 0, 10);
        aCamera.SetFocalPoint(0, 0, 0);
        aCamera.ParallelProjectionOn();
        renderer.SetActiveCamera(aCamera);
        renderer.ResetCamera();
        renderer.ResetCameraClippingRange();
        
        renderer.GetActiveCamera().SetParallelScale(dim[1]/2);
       
        // Set a background color for the renderer and set the size of the
        // render window (expressed in pixels).
        renderer.SetBackground(0, 0, 0);
        setPanelSize(xSize, ySize);    
        // Note that when camera movement occurs (as it does in the Dolly()
        // method), the clipping planes often need adjusting. Clipping planes
        // consist of two planes: near and far along the view direction. The
        // near plane clips out objects in front of the plane the far plane
        // clips out objects behind the plane. This way only what is drawn
        // between the planes is actually rendered.
        //renderer.ResetCameraClippingRange();
        
        sliceText = new vtkTextActor();
        sliceText.SetInput("Image " + currentSliceNr + "/" + tex.length);
        sliceText.SetDisplayPosition( 10, 30 );
        sliceText.GetTextProperty().SetColor(1, 1, 0);
        renderer.AddActor2D(sliceText);
    }

    public int getCurrentSliceNr() {
        return currentSliceNr;
    }
    
    public void showSlice(int sliceNr) {
        currentSliceNr = sliceNr;
        planeActor.SetTexture(tex[currentSliceNr]);
        sliceText.SetInput("Image " + currentSliceNr + "/" + tex.length);
        //update();
    }
    
    

}


