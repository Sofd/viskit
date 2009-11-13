package vtk;

import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.SwingUtilities;

import de.sofd.viskit.image3D.util.*;

@SuppressWarnings("serial")
/**
 * Basic panel for vtk application. Without controllers.
 */
public class vtkPanel extends Canvas {
    protected boolean isWindowSet = false;
    protected boolean isRendering = false;
    
    protected vtkRenderer renderer = new vtkRenderer();
    public vtkRenderer getRenderer() {
        return renderer;
    }

    protected vtkRenderWindow renderWindow = new vtkRenderWindow();

    protected WindowObservable windowSetObservable = new WindowObservable();
    
    protected native int RenderCreate(vtkRenderWindow id0);
    protected native int Lock();
    protected native int UnLock();
    
    public vtkPanel( int width, int height ) {
        super.setSize(width, height);
        
        renderWindow.AddRenderer(renderer);
        renderWindow.SetSize(width, height);
        
        addWindowSetObserver(new WindowSetObserver());
    }

    public void addWindowSetObserver(Observer obs) {
        windowSetObservable.addObserver(new WindowSetObserver());
    }

    public boolean getIsWindowSet() {
        return this.isWindowSet;
    }
    
    /**
     * Logs infos about direct rendering, OpenGL support and capabilities.
     * Must be performed on awt event thread.
     * 
     * @param ps stream to write info log
     */
    public void infoLog( final PrintStream ps ) {

        Runnable updateAComponent = new Runnable() {
            public void run() {
                Lock();
                ps.println("direct rendering = " + (renderWindow.IsDirect() == 1));
                ps.println("opengl supported = "
                        + (renderWindow.SupportsOpenGL() == 1));
                ps.println("report = " + renderWindow.ReportCapabilities());
                UnLock();
            }
        };

        SwingUtilities.invokeLater(updateAComponent);

    }
    
    public void removeWindowSetObserver(Observer obs) {
        windowSetObservable.deleteObserver(obs);
    }
    
    public synchronized void render() {
        if ( isRendering )
            return;
        
        isRendering = true;
        
        if (renderer.VisibleActorCount() == 0) {
            isRendering = false;
            return;
        }
        
        if (renderWindow == null) 
            return;
        
        if (! isWindowSet ) {
            setWindow();
        }
        
        Lock();
        renderWindow.Render();
        UnLock();
        
        isRendering = false;
    }
    
    /**
     * Set size of vtkPanel so Java JPanel and VTK render window coordinates are
     * synchronised. When a call to vtkPanel.setSize() is made before panel is
     * rendered, panel size is not forwarded to VTK and there must be another
     * call to setSize once the panel finished rendering for the first time.
     * 
     * @param xSize  size in X.
     * @param ySize  size in Y.
     */
    public void setPanelSize(final int xSize,
                             final int ySize) {
        
        // Add observer to update size when first rendering is completed
        // Set it before call to 'setSize' 'isWindowSet' so we do not
        // miss notification if rendering is completed in an other thread.
        final Observer windowSetObserver = new Observer() {
            public void update(Observable o, Object arg) {
                setSize(xSize, ySize);
                removeWindowSetObserver(this);
            }
        };
        addWindowSetObserver(windowSetObserver);

        // Set size here, if needed the 'windowSetObserver' will make sure
        // that another call to 'setSize' is mage when vtkPanel is rendered
        // for the first time.
        setSize(xSize, ySize);

        // No need to use 'windowSetObserver' is window is already set.
        if (getIsWindowSet()) {
            removeWindowSetObserver(windowSetObserver);
        }
    }

    protected synchronized void setWindow() {
        RenderCreate(renderWindow);
        
        Lock();
        renderWindow.SetSize(getWidth(), getHeight());
        UnLock();
        
        isWindowSet = true;
        
        // notify observers that we have a renderwindow created
        windowSetObservable.notifyObservers();
    }
    
    /**
     * Takes a snapshot of the panel and stores
     * it as tiff-file.
     * 
     * @param filename tiff file name
     * @param mag magnitution of image
     */
    public void takeSnapshot(String filename, int mag) {

        Lock();

        vtkWindowToImageFilter w2if = new vtkWindowToImageFilter();
        w2if.SetInput(renderWindow);

        w2if.SetMagnification(mag);
        w2if.Update();

        vtkTIFFWriter writer = new vtkTIFFWriter();
        writer.SetInput(w2if.GetOutput());
        writer.SetFileName(filename);
        writer.Write();

        UnLock();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        isWindowSet = false;
        renderWindow.SetForceMakeCurrent();
        isRendering = false;
    }
    
    @Override
    public void paint(Graphics g) {
        this.render();
    }
    
    @Override
    public void removeNotify() {
        isRendering = true;
        super.removeNotify();
    }

    @Override
    public void setSize(int x, int y) {
        super.setSize(x, y);
        
        if (isWindowSet) {
            Lock();
            renderWindow.SetSize(x, y);
            UnLock();
        }
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }
    
    

    

}
