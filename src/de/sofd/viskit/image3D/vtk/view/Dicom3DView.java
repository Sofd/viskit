package de.sofd.viskit.image3D.vtk.view;

import vtk.*;

import java.io.*;

import org.apache.log4j.*;


/**
 * This example reads a volume dataset, extracts two isosurfaces that
 * represent the skin and bone, and then displays them.
 */
public class Dicom3DView extends VtkScenePanel {

    static final Logger logger = Logger.getLogger(Dicom3DView.class);
    
    protected vtkMarchingCubes boneExtractor;
    
    public Dicom3DView(vtkImageData imageData) throws IOException {
        super(500, 500);
        
        
        /*double[] range = smooth.GetOutput().GetScalarRange();
        logger.info("scalar range : " + range[0] + " " + range[1]);*/
        
        // An isosurface, or contour value of 500 is known to correspond to the
        // skin of the patient. Once generated, a vtkPolyDataNormals filter is
        // is used to create normals for smooth surface shading during rendering.
        // The triangle stripper is used to create triangle strips from the
        // isosurface these render much faster on some systems.
        /*vtkContourFilter skinExtractor = new vtkContourFilter();
        skinExtractor.SetInput(imageData);
        
        
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
        skin.GetProperty().SetSpecularPower(20);*/
    
        // An isosurface, or contour value of 1150 is known to correspond to the
        // skin of the patient. Once generated, a vtkPolyDataNormals filter is
        // is used to create normals for smooth surface shading during rendering.
        // The triangle stripper is used to create triangle strips from the
        // isosurface these render much faster on some systems.
        boneExtractor = new vtkMarchingCubes();
        //boneExtractor.SetInput(imageData);
        boneExtractor.SetInput(imageData);
        boneExtractor.SetValue(0, 250);
        boneExtractor.ComputeNormalsOn();
        double[] spacing = imageData.GetSpacing();
        
        double minSpacing = 1000;
        if ( spacing[0] < minSpacing ) minSpacing = spacing[0];
        if ( spacing[1] < minSpacing ) minSpacing = spacing[1];
        if ( spacing[2] < minSpacing ) minSpacing = spacing[2];
        logger.info("spacing : " + spacing[0] + ", " + spacing[1] + ", " + spacing[2]);
        
        /*vtkTransformPolyDataFilter transformPolyDataFilter = new vtkTransformPolyDataFilter();
        vtkTransform scaling = new vtkTransform();
        scaling.Scale(spacing[0]/minSpacing, spacing[1]/minSpacing, spacing[2]/minSpacing);
        transformPolyDataFilter.SetTransform(scaling);
        transformPolyDataFilter.SetInput(boneExtractor.GetOutput());*/
        
        /*vtkPolyDataConnectivityFilter connector = new vtkPolyDataConnectivityFilter();
        connector.SetInput(boneExtractor.GetOutput());
        connector.SetExtractionModeToLargestRegion();*/
        
        vtkTriangleFilter triangleFilter = new vtkTriangleFilter();
        triangleFilter.SetInput(boneExtractor.GetOutput());
        //triangleFilter.SetInput(transformPolyDataFilter.GetOutput());
        //triangleFilter.SetInput(connector.GetOutput());
        /*triangleFilter.Update();
        logger.info("== before decimatePro ==");
        logger.info("number of cells " + triangleFilter.GetOutput().GetNumberOfCells());
        logger.info("number of lines " + triangleFilter.GetOutput().GetNumberOfLines());
        logger.info("number of pieces " + triangleFilter.GetOutput().GetNumberOfPieces());
        logger.info("number of points " + triangleFilter.GetOutput().GetNumberOfPoints());
        logger.info("number of polys " + triangleFilter.GetOutput().GetNumberOfPolys());
        logger.info("number of strips " + triangleFilter.GetOutput().GetNumberOfStrips());
        logger.info("number of verts " + triangleFilter.GetOutput().GetNumberOfVerts());*/
        
        /*vtkDecimatePro decimator = new vtkDecimatePro();
        decimator.SetInput(triangleFilter.GetOutput());
        decimator.PreserveTopologyOff();
        decimator.SplittingOn();
        decimator.SetSplitAngle(10);
        decimator.SetMaximumError(0.001);
        decimator.BoundaryVertexDeletionOn();*/
        //decimator.SetTargetReduction(0.1);
        /*decimator.Update();
        
        logger.info("== after decimatePro ==");
        logger.info("number of cells " + decimator.GetOutput().GetNumberOfCells());
        logger.info("number of lines " + decimator.GetOutput().GetNumberOfLines());
        logger.info("number of pieces " + decimator.GetOutput().GetNumberOfPieces());
        logger.info("number of points " + decimator.GetOutput().GetNumberOfPoints());
        logger.info("number of polys " + decimator.GetOutput().GetNumberOfPolys());
        logger.info("number of strips " + decimator.GetOutput().GetNumberOfStrips());
        logger.info("number of verts " + decimator.GetOutput().GetNumberOfVerts());*/
        /*vtkPolyDataNormals boneNormals = new vtkPolyDataNormals();
        boneNormals.SetInput(decimator.GetOutput());
        boneNormals.SetFeatureAngle(60.0);*/
        vtkStripper boneStripper = new vtkStripper();
        //boneStripper.SetInput(decimator.GetOutput());
        boneStripper.SetInput(triangleFilter.GetOutput());
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
        //renderer.AddActor(skin);
        renderer.AddActor(bone);
        renderer.SetActiveCamera(aCamera);
        renderer.ResetCamera();
        aCamera.Dolly(1.5);
        // Set a background color for the renderer and set the size of the
        // render window (expressed in pixels).
        renderer.SetBackground(0, 0, 0);
            
        // Note that when camera movement occurs (as it does in the Dolly()
        // method), the clipping planes often need adjusting. Clipping planes
        // consist of two planes: near and far along the view direction. The
        // near plane clips out objects in front of the plane the far plane
        // clips out objects behind the plane. This way only what is drawn
        // between the planes is actually rendered.
        renderer.ResetCameraClippingRange();
        
        setPanelSize(500, 500);
    }
    
    public void setInput(vtkImageData imageData) {
        boneExtractor.SetInput(imageData);
    }
    
    public void updateContourLevel(int level)
    {
        boneExtractor.SetValue(0, level);
        //boneExtractor.Update();
    }

    

    
}


