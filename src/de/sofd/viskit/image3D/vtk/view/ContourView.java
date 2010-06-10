package de.sofd.viskit.image3D.vtk.view;

import vtk.*;

import java.io.*;
import java.util.ArrayList;

import org.apache.log4j.*;
import org.dcm4che2.data.DicomObject;

import de.sofd.viskit.image3D.model.VolumeBasicConfig;
import de.sofd.viskit.image3D.vtk.util.Dicom2ImageData;

/**
 * @author oliver
 *
 * Ansicht eines Isosurface-Modells.
 * Die Klasse wird mit einer Liste von Dicom-Bildern initialisiert und generiert
 * mit Hilfe des Marching-Cubes-Algorithmus ein Polygonmodell.
 */
@SuppressWarnings("serial")
public class ContourView extends VtkScenePanel {

    static final Logger logger = Logger.getLogger(ContourView.class);
    
    protected vtkDecimatePro decimator;
    protected vtkImageData imageData;
    protected vtkImageGaussianSmooth imageSmoother;
    protected vtkImageResample resampler;
    protected vtkMarchingCubes marchingCubes;
    protected vtkSmoothPolyDataFilter meshSmoother;
    protected vtkStripper stripper;
    protected vtkTriangleFilter triangleFilter;
    
    protected boolean decimating = false;
    protected boolean imageSmoothing = true;
    protected boolean meshSmoothing = false;
    protected boolean resampling = true;
    
    protected int resampleWidth = 150;

    /**
     * Umwandlung von Dicom-Bilder in Polygondaten.
     * Erzeugt Render-Pipeline für Ausgabe.
     * 
     * @param dicomList Liste mit Dicom-Objekten
     * @param basicConfig Basis-Konfiguration der Dicom-Bilder, mit z.B. Höhe und Breite der Bilder
     * @throws IOException
     */
    public ContourView(ArrayList<DicomObject> dicomList, VolumeBasicConfig basicConfig) throws IOException {
        super(500, 500);
        
        long time1 = System.currentTimeMillis();
        
        imageData = Dicom2ImageData.getImageData(dicomList);
        
        imageData.Update();
        imageData.UpdateData();
        
        long time2 = System.currentTimeMillis();
        logger.debug("conversion time : " + (time2-time1) + " ms");
        
        int dim[] =  imageData.GetDimensions();
        logger.debug("image dimension : " + dim[0] + " " + dim[1] + " " + dim[2] + " " + imageData.GetPointData().GetScalars().GetSize());
        
        double[] range = imageData.GetScalarRange();
        logger.debug("range " + range[0] + " " + range[1]);
        
        //Smoothing auf Bilddaten anwenden
        imageSmoother = new vtkImageGaussianSmooth();
                
        //Bilddaten-Quader verkleinern
        resampler = new vtkImageResample();
        resampler.SetDimensionality(3);
        resampler.InterpolateOn();
        
        updateResampler();
                
        //Umwandlung 3D-Daten -> Polygondaten
        marchingCubes = new vtkMarchingCubes();
        //Haut extrahieren (bei CT)
        marchingCubes.SetValue(0, 500);
        marchingCubes.ComputeNormalsOn();
        
        
       /* double[] spacing = imageData.GetSpacing();
        
        double minSpacing = 1000;
        if ( spacing[0] < minSpacing ) minSpacing = spacing[0];
        if ( spacing[1] < minSpacing ) minSpacing = spacing[1];
        if ( spacing[2] < minSpacing ) minSpacing = spacing[2];
        logger.info("spacing : " + spacing[0] + ", " + spacing[1] + ", " + spacing[2]);*/
        
        /*vtkTransformPolyDataFilter transformPolyDataFilter = new vtkTransformPolyDataFilter();
        vtkTransform scaling = new vtkTransform();
        scaling.Scale(spacing[0]/minSpacing, spacing[1]/minSpacing, spacing[2]/minSpacing);
        transformPolyDataFilter.SetTransform(scaling);
        transformPolyDataFilter.SetInput(boneExtractor.GetOutput());*/
        
        /*vtkPolyDataConnectivityFilter connector = new vtkPolyDataConnectivityFilter();
        connector.SetInput(boneExtractor.GetOutput());
        connector.SetExtractionModeToLargestRegion();*/
        
        triangleFilter = new vtkTriangleFilter();

        /*triangleFilter.Update();
        logger.info("== before decimatePro ==");
        logger.info("number of cells " + triangleFilter.GetOutput().GetNumberOfCells());
        logger.info("number of lines " + triangleFilter.GetOutput().GetNumberOfLines());
        logger.info("number of pieces " + triangleFilter.GetOutput().GetNumberOfPieces());
        logger.info("number of points " + triangleFilter.GetOutput().GetNumberOfPoints());
        logger.info("number of polys " + triangleFilter.GetOutput().GetNumberOfPolys());
        logger.info("number of strips " + triangleFilter.GetOutput().GetNumberOfStrips());
        logger.info("number of verts " + triangleFilter.GetOutput().GetNumberOfVerts());*/
        
        decimator = new vtkDecimatePro();
        decimator.SetMaximumError(0.001);
        decimator.SetTargetReduction(0.75);
        decimator.PreserveTopologyOn();
                
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
        
        meshSmoother = new vtkSmoothPolyDataFilter();
        meshSmoother.SetNumberOfIterations(50);
                
        stripper = new vtkStripper();
        
        reconnectFilters();
        
        vtkPolyDataMapper polyDataMapper = new vtkPolyDataMapper();
        polyDataMapper.SetInput(stripper.GetOutput());
        polyDataMapper.ScalarVisibilityOff();
        
        vtkActor actor = new vtkActor();
        actor.SetMapper(polyDataMapper);
        actor.GetProperty().SetDiffuseColor(1, 1, .9412);
    
        vtkCamera camera = new vtkCamera();
        camera.SetViewUp(0, 0, -1);
        camera.SetPosition(0, 1, 0);
        camera.SetFocalPoint(0, 0, 0);
        camera.ComputeViewPlaneNormal();
        camera.Dolly(1.5);
        
        renderer.AddActor(actor);
        renderer.SetActiveCamera(camera);
        renderer.ResetCamera();
        renderer.SetBackground(0, 0, 0);
        renderer.ResetCameraClippingRange();
        
        setPanelSize(500, 500);
    }

    public vtkImageData getImageData() {
        return imageData;
    }

    public int getResampleWidth() {
        return resampleWidth;
    }

    public void reconnectFilters() {
        long t0 = System.currentTimeMillis();
        long t1 = t0;
        
        if (imageSmoothing) {
            imageSmoother.SetInput(imageData);
            imageSmoother.Update();
            
            t1 = System.currentTimeMillis();
            logger.info("imageSmoothing : " + (t1-t0) + " ms.");
            t0 = t1;
            
        }
        
        if (resampling) {
            if (imageSmoothing)
                resampler.SetInput(imageSmoother.GetOutput());
            else
                resampler.SetInput(imageData);
            
            resampler.Update();
            
            t1 = System.currentTimeMillis();
            logger.info("resampler : " + (t1-t0) + " ms.");
            t0 = t1;
        }
        
        if (resampling)
            marchingCubes.SetInput(resampler.GetOutput());
        else if (imageSmoothing)
            marchingCubes.SetInput(imageSmoother.GetOutput());
        else
            marchingCubes.SetInput(imageData);
        
        marchingCubes.Update();
        
        t1 = System.currentTimeMillis();
        logger.info("marchingCubes : " + (t1-t0) + " ms.");
        t0 = t1;
        
        triangleFilter.SetInput(marchingCubes.GetOutput());
        
        triangleFilter.Update();
        
        t1 = System.currentTimeMillis();
        logger.info("triangleFilter : " + (t1-t0) + " ms.");
        t0 = t1;
        
        if (decimating) {
            decimator.SetInput(triangleFilter.GetOutput());
            
            decimator.Update();
            
            t1 = System.currentTimeMillis();
            logger.info("decimator : " + (t1-t0) + " ms.");
            t0 = t1;
        }
        
        if (meshSmoothing) {
            if (decimating)
                meshSmoother.SetInput(decimator.GetOutput());
            else
                meshSmoother.SetInput(triangleFilter.GetOutput());
            
            meshSmoother.Update();
            
            t1 = System.currentTimeMillis();
            logger.info("meshSmoother : " + (t1-t0) + " ms.");
            t0 = t1;
        }
        
        if (meshSmoothing)
            stripper.SetInput(meshSmoother.GetOutput());
        else if (decimating)
            stripper.SetInput(decimator.GetOutput());
        else
            stripper.SetInput(triangleFilter.GetOutput());
        
        stripper.Update();
        
        t1 = System.currentTimeMillis();
        logger.info("stripper : " + (t1-t0) + " ms.");
        t0 = t1;
        
        if (decimating)
            logger.info("number of polys " + decimator.GetOutput().GetNumberOfPolys());
        else
            logger.info("number of polys " + triangleFilter.GetOutput().GetNumberOfPolys());
    }

    public void setDecimating(boolean decimating) {
        this.decimating = decimating;
    }

    public void setImageSmoothing(boolean imageSmoothing) {
        this.imageSmoothing = imageSmoothing;
    }
    
    public void setMeshSmoothing(boolean meshSmoothing) {
        this.meshSmoothing = meshSmoothing;
    }
    
    public void setResampleWidth(int resampleWidth) {
        this.resampleWidth = resampleWidth;
    }
    
    public void setResampling(boolean resampling) {
        this.resampling = resampling;
    }
    
    /**
     * Neuberechnung der Kontur-Stufe (Trennung Objekt-Nichtobjekt).
     * Render-Pipeline wird automatisch aktualisiert.
     * 
     * @param level Konturlevel (bei CT:500=Haut, 1150=Knochen) 
     */
    public void updateContourLevel(int level)
    {
        marchingCubes.SetValue(0, level);
    }
    
    public void updateResampler() {
        int dim[] =  imageData.GetDimensions();
        
        resampler.SetAxisMagnificationFactor(0, Math.min(resampleWidth*1.0/dim[0], 1));
        resampler.SetAxisMagnificationFactor(1, Math.min(resampleWidth*1.0/dim[1], 1));
        resampler.SetAxisMagnificationFactor(2, Math.min(resampleWidth*1.0/dim[2], 1));
    }

    

    
}


