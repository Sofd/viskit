package de.sofd.viskit.controllers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import de.sofd.viskit.draw2d.gc.ViskitGC;
import de.sofd.viskit.model.DicomImageListViewModelElement;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;
import de.sofd.viskit.ui.imagelist.event.ImageListViewCellPaintEvent;
import de.sofd.viskit.ui.imagelist.event.ImageListViewCellPaintListener;

/**
 * Controller that references a JImageListView and an "enabled" flag. When
 * enabled, the controller 
 * 
 * @author olaf
 */
public class ImageListViewPrintTextToCellsController {
    
    protected JImageListView controlledImageListView;
    public static final String PROP_CONTROLLEDIMAGELISTVIEW = "controlledImageListView";
    private boolean enabled;
    public static final String PROP_ENABLED = "enabled";

    public ImageListViewPrintTextToCellsController() {
    }

    public ImageListViewPrintTextToCellsController(JImageListView controlledImageListView) {
        setControlledImageListView(controlledImageListView);
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
     * Get the value of controlledImageListView
     *
     * @return the value of controlledImageListView
     */
    public JImageListView getControlledImageListView() {
        return controlledImageListView;
    }

    /**
     * Set the value of controlledImageListView
     *
     * @param controlledImageListView new value of controlledImageListView
     */
    public void setControlledImageListView(JImageListView controlledImageListView) {
        JImageListView oldControlledImageListView = this.controlledImageListView;
        this.controlledImageListView = controlledImageListView;
        if (null != oldControlledImageListView) {
            oldControlledImageListView.removeCellPaintListener(cellPaintListener);
        }
        if (null != controlledImageListView) {
            controlledImageListView.addCellPaintListener(JImageListView.PAINT_ZORDER_LABELS, cellPaintListener);
        }
        propertyChangeSupport.firePropertyChange(PROP_CONTROLLEDIMAGELISTVIEW, oldControlledImageListView, controlledImageListView);
        // TODO: initiate a list repaint?
    }

    private ImageListViewCellPaintListener cellPaintListener = new ImageListViewCellPaintListener() {
        private boolean inProgrammedChange = false;
        @Override
        public void onCellPaint(ImageListViewCellPaintEvent e) {
            if (!isEnabled()) {
                return;
            }
            if (inProgrammedChange) {
                return;
            }
            inProgrammedChange = true;
            String textToPrint;
            ImageListViewCell cell = e.getSource();
            ImageListViewModelElement elt = cell.getDisplayedModelElement();
            if (!(elt instanceof DicomImageListViewModelElement)) {
                textToPrint = getTextToPrint(cell, elt);
            } else {
                DicomImageListViewModelElement delt = (DicomImageListViewModelElement) elt;
                DicomObject dicomImageMetaData = delt.getDicomImageMetaData();
                textToPrint = getTextToPrint(cell, delt, dicomImageMetaData);
            }
            try {
                if (e.getGc().isGraphics2DAvailable() && ! e.getGc().isGlPreferred()) {
                    // paint using Java2D
                    Graphics2D g2d = e.getGc().getGraphics2D();
                    g2d.setColor(Color.white);
                    //g2d.drawString("Hello World", 5, 50);
                    g2d.drawString(textToPrint, 5, 50);
               } else {
                       // paint using OpenGL
                    // TODO: Impl
                    GL2 gl = e.getGc().getGl().getGL2();
                    gl.glPushAttrib(GL2.GL_CURRENT_BIT|GL2.GL_ENABLE_BIT);
                    try {
                        gl.glShadeModel(GL2.GL_FLAT);
//                        Color c = getDrawingObject().getColor();
//                        gl.glColor3f((float) c.getRed() / 255F,
//                                     (float) c.getGreen() / 255F,
//                                     (float) c.getBlue() / 255F);

                    } finally {
                        gl.glPopAttrib();
                    }
                }
            } finally {
                inProgrammedChange = false;
            }
        }
    };

    /**
     * Called to obtain the text to print, if the cell being drawn does NOT display a DICOM ImageListViewModelElement.
     * 
     * @param cell cell to be drawn
     * @param elt == cell.getDisplayedModelElement(). Passed in as an additional parameter for convenience.
     * @return
     */
    protected String getTextToPrint(ImageListViewCell cell, ImageListViewModelElement elt) {
        return "Hello World";
    }
    
    /**
     * Called to obtain the text to print, if the cell being drawn does NOT display a DICOM ImageListViewModelElement.
     * 
     * @param cell cell to be drawn
     * @param elt == cell.getDisplayedModelElement(). Passed in as an additional parameter for convenience.
     * @param dicomImageMetaData == delt.getDicomImageMetaData(). Passed in as an additional parameter for convenience.
     * @return
     */
    protected String getTextToPrint(ImageListViewCell cell, DicomImageListViewModelElement elt, DicomObject dicomImageMetaData) {
        return dicomImageMetaData.getString(Tag.PatientName);
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
