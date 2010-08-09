package de.sofd.viskit.controllers;

import de.sofd.util.Misc;
import de.sofd.viskit.model.DicomImageListViewModelElement;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.dcm4che2.data.Tag;

/**
 *
 * @author olaf
 */
public class ImageListViewRoiInputEventController {

    protected JImageListView controlledImageListView;
    public static final String PROP_CONTROLLEDIMAGELISTVIEW = "controlledImageListView";

    public ImageListViewRoiInputEventController() {
    }

    public ImageListViewRoiInputEventController(JImageListView controlledImageListView) {
        setControlledImageListView(controlledImageListView);
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
            oldControlledImageListView.removeCellMouseListener(mouseHandler);
            oldControlledImageListView.removeCellMouseMotionListener(mouseHandler);
            oldControlledImageListView.removeCellMouseWheelListener(mouseHandler);
        }
        if (null != controlledImageListView) {
            controlledImageListView.addCellMouseListener(mouseHandler);
            controlledImageListView.addCellMouseMotionListener(mouseHandler);
            controlledImageListView.addCellMouseWheelListener(mouseHandler);
        }
        propertyChangeSupport.firePropertyChange(PROP_CONTROLLEDIMAGELISTVIEW, oldControlledImageListView, controlledImageListView);
    }

    private MouseAdapter mouseHandler = new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            dispatchEventToCell(e, true);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            dispatchEventToCell(e, true);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            dispatchEventToCell(e, true);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            dispatchEventToCell(e, true);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            dispatchEventToCell(e, true);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            dispatchEventToCell(e, true);
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            dispatchEventToCell(e, true);
        }

        protected void dispatchEventToCell(MouseEvent evt, boolean refreshCell) {
            ImageListViewCell cell = (ImageListViewCell) evt.getSource();
            if (! cell.getDisplayedModelElement().getInitializationState().equals(ImageListViewModelElement.InitializationState.INITIALIZED)) {
                return;
            }
            if (null != cell.getLatestSize()) {
                // TODO: generalized cell -> image AffineTransformation instead of this zoom/pan vector hackery? But one
                //       would have to update that whenever cell.getLatestSize() changes...

                Point2D imgSize = new Point2D.Double(cell.getScale() * getOriginalImageWidth(cell),
                                                     cell.getScale() * getOriginalImageHeight(cell));
                Dimension cellSize = cell.getLatestSize();
                Point2D imageOffset = new Point2D.Double((cellSize.width + 2 * cell.getCenterOffset().getX() - (int) imgSize.getX()) / 2,
                                                         (cellSize.height + 2 * cell.getCenterOffset().getY() - (int) imgSize.getY()) / 2);

                //Point2D imgSize = getScaledImageSize();
                MouseEvent translatedEvt = Misc.deepCopy(evt);
                translatedEvt.translatePoint((int) -imageOffset.getX(), (int) -imageOffset.getY());
                cell.getRoiDrawingViewer().processInputEvent(translatedEvt);
                if (refreshCell) {
                    controlledImageListView.refreshCell(cell);
                }
            }
        }

        public int getOriginalImageWidth(ImageListViewCell cell) {
            ImageListViewModelElement elt = cell.getDisplayedModelElement();
            if (elt instanceof DicomImageListViewModelElement) {
                // performance optimization for this case -- read the value from DICOM metadata instead of getting the image
                DicomImageListViewModelElement dicomElt = (DicomImageListViewModelElement) elt;
                return dicomElt.getDicomImageMetaData().getInt(Tag.Columns);
            } else if (elt.hasRawImage() && elt.isRawImagePreferable()){
                return elt.getRawImage().getWidth();
            } else {
                return elt.getImage().getWidth();
            }
        }

        public int getOriginalImageHeight(ImageListViewCell cell) {
            ImageListViewModelElement elt = cell.getDisplayedModelElement();
            if (elt instanceof DicomImageListViewModelElement) {
                // performance optimization for this case -- read the value from DICOM metadata instead of getting the image
                DicomImageListViewModelElement dicomElt = (DicomImageListViewModelElement) elt;
                return dicomElt.getDicomImageMetaData().getInt(Tag.Rows);
            } else if (elt.hasRawImage() && elt.isRawImagePreferable()){
                return elt.getRawImage().getHeight();
            } else {
                return elt.getImage().getHeight();
            }
        }

    };

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
