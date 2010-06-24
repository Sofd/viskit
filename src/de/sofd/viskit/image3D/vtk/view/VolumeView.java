package de.sofd.viskit.image3D.vtk.view;

import vtk.*;

import java.io.*;
import java.util.ArrayList;

import org.apache.log4j.*;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import de.sofd.viskit.image3D.vtk.controller.*;
import de.sofd.viskit.image3D.vtk.util.Dicom2ImageData;

/**
 * @author oliver
 *
 * Volume-Rendering
 * Die Klasse wird mit einer Liste von Dicom-Bildern initialisiert.
 */
@SuppressWarnings("serial")
public class VolumeView extends VtkScenePanel {

    static final Logger logger = Logger.getLogger(VolumeView.class);
    
    protected vtkImageData imageData;
    
    protected int windowingCenter;
    protected int windowingWidth;
    protected double sampleDist = 100.0;
    protected double sampleDistFinal = 100.0;

    protected vtkColorTransferFunction colTrans;
    protected vtkPiecewiseFunction opTrans;
    protected vtkVolumeTextureMapper3D volumeMapper;
    
    protected vtkFixedPointVolumeRayCastMapper rayCaster;
    
    protected boolean imageSmoothing=true;
    protected boolean finalRendering=false;
    
    protected vtkImageGaussianSmooth imageSmoother;

    protected vtkVolume volume;

    public VolumeView(ArrayList<DicomObject> dicomList) throws IOException {
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
                
        windowingCenter = (int)dicomList.get(0).getDouble(Tag.WindowCenter);
        windowingWidth = (int)dicomList.get(0).getDouble(Tag.WindowWidth);
        
        logger.debug("windowingCenter " + windowingCenter);
        logger.debug("windowingWidth " + windowingWidth);
                
        //map scalar to opacity
        opTrans = new vtkPiecewiseFunction();
                
        //map scalar to color
        colTrans = new vtkColorTransferFunction();
        
        updateTransfer();
        
        vtkVolumeProperty volProp = new vtkVolumeProperty();
        volProp.SetScalarOpacity(opTrans);
        volProp.SetColor(colTrans);
        volProp.SetInterpolationTypeToLinear();
                
        volumeMapper = new vtkVolumeTextureMapper3D();
        volumeMapper.SetBlendModeToComposite();
        volumeMapper.SetPreferredMethodToFragmentProgram();
        
        //volumeMapper.SetMaximumNumberOfPlanes(50);
        rayCaster = new vtkFixedPointVolumeRayCastMapper();
        rayCaster.SetBlendModeToComposite();
                
        updateSampleDist();
        
        volume = new vtkVolume();
        volume.SetProperty(volProp);
        
        reconnectFilters();
        updateMapper();
       
        vtkCamera camera = new vtkCamera();
        camera.SetViewUp(0, 0, -1);
        camera.SetPosition(0, 1, 0);
        camera.SetFocalPoint(0, 0, 0);
        camera.ComputeViewPlaneNormal();
        camera.Dolly(1.5);
        
        renderer.AddVolume(volume);
        renderer.SetActiveCamera(camera);
        renderer.ResetCamera();
        renderer.SetBackground(0, 0, 0);
        renderer.ResetCameraClippingRange();
        
        setPanelSize(500, 500);
    }
    
    public vtkImageData getImageData() {
        return imageData;
    }

    @Override
    protected DefaultVtkSceneInputController getNewController() {
        return new VolumeInputController(renderer, renderWindow, this);
    }
    
    public double getSampleDist() {
        return sampleDist;
    }
    
    public double getSampleDistFinal() {
        return sampleDistFinal;
    }

    public int getWindowingCenter() {
        return windowingCenter;
    }

    public int getWindowingWidth() {
        return windowingWidth;
    }

    public boolean isFinalRendering() {
        return finalRendering;
    }

    public boolean isImageSmoothing() {
        return imageSmoothing;
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
            
            rayCaster.SetInput(imageSmoother.GetOutput());
            volumeMapper.SetInput(imageSmoother.GetOutput());
        } else {
            volumeMapper.SetInput(imageData);
            rayCaster.SetInput(imageData);
        }
    }

    public void setFinalRendering(boolean finalRendering) {
        this.finalRendering = finalRendering;
    }

    public void setImageSmoothing(boolean imageSmoothing) {
        this.imageSmoothing = imageSmoothing;
    }

    public void setSampleDist(double sampleDist) {
        this.sampleDist = sampleDist;
    }
    
    public void setSampleDistFinal(double sampleDistFinal) {
        this.sampleDistFinal = sampleDistFinal;
    }
    
    public void setWindowingCenter(int windowingCenter) {
        this.windowingCenter = windowingCenter;
    }
            
    public void setWindowingWidth(int windowingWidth) {
        this.windowingWidth = windowingWidth;
    }
    
    public void updateMapper() {
        if (finalRendering)
            volume.SetMapper(rayCaster);
        else
            volume.SetMapper(volumeMapper);
        
    }

    public void updateSampleDist() {
        volumeMapper.SetSampleDistance(sampleDist/100.0);    
    }
    
    public void updateSampleDistFinal() {
        rayCaster.SetSampleDistance(sampleDistFinal/100.0);    
    }

    public void updateTransfer() {
        double[] range = imageData.GetScalarRange();
        
        opTrans.RemoveAllPoints();
        opTrans.AddPoint(Math.max(windowingCenter - windowingWidth/2, range[0]), 0.0);
        opTrans.AddPoint(Math.min(windowingCenter + windowingWidth/2, range[1]), 1.0);
        
        colTrans.RemoveAllPoints();
        colTrans.AddRGBPoint(Math.max(windowingCenter - windowingWidth/2, range[0]), 0.0, 0.0, 0.0);
        colTrans.AddRGBPoint(Math.min(windowingCenter + windowingWidth/2, range[1]), 1.0, 1.0, 1.0);
        
    }


    

    

    
}


