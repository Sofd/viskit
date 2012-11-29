package de.sofd.viskit.controllers;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.DecimalFormat;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;

import de.sofd.viskit.model.DicomImageListViewModelElement;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.ImageListView;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintEvent;
import de.sofd.viskit.ui.imagelist.event.cellpaint.ImageListViewCellPaintListener;

/**
 *
 * @author olaf
 */
public class ImageListViewMouseMeasurementController {

    // TODO: make these parameterizable
    private static final int MOUSE_BUTTON = MouseEvent.BUTTON3;
    private static final int MOUSE_MASK = MouseEvent.BUTTON3_MASK;

    protected ImageListView controlledImageListView;
    public static final String PROP_CONTROLLEDIMAGELISTVIEW = "controlledImageListView";
    private boolean enabled;
    public static final String PROP_ENABLED = "enabled";
    private Color drawingColor = Color.red;
    public static final String PROP_DRAWINGCOLOR = "drawingColor";

    protected ImageListViewCell currentlyMeasuredCell;
    protected Point2D startingPoint, draggedPoint;  // in cell coordinates
    
    private int zOrder;
    public static final String PROP_ZORDER = "zOrder";
    protected static final int DEFAULT_ZORDER = ImageListView.PAINT_ZORDER_LABELS + 100;

    public ImageListViewMouseMeasurementController() {
        this(null, DEFAULT_ZORDER);
    }

    public ImageListViewMouseMeasurementController(ImageListView controlledImageListView) {
        this(controlledImageListView, DEFAULT_ZORDER);
    }

    public ImageListViewMouseMeasurementController(ImageListView controlledImageListView, int zOrder) {
        if (controlledImageListView != null) {
            setControlledImageListView(controlledImageListView);
        }
        setZOrder(zOrder);
    }

    /**
     * Get the value of controlledImageListView
     *
     * @return the value of controlledImageListView
     */
    public ImageListView getControlledImageListView() {
        return controlledImageListView;
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
        if (controlledImageListView != null) {
            controlledImageListView.refreshCells();
        }
    }

    /**
     * Set the value of controlledImageListView
     *
     * @param controlledImageListView new value of controlledImageListView
     */
    public void setControlledImageListView(ImageListView controlledImageListView) {
        ImageListView oldControlledImageListView = this.controlledImageListView;
        this.controlledImageListView = controlledImageListView;
        if (null != oldControlledImageListView) {
            oldControlledImageListView.removeCellMouseListener(mouseHandler);
            oldControlledImageListView.removeCellMouseMotionListener(mouseHandler);
            oldControlledImageListView.removeCellPaintListener(cellPaintHandler);
            controlledImageListView.refreshCells();
        }
        if (null != controlledImageListView) {
            controlledImageListView.addCellMouseListener(getZOrder(), mouseHandler);
            controlledImageListView.addCellMouseMotionListener(getZOrder(), mouseHandler);
            controlledImageListView.addCellPaintListener(getZOrder(), cellPaintHandler);
        }
        propertyChangeSupport.firePropertyChange(PROP_CONTROLLEDIMAGELISTVIEW, oldControlledImageListView, controlledImageListView);
        controlledImageListView.refreshCells();
    }

    public Color getDrawingColor() {
        return drawingColor;
    }

    public void setDrawingColor(Color drawingColor) {
        Color oldValue = this.drawingColor;
        this.drawingColor = drawingColor;
        propertyChangeSupport.firePropertyChange(PROP_DRAWINGCOLOR, oldValue , drawingColor);
    }

    /**
     * Get the value of zOrder
     *
     * @return the value of zOrder
     */
    public int getZOrder() {
        return zOrder;
    }
    
    /**
     * Set the value of zOrder
     *
     * @param enabled new value of zOrder
     */
    public void setZOrder(int zOrder) {
        int oldZOrder = this.zOrder;
        this.zOrder = zOrder;
        propertyChangeSupport.firePropertyChange(PROP_ZORDER, oldZOrder, zOrder);
        if (controlledImageListView != null) {
            controlledImageListView.removeCellMouseListener(mouseHandler);
            controlledImageListView.removeCellMouseMotionListener(mouseHandler);
            controlledImageListView.removeCellPaintListener(cellPaintHandler);
            controlledImageListView.addCellMouseListener(zOrder, mouseHandler);
            controlledImageListView.addCellMouseMotionListener(zOrder, mouseHandler);
            controlledImageListView.addCellPaintListener(zOrder, cellPaintHandler);
            controlledImageListView.refreshCells();
        }
    }

    private MouseAdapter mouseHandler = new MouseAdapter() {

        @Override
        public void mousePressed(MouseEvent e) {
            if (!isEnabled()) {
                return;
            }
            if (controlledImageListView.getModel().getSize() > 0) {
                if (e.isShiftDown() && (e.getButton() == MOUSE_BUTTON || (e.getModifiers() & MOUSE_MASK) != 0)) {
                    currentlyMeasuredCell = (ImageListViewCell) e.getSource();
                    startingPoint = new Point2D.Double(e.getX(), e.getY());
                    draggedPoint = null;
                    currentlyMeasuredCell.refresh();
                    e.consume();
                }
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            currentlyMeasuredCell = null;
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (!isEnabled()) {
                return;
            }
            if (controlledImageListView.getModel().getSize() > 0) {
                if (currentlyMeasuredCell != null &&
                        e.isShiftDown() && (e.getButton() == MOUSE_BUTTON || (e.getModifiers() & MOUSE_MASK) != 0)) {
                    draggedPoint = new Point2D.Double(e.getX(), e.getY());
                    currentlyMeasuredCell.refresh();
                    e.consume();
                }
            }
        }

    };
    
    private ImageListViewCellPaintListener cellPaintHandler = new ImageListViewCellPaintListener() {
        
        @Override
        public void onCellPaint(ImageListViewCellPaintEvent e) {
            if (!isEnabled() || currentlyMeasuredCell != e.getSource() || startingPoint == null || draggedPoint == null) {
                return;
            }
            getControlledImageListView().getBackend().paintMeasurementIntoCell(
                    e,
                    startingPoint,
                    draggedPoint,
                    getDistanceLabel(currentlyMeasuredCell, startingPoint, draggedPoint),
                    drawingColor);
        }

    };

    protected DecimalFormat df = new DecimalFormat();
    {
        // TODO: proper rounding to a fixed number of significant decimal places
        df.setMinimumFractionDigits(3);
        df.setMaximumFractionDigits(3);
        df.setGroupingUsed(false);
        df.setMinimumIntegerDigits(1);
    }
    
    protected String getDistanceLabel(ImageListViewCell cell, Point2D p0, Point2D p1) {
        double dx = (p1.getX() - p0.getX()) / cell.getScale();
        double dy = (p1.getY() - p0.getY()) / cell.getScale();
        String unit = " pixel";
        ImageListViewModelElement elt = cell.getDisplayedModelElement();
        if (elt instanceof DicomImageListViewModelElement) {
            DicomImageListViewModelElement delt = (DicomImageListViewModelElement) elt;
            DicomObject dcm = delt.getDicomImageMetaData();
            float[] rowCol;
            try {
                if (dcm.contains(Tag.PixelSpacing)) {
                    rowCol = dcm.getFloats(Tag.PixelSpacing);
                    if ((rowCol.length != 2) || (rowCol[0] <= 0) || (rowCol[1] <= 0)) {
                        throw new RuntimeException("Illegal PixelSpacing tag in DICOM metadata (2 positive real numbers expected)");
                    }
                } else if (dcm.contains(Tag.ImagerPixelSpacing)) {
                    rowCol = dcm.getFloats(Tag.ImagerPixelSpacing);
                    if ((rowCol.length != 2) || (rowCol[0] <= 0) || (rowCol[1] <= 0)) {
                        throw new RuntimeException("Illegal ImagerPixelSpacing tag in DICOM metadata (2 positive real numbers expected)");
                    }
                } else {
                    throw new RuntimeException("DICOM metadata contained neither a PixelSpacing nor an ImagerPixelSpacing tag");
                }
                dx *= rowCol[1];
                dy *= rowCol[0];
                unit = " mm";
            } catch (RuntimeException e) {
                // ignore, fall back to pixels (s.a.)
            }
        }
        return "" + df.format(Math.sqrt(dx*dx + dy*dy)) + unit;
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
