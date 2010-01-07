package de.sofd.viskit.ui.imagelist.cellviewers;

import de.sofd.viskit.model.DicomImageListViewModelElement;
import de.sofd.viskit.model.ImageListViewModelElement;
import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import javax.swing.JPanel;
import org.dcm4che2.data.Tag;

/**
 * Base class for for ImageListView cell viewers (components displaying a {@link ImageListViewCell}).
 *
 * @author olaf
 */
public abstract class BaseImageListViewCellViewer extends JPanel {

    protected final ImageListViewCell displayedCell;

    public BaseImageListViewCellViewer(ImageListViewCell cell) {
        this.displayedCell = cell;
    }

    public ImageListViewCell getDisplayedCell() {
        return displayedCell;
    }

    public int getOriginalImageWidth() {
        ImageListViewModelElement elt = displayedCell.getDisplayedModelElement();
        if (elt instanceof DicomImageListViewModelElement) {
            // performance optimization for this case -- read the value from DICOM metadata instead of getting the image
            DicomImageListViewModelElement dicomElt = (DicomImageListViewModelElement) elt;
            return dicomElt.getDicomImageMetaData().getInt(Tag.Columns);
        } else {
            return elt.getImage().getWidth();
        }
    }

    public int getOriginalImageHeight() {
        ImageListViewModelElement elt = displayedCell.getDisplayedModelElement();
        if (elt instanceof DicomImageListViewModelElement) {
            // performance optimization for this case -- read the value from DICOM metadata instead of getting the image
            DicomImageListViewModelElement dicomElt = (DicomImageListViewModelElement) elt;
            return dicomElt.getDicomImageMetaData().getInt(Tag.Rows);
        } else {
            return elt.getImage().getHeight();
        }
    }

    public double getZoomFactor() {
        return displayedCell.getScale();
        // TODO: when it changes, we'd want to recomputeImageOrigin()...
    }

    protected AffineTransform getDicomToUiTransform() {
        double z = getZoomFactor();
        return AffineTransform.getScaleInstance(z, z);
    }

    public Point2D getScaledImageSize() {
        return getDicomToUiTransform().transform(new Point2D.Double(getOriginalImageWidth(), getOriginalImageHeight()), null);
    }

    @Override
    public Dimension getPreferredSize() {
        Point2D scaledImageSize = getScaledImageSize();
        Insets insets = getInsets();  // insets imposed by our getBorder()
        return new Dimension((int)(scaledImageSize.getX()+insets.left+insets.right),
                             (int)(scaledImageSize.getY()+insets.top+insets.bottom));
    }

    public Point2D getImageOffset() {
        Point2D imgSize = getScaledImageSize();
        Dimension latestSize = displayedCell.getLatestSize();
        return new Point2D.Double((latestSize.width + 2 * displayedCell.getCenterOffset().getX() - (int) imgSize.getX()) / 2,
                                  (latestSize.height + 2 * displayedCell.getCenterOffset().getY() - (int) imgSize.getY()) / 2);
    }

}
