package vtk;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.*;
import java.util.Observable;
import java.util.Observer;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import de.sofd.viskit.image3D.util.FPSCounter;
import de.sofd.viskit.image3D.vtk.controller.DelayAction;
import de.sofd.viskit.image3D.vtk.util.*;

@SuppressWarnings("serial")
/**
 * Basic panel for vtk application. Without controllers.
 */
public class vtkPanel extends Canvas {
    private Image dbImage;
    private Graphics dbGraphics;

    protected boolean isWindowSet = false;
    protected boolean isRendering = false;
    
    protected vtkRenderer renderer = new vtkRenderer();
    protected vtkRenderWindow renderWindow = new vtkRenderWindow();
    protected WindowObservable windowSetObservable = new WindowObservable();
    
    protected vtkTextActor fpsText;
    
    protected Timer timer;
    protected FPSCounter fpsCounter;
    
    protected native int RenderCreate(vtkRenderWindow id0);
    protected native int Lock();
    protected native int UnLock();
    
    Runnable updateRender = new Runnable() {
        public void run() {
            display();
        }
    };
    
    public vtkPanel( int width, int height ) {
        super.setSize(width, height);
        setBackground(new Color(0, 0, 0));
        
        renderWindow.DoubleBufferOn();
        renderWindow.SwapBuffersOn();
        renderWindow.AddRenderer(renderer);
        renderWindow.SetSize(width, height);
        
        addWindowSetObserver(new WindowSetObserver());
        
        addComponentListener(new ComponentAdapter()
          {
            
            public void componentResized(ComponentEvent event)
            {
              //System.out.println("risized");
              int width = getWidth();
              int height = getHeight();
              setPanelSize(width, height);
              update();
            }
          });
        
        renderer.SetBackground(0.0, 0.0, 0.0);
        
        timer = new Timer(0, new DelayAction(this));
        fpsCounter = new FPSCounter();
        
        fpsText = new vtkTextActor();
        fpsText.SetInput("");
        fpsText.SetDisplayPosition( 10, 10 );
        fpsText.GetTextProperty().SetColor(1, 1, 0);
        renderer.AddActor2D(fpsText);

        
    }

    public void addWindowSetObserver(Observer obs) {
        windowSetObservable.addObserver(obs);
    }
    
    public boolean getIsWindowSet() {
        return this.isWindowSet;
    }
    
    public vtkRenderer getRenderer() {
        return renderer;
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
        //System.out.println("setpanelsize");
        
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

        // No need to use 'windowSetObserver' if window is already set.
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
    
    public void startTimer() {
        timer.start();
    }
    
    /**
     * Takes a snapshot of the panel and stores
     * it as tiff-file.
     * 
     * @param filename tiff file name
     * @param mag magnitution of image
     */
    public synchronized void takeSnapshot(String filename, int mag) {

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
    
    public synchronized void update() {
        SwingUtilities.invokeLater(updateRender);
        fpsCounter.update();
        fpsText.SetInput("Fps : "+fpsCounter.getFps());
    }

    public synchronized void display()
    {
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
        //iren.Modified();
        renderWindow.Render();
        UnLock();
        isRendering = false;
    }
    
    @Override
    public void addNotify() {
        //System.out.println("addnotify");
        super.addNotify();
        isWindowSet = false;
        renderWindow.SetForceMakeCurrent();
        isRendering = false;
    }
    
    @Override
    public void paint(Graphics g) {
        //System.out.println("paint");
        //System.out.println("timer " + timerCounter);
        
        this.update();
        
    }
    
    @Override
    public void removeNotify() {
        //System.out.println("removenotify");
        isRendering = true;
        super.removeNotify();
    }

    @Override
    public void setSize(int x, int y) {
        //System.out.println("setsize");
        super.setSize(x, y);
        
        if (isWindowSet) {
            Lock();
            renderWindow.SetSize(x, y);
            /*iren.SetSize(x, y);
            iren.ConfigureEvent();*/
            UnLock();
            
            //render();
        }
        
        
    }

    @Override
    public void update(Graphics g) {
        
        //Double-Buffer initialisieren
        if (dbImage == null) {
            dbImage = createImage( this.getSize().width, this.getSize().height );
            dbGraphics = dbImage.getGraphics();
        }
        
        //Hintergrund loeschen
        dbGraphics.setColor(getBackground());
        dbGraphics.fillRect( 0, 0, this.getSize().width, this.getSize().height );
          
        //Vordergrund zeichnen
        dbGraphics.setColor(getForeground());
        paint(dbGraphics);
        
        //Offscreen anzeigen
        g.drawImage(dbImage,0,0,this);
    }
    
    
    

    

}
