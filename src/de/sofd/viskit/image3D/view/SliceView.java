package de.sofd.viskit.image3D.view;

import vtk.*;

import java.io.*;

import org.apache.log4j.*;

/**
 * This example reads a volume dataset, extracts two isosurfaces that
 * represent the skin and bone, and then displays them.
 */
@SuppressWarnings("serial")
public class SliceView extends VtkScenePanel {

    static final Logger logger = Logger.getLogger(Dicom3DView.class);
    
    public SliceView(vtkImageData imageData) throws IOException {
        super(512, 512);
        
        
        vtkContourFilter skinExtractor = new vtkContourFilter();
        skinExtractor.SetInput(imageData);
        
        /*for ( int i=0; i<5; ++i)
            skinExtractor.SetValue(i, i*200);*/
        
        skinExtractor.SetValue(0, 500);
        
        vtkPolyDataNormals skinNormals = new vtkPolyDataNormals();
        skinNormals.SetInput(skinExtractor.GetOutput());
        skinNormals.SetFeatureAngle(60.0);
    //        vtkStripper skinStripper = new vtkStripper();
    //        skinStripper.SetInput(skinNormals.GetOutput());
        vtkPolyDataMapper skinMapper = new vtkPolyDataMapper();
        skinMapper.SetInput(skinNormals.GetOutput());
        skinMapper.ScalarVisibilityOff();
        vtkActor skin = new vtkActor();
        skin.SetMapper(skinMapper);
        skin.GetProperty().SetDiffuseColor(1, .49, .25);
        skin.GetProperty().SetSpecular(.3);
        skin.GetProperty().SetSpecularPower(20);
    
        // An isosurface, or contour value of 1150 is known to correspond to the
        // skin of the patient. Once generated, a vtkPolyDataNormals filter is
        // is used to create normals for smooth surface shading during rendering.
        // The triangle stripper is used to create triangle strips from the
        // isosurface these render much faster on some systems.
        vtkContourFilter boneExtractor = new vtkContourFilter();
        boneExtractor.SetInput(imageData);
        boneExtractor.SetValue(0, 1150);
        vtkPolyDataNormals boneNormals = new vtkPolyDataNormals();
        boneNormals.SetInput(boneExtractor.GetOutput());
        boneNormals.SetFeatureAngle(60.0);
        vtkStripper boneStripper = new vtkStripper();
        boneStripper.SetInput(boneNormals.GetOutput());
        vtkPolyDataMapper boneMapper = new vtkPolyDataMapper();
        boneMapper.SetInput(boneStripper.GetOutput());
        boneMapper.ScalarVisibilityOff();
        vtkActor bone = new vtkActor();
        bone.SetMapper(boneMapper);
        bone.GetProperty().SetDiffuseColor(1, 1, .9412);
    
        // An outline provides context around the data.
        vtkOutlineFilter outlineData = new vtkOutlineFilter();
        outlineData.SetInput(imageData);
        vtkPolyDataMapper mapOutline = new vtkPolyDataMapper();
        mapOutline.SetInput(outlineData.GetOutput());
        vtkActor outline = new vtkActor();
        outline.SetMapper(mapOutline);
        outline.GetProperty().SetColor(0, 0, 0);
    
        // It is convenient to create an initial view of the data. The FocalPoint
        // and Position form a vector direction. Later on (ResetCamera() method)
        // this vector is used to position the camera to look at the data in
        // this direction.
        vtkCamera aCamera = new vtkCamera();
        aCamera.SetViewUp(0, 0, -1);
        aCamera.SetPosition(0, 1, 0);
        aCamera.SetFocalPoint(0, 0, 0);
        aCamera.ComputeViewPlaneNormal();
    
        // Actors are added to the renderer. An initial camera view is created.
        // The Dolly() method moves the camera towards the FocalPoint,
        // thereby enlarging the image.
        renderer.AddActor(skin);
        renderer.AddActor(bone);
        renderer.SetActiveCamera(aCamera);
        renderer.ResetCamera();
        aCamera.Dolly(1.5);
    
        // Set a background color for the renderer and set the size of the
        // render window (expressed in pixels).
        renderer.SetBackground(1, 1, 1);
        setPanelSize(200, 200);
    
        // Note that when camera movement occurs (as it does in the Dolly()
        // method), the clipping planes often need adjusting. Clipping planes
        // consist of two planes: near and far along the view direction. The
        // near plane clips out objects in front of the plane the far plane
        // clips out objects behind the plane. This way only what is drawn
        // between the planes is actually rendered.
        renderer.ResetCameraClippingRange();
    }
}


