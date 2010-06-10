package de.sofd.viskit.image3D.vtk.view;

import org.apache.log4j.*;

import de.sofd.viskit.image3D.vtk.controller.DefaultVtkSceneInputController;
import vtk.*;

@SuppressWarnings("serial")
/**
 * Vtk panel with camera, light and controller ( mouse and keyboard ).
 */
public class VtkScenePanel extends vtkPanel {
    static final Logger logger = Logger.getLogger(VtkScenePanel.class);
    
    protected boolean isLightSet = false;
    protected boolean isLightFollowingCamera = true;
    
    protected vtkCamera camera = null;
    protected vtkLight light = null;

    public VtkScenePanel( int width, int height )
    {
        super(width, height);
        
        light = new vtkLight();
        
        DefaultVtkSceneInputController controller =    getNewController();
        
        this.addMouseListener(controller);
        this.addMouseMotionListener(controller);
        this.addKeyListener(controller);
    }

    public vtkLight getLight() {
        return light;
    }
    
    protected DefaultVtkSceneInputController getNewController() {
        return new DefaultVtkSceneInputController(renderer, renderWindow, this);
    }

    public void pickActor(int x, int y) {

        vtkPropPicker picker = new vtkPropPicker();

        Lock();
        picker.PickProp(x, renderWindow.GetSize()[1] - y, renderer);
        UnLock();

        //actor == geometrical object
        if (picker.GetActor() != null)
            logger.debug(picker.GetActor().GetClassName());
    }
    
    public void resetCamera() {
        Lock();
        renderer.ResetCamera();
        UnLock();
    }

    public void resetCameraClippingRange() {
        Lock();
        renderer.ResetCameraClippingRange();
        UnLock();
    }
    
    public void rotateCamera(int x, int y, int lastX, int lastY) {
        if ( camera == null )
            return;
        
        camera.Azimuth(lastX - x);
        camera.Elevation(y - lastY);
        camera.OrthogonalizeViewUp();
        
        resetCameraClippingRange();
        
        if (this.isLightFollowingCamera) {
            updateLight();
        }
        
    }

    protected synchronized void setLight() {
        renderer.AddLight(light);
        updateLight();
        isLightSet = true;
    }
    
    public void setLightFollowsCameraOff() {
        this.isLightFollowingCamera = false;
    }

    public void setLightFollowsCameraOn() {
        this.isLightFollowingCamera = true;
    }
    
    @Override
    protected synchronized void setWindow()
    {
        if ( renderWindow == null )
            return;
        
        // set the window id and the active camera
        camera = renderer.GetActiveCamera();
        
        if ( !isLightSet ) {
            setLight();
        }
        
        RenderCreate(renderWindow);
        
        Lock();
        renderWindow.SetSize(getWidth(), getHeight());
        UnLock();
        
        isWindowSet = true;
        // notify observers that we have a renderwindow created
        windowSetObservable.notifyObservers();
    }
    
    public void translateCamera(int x, int y, int lastX, int lastY) {
        if ( camera == null )
            return;
        
        // get the current focal point and position
        double focal[] = camera.GetFocalPoint();
        double pos[] = camera.GetPosition();

        // calculate the focal depth since we'll be using it a lot
        renderer.SetWorldPoint(focal[0], focal[1], focal[2], 1.0);
        renderer.WorldToDisplay();
        double focalDepth = renderer.GetDisplayPoint()[2];

        double displayPoint[] = new double[3];
        displayPoint[0] = renderWindow.GetSize()[0] / 2.0 + (x - lastX);
        displayPoint[1] = renderWindow.GetSize()[1] / 2.0 - (y - lastY);
        displayPoint[2] = focalDepth;
        renderer.SetDisplayPoint(displayPoint);
        
        renderer.DisplayToWorld();
        double worldPoint[] = renderer.GetWorldPoint();
        
        if (worldPoint[3] != 0.0) {
            worldPoint[0] = worldPoint[0] / worldPoint[3];
            worldPoint[1] = worldPoint[1] / worldPoint[3];
            worldPoint[2] = worldPoint[2] / worldPoint[3];
        }

        /*
         * Compute a translation vector, moving everything 1/2 the distance
         * to the cursor. (Arbitrary scale factor)
         */
        camera.SetFocalPoint((focal[0] - worldPoint[0]) / 2.0 + focal[0],
                (focal[1] - worldPoint[1]) / 2.0 + focal[1],
                (focal[2] - worldPoint[2]) / 2.0 + focal[2]);
        camera.SetPosition((focal[0] - worldPoint[0]) / 2.0 + pos[0],
                (focal[1] - worldPoint[1]) / 2.0 + pos[1],
                (focal[2] - worldPoint[2]) / 2.0 + pos[2]);
        
        resetCameraClippingRange();
    }
    
    public void updateLight() {
        light.SetPosition(camera.GetPosition());
        light.SetFocalPoint(camera.GetFocalPoint());
    }
    
    public void zoomCam(int x, int y, int lastX, int lastY) {
        if ( camera == null )
            return;
        
        double zoomFactor = Math.pow(1.02, (y - lastY));
        
        if (camera.GetParallelProjection() == 1) {
            camera.SetParallelScale(camera.GetParallelScale() / zoomFactor);
        } else {
            camera.Dolly(zoomFactor);
            resetCameraClippingRange();
        }
        
    }

    

    

    

    

}
