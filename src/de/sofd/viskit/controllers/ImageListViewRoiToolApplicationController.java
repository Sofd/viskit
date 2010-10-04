package de.sofd.viskit.controllers;

import de.sofd.draw2d.event.DrawingListener;
import de.sofd.draw2d.event.DrawingObjectTagChangeEvent;
import de.sofd.draw2d.viewer.tools.DrawingViewerTool;
import de.sofd.draw2d.viewer.tools.SelectorTool;
import de.sofd.draw2d.viewer.tools.TagNames;
import de.sofd.viskit.ui.RoiToolPanel;
import de.sofd.viskit.ui.imagelist.ImageListView;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.event.ImageListViewCellAddEvent;
import de.sofd.viskit.ui.imagelist.event.ImageListViewCellRemoveEvent;
import de.sofd.viskit.ui.imagelist.event.ImageListViewEvent;
import de.sofd.viskit.ui.imagelist.event.ImageListViewListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Controller that maintains a list of references to {@link JImageListView} objects,
 * a reference to a {@link RoiToolPanel}, and
 * and a boolean "enabled" flag. If the flag is set to true, a DrawingViewer tool class
 * being selected in the panel leads to an instance of that tool being activated on
 * all cells of all the JImageListViews. Also, immediately after a drawing object has
 * been created in any of the cells, a SelectorTool is re-activated on all the cells.
 *
 * @author Sofd GmbH
 */
public class ImageListViewRoiToolApplicationController {

    static final Logger logger = Logger.getLogger(ImageListViewRoiToolApplicationController.class);

    private final List<ImageListView> lists = new ArrayList<ImageListView>();
    private RoiToolPanel roiToolPanel;
    private boolean enabled = true;
    public static final String PROP_ENABLED = "enabled";

    public ImageListViewRoiToolApplicationController() {
    }

    public ImageListViewRoiToolApplicationController(ImageListView... lists) {
        setLists(lists);
    }

    /**
     * Get the value of lists
     *
     * @return the value of lists
     */
    public ImageListView[] getLists() {
        return (ImageListView[]) lists.toArray(new ImageListView[0]);
    }

    /**
     * Set the value of lists
     *
     * @param lists new value of lists
     */
    public void setLists(ImageListView[] lists) {
        disconnectUiElements();
        this.lists.clear();
        for (ImageListView lv : lists) {
            this.lists.add(lv);
        }
        connectUiElements();
    }

    /**
     * Get the value of lists at specified index
     *
     * @param index
     * @return the value of lists at specified index
     */
    public ImageListView getLists(int index) {
        return this.lists.get(index);
    }

    /**
     * Get the value of enabled
     *
     * @return the value of enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set the value of enabled
     *
     * @param enabled new value of enabled
     */
    public void setEnabled(boolean enabled) {
        boolean oldEnabled = this.enabled;
        this.enabled = enabled;
        propertyChangeSupport.firePropertyChange(PROP_ENABLED, oldEnabled, enabled);
    }

    /**
     * Get the value of roiToolPanel
     *
     * @return the value of roiToolPanel
     */
    public RoiToolPanel getRoiToolPanel() {
        return roiToolPanel;
    }

    /**
     * Set the value of roiToolPanel
     *
     * @param roiToolPanel new value of roiToolPanel
     */
    public void setRoiToolPanel(RoiToolPanel roiToolPanel) {
        disconnectUiElements();
        this.roiToolPanel = roiToolPanel;
        connectUiElements();
    }

    protected void connectUiElements() {
        if (this.roiToolPanel == null || this.lists.isEmpty()) {
            return;
        }
        this.roiToolPanel.addPropertyChangeListener(roiToolChangeHandler);
        for (ImageListView lv : this.lists) {
            for (int i = 0; i < lv.getLength(); i++) {
                lv.getElementAt(i).getRoiDrawing().addDrawingListener(drawingEventHandler);
            }
            lv.addPropertyChangeListener(listModelChangeHandler);
            lv.addImageListViewListener(listCellAddRemoveHandler);
        }
    }

    protected void disconnectUiElements() {
        if (this.roiToolPanel == null || this.lists.isEmpty()) {
            return;
        }
        this.roiToolPanel.removePropertyChangeListener(roiToolChangeHandler);
        for (ImageListView lv : this.lists) {
            for (int i = 0; i < lv.getLength(); i++) {
                lv.getElementAt(i).getRoiDrawing().removeDrawingListener(drawingEventHandler);
            }
            lv.removePropertyChangeListener(listModelChangeHandler);
            lv.removeImageListViewListener(listCellAddRemoveHandler);
        }
    }

    private PropertyChangeListener roiToolChangeHandler = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (! RoiToolPanel.PROP_TOOLCLASS.equals(evt.getPropertyName())) {
                return;
            }
            activateToolClass(roiToolPanel.getToolClass());
        }
    };

    /**
     * Event handler that's called if anything changes in any of the ROI drawings
     * in any of our lists.
     */
    private DrawingListener drawingEventHandler = new DrawingListener() {
        @Override
        public void onDrawingEvent(EventObject e) {
            // if any DrawingObject in any ROI Drawing has been completed,
            // activate the SelectorTool again
            if (e instanceof DrawingObjectTagChangeEvent) {
                DrawingObjectTagChangeEvent tce = (DrawingObjectTagChangeEvent) e;
                if (tce.isAfterChange() && tce.getTagName() == TagNames.TN_CREATION_COMPLETED) {
                    activateToolClass(SelectorTool.class);
                }
            }
        }
    };

    private ImageListViewListener listCellAddRemoveHandler = new ImageListViewListener() {
        @Override
        public void onImageListViewEvent(ImageListViewEvent e) {
            if (null == roiToolPanel) {
                // no ROI tool panel => we can assume we haven't connected our GUI elements. No action required.
                return;
            }
            //disconnectUiElements();
            //connectUiElements();
            // same effect more efficiently:
            ImageListView list = e.getSource();
            if (e instanceof ImageListViewCellAddEvent) {
                ImageListViewCellAddEvent cae = (ImageListViewCellAddEvent) e;
                ImageListViewCell cell = cae.getCell();
                cell.getDisplayedModelElement().getRoiDrawing().addDrawingListener(drawingEventHandler);
                activateToolClassOnCell(roiToolPanel.getToolClass(), cell);
            } else if (e instanceof ImageListViewCellRemoveEvent) {
                ImageListViewCellRemoveEvent cre = (ImageListViewCellRemoveEvent) e;
                ImageListViewCell cell = cre.getCell();
                cell.getDisplayedModelElement().getRoiDrawing().removeDrawingListener(drawingEventHandler);
                activateToolClassOnCell(null, cell);
            }
        }
    };

    private PropertyChangeListener listModelChangeHandler = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (! ImageListView.PROP_MODEL.equals(evt.getPropertyName())) {
                return;
            }
            disconnectUiElements();
            connectUiElements();
        }
    };


    /**
     * Activate a DrawingViewerTool on all ROI DrawingViewers of all lists.
     * Because each DrawingViewer needs its own instance of the DrawingViewerTool, the
     * class of the tool is passed instead of an instance.
     *
     * @param toolClass
     */
    public void activateToolClass(Class<? extends DrawingViewerTool> toolClass) {
        if (!isEnabled()) { return; }
        logger.debug("activating tool on all roiDrawingViewers: " + toolClass);
        for (ImageListView lv : lists) {
            for (int i = 0; i < lv.getLength(); i++) {
                activateToolClassOnCell(toolClass, lv.getCell(i));
            }
        }
        roiToolPanel.setToolClass(toolClass);
    }

    protected static void activateToolClassOnCell(Class<? extends DrawingViewerTool> toolClass, ImageListViewCell cell) {
        try {
            if (toolClass != null) {
                DrawingViewerTool tool = toolClass.newInstance();
                cell.getRoiDrawingViewer().activateTool(tool);
            } else {
                cell.getRoiDrawingViewer().deactivateCurrentTool();
            }
        } catch (InstantiationException ex) {
            throw new IllegalStateException("couldn't activate tool class " + toolClass + " on cell " + cell, ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("couldn't activate tool class " + toolClass + " on cell " + cell, ex);
        }
    }


    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

}
