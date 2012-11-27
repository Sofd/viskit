package de.sofd.viskit.ui.imagelist;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

import de.sofd.draw2d.viewer.DrawingViewer;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.model.LookupTable;

/**
 * Base class for most {@link ImageListViewCell} implementations. Implements
 * all the required property getters and setters as internal, bound properties.
 *
 * @author olaf
 */
public class ImageListViewCellBase implements ImageListViewCell {

    private final ImageListView owner;
    private final ImageListViewModelElement displayedModelElement;
    private int windowLocation;
    private int windowWidth;
    private LookupTable lookupTable;
    private CompositingMode compositingMode = CompositingMode.CM_REPLACE;
    private boolean outputGrayscaleRGBs = false;
    private double scale;
    private Dimension latestSize; // = new Dimension(0, 0);
    private Point2D centerOffset;
    private boolean interactiveWindowingInProgress;
    private DrawingViewer roiDrawingViewer;
    private final Set<String> interactivelyChangingProps = new HashSet<String>();

    protected ImageListViewCellBase(ImageListView owner, ImageListViewModelElement displayedModelElement) {
        this.owner = owner;
        this.displayedModelElement = displayedModelElement;
        windowLocation = 300;
        windowWidth = 600;
        scale = 1.0;
        centerOffset = new Point2D.Double(0, 0);
        interactiveWindowingInProgress = false;
        roiDrawingViewer = owner.getBackend().createRoiDrawingViewer(displayedModelElement, this);
    }

    @Override
    public ImageListView getOwner() {
        return owner;
    }

    @Override
    public ImageListViewModelElement getDisplayedModelElement() {
        return displayedModelElement;
    }


    /**
     * Get the value of windowLocation
     *
     * @return the value of windowLocation
     */
    @Override
    public int getWindowLocation() {
        return windowLocation;
    }

    /**
     * Set the value of windowLocation
     *
     * @param windowLocation new value of windowLocation
     */
    @Override
    public void setWindowLocation(int windowLocation) {
        int oldWindowLocation = this.windowLocation;
        this.windowLocation = windowLocation;
        propertyChangeSupport.firePropertyChange(PROP_WINDOWLOCATION, oldWindowLocation, windowLocation);
    }

    /**
     * Get the value of windowWidth
     *
     * @return the value of windowWidth
     */
    @Override
    public int getWindowWidth() {
        return windowWidth;
    }

    /**
     * Set the value of windowWidth
     *
     * @param windowWidth new value of windowWidth
     */
    @Override
    public void setWindowWidth(int windowWidth) {
        int oldWindowWidth = this.windowWidth;
        this.windowWidth = windowWidth;
        propertyChangeSupport.firePropertyChange(PROP_WINDOWWIDTH, oldWindowWidth, windowWidth);
    }

    /**
     * Get the value of interactiveWindowingInProgress
     *
     * @return the value of interactiveWindowingInProgress
     */
    @Override
    public boolean isInteractiveWindowingInProgress() {
        return interactiveWindowingInProgress;
    }

    /**
     * Set the value of interactiveWindowingInProgress
     *
     * @param interactiveWindowingInProgress new value of interactiveWindowingInProgress
     */
    protected void setInteractiveWindowingInProgress(boolean interactiveWindowingInProgress) {
        boolean oldInteractiveWindowingInProgress = this.interactiveWindowingInProgress;
        this.interactiveWindowingInProgress = interactiveWindowingInProgress;
        propertyChangeSupport.firePropertyChange(PROP_INTERACTIVEWINDOWINGINPROGRESS, oldInteractiveWindowingInProgress, interactiveWindowingInProgress);
    }

    @Override
    public LookupTable getLookupTable() {
        return lookupTable;
    }

    @Override
    public void setLookupTable(LookupTable lut) {
        LookupTable oldValue = this.lookupTable;
        this.lookupTable = lut;
        propertyChangeSupport.firePropertyChange(PROP_LOOKUPTABLE, oldValue, lookupTable);
        refresh();
    }

    @Override
    public CompositingMode getCompositingMode() {
        return compositingMode;
    }
    
    @Override
    public void setCompositingMode(CompositingMode cm) {
        CompositingMode oldValue = this.compositingMode;
        this.compositingMode = cm;
        propertyChangeSupport.firePropertyChange(PROP_COMPOSITINGMODE, oldValue, compositingMode);
        refresh();
    }
    
    @Override
    public boolean isOutputGrayscaleRGBs() {
        return outputGrayscaleRGBs;
    }
    
    @Override
    public void setOutputGrayscaleRGBs(boolean outputGrayscaleRGBs) {
        boolean oldValue = this.outputGrayscaleRGBs;
        this.outputGrayscaleRGBs = outputGrayscaleRGBs;
        propertyChangeSupport.firePropertyChange(PROP_OUTPUTGRAYSCALERGBS, oldValue, outputGrayscaleRGBs);
        refresh();
    }
    
    
    /**
     * Get the value of scale
     *
     * @return the value of scale
     */
    @Override
    public double getScale() {
        return scale;
    }

    /**
     * Set the value of scale
     *
     * @param scale new value of scale
     */
    @Override
    public void setScale(double scale) {
        double oldScale = this.scale;
        this.scale = scale;
        this.roiDrawingViewer.setObjectToDisplayTransform(AffineTransform.getScaleInstance(scale, scale));
        propertyChangeSupport.firePropertyChange(PROP_SCALE, oldScale, scale);
    }

    @Override
    public Dimension getLatestSize() {
        return latestSize;
    }

    @Override
    public void setLatestSize(Dimension size) {
        this.latestSize = size;
    }

    /**
     * Get the value of centerOffset
     *
     * @return the value of centerOffset
     */
    @Override
    public Point2D getCenterOffset() {
        return centerOffset;
    }

    /**
     * Set the value of centerOffset
     *
     * @param centerOffset new value of centerOffset
     */
    @Override
    public void setCenterOffset(Point2D centerOffset) {
        setCenterOffset(centerOffset.getX(), centerOffset.getY());
    }

    @Override
    public void setCenterOffset(double x, double y) {
        Point2D oldCenterOffset = this.centerOffset;
        this.centerOffset = new Point2D.Double(x, y);
        propertyChangeSupport.firePropertyChange(PROP_CENTEROFFSET, oldCenterOffset, centerOffset);
    }

    /**
     * The properties that are currently being changed interactively (by end user interaction,
     * e.g. with the mouse or keyboard). For example, if the user changes the {@link #setS
     *
     *
     *
     * @return the value of interactivelyChangingProps
     */
    @Override
    public String[] getInteractivelyChangingProps() {
        return interactivelyChangingProps.toArray(new String[interactivelyChangingProps.size()]);
    }

    @Override
    public boolean isInteractivelyChangingProp(String propName) {
        return interactivelyChangingProps.contains(propName);
    }

    @Override
    public void runWithPropChangingInteractively(String propName, Runnable runnable) {
        // TODO: check whether propName is one of the PROP_* constants? Ugly and not so easy (props defined in subclasses...)
        boolean wasChanging = isInteractivelyChangingProp(propName);
        if (!wasChanging) {
            interactivelyChangingProps.add(propName);
        }
        try {
            runnable.run();
        } finally {
            if (!wasChanging) {
                interactivelyChangingProps.remove(propName);
            }
        }
    }

    @Override
    public void setInteractively(final String propName, final Object value) {
        // TODO: efficiency?
        runWithPropChangingInteractively(propName, new Runnable() {
            @Override
            public void run() {
                try {
                    BeanInfo bi = Introspector.getBeanInfo(ImageListViewCellBase.this.getClass());
                    for (PropertyDescriptor pd : bi.getPropertyDescriptors()) {
                        if (! pd.getName().equals(propName)) {
                            continue;
                        }
                        pd.getWriteMethod().invoke(ImageListViewCellBase.this, value);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }


    /**
     * Get the value of roiDrawingViewer
     *
     * @return the value of roiDrawingViewer
     */
    @Override
    public DrawingViewer getRoiDrawingViewer() {
        return roiDrawingViewer;
    }

    /**
     * Set the value of roiDrawingViewer
     *
     * @param roiDrawingViewer new value of roiDrawingViewer
     */
    void setRoiDrawingViewer(DrawingViewer roiDrawingViewer) {
        assert(roiDrawingViewer.getDrawing() == displayedModelElement.getRoiDrawing());
        this.roiDrawingViewer = roiDrawingViewer;
    }

    @Override
    public void refresh() {
        getOwner().refreshCell(this);
    }

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

}
