package de.sofd.viskit.ui;

import java.beans.PropertyChangeListener;

import de.sofd.draw2d.viewer.tools.DrawingViewerTool;

/**
 * UI independent interface for the Roi Tool Pane
 * 
 * @author honglinh
 *
 */
public interface RoiToolPane {
    
    
    public Class<? extends DrawingViewerTool> getToolClass();
    
    public void setToolClass(Class<? extends DrawingViewerTool> toolClass);
    
    public void addPropertyChangeListener(PropertyChangeListener listener);
    
    public void removePropertyChangeListener(PropertyChangeListener listener);

}
