package de.sofd.viskit.ui.imagelist;

import de.sofd.viskit.model.ImageListViewModelElement;
import java.awt.geom.Point2D;

/**
 *
 * @author olaf
 */
public class DefaultImageListViewCell extends ImageListViewCellBase {

    public DefaultImageListViewCell(ImageListView owner, ImageListViewModelElement displayedModelElement) {
        super(owner, displayedModelElement);
    }

    @Override
    public void setScale(double scale) {
        super.setScale(scale);
        getOwner().refreshCellForElement(getDisplayedModelElement());
    }

    @Override
    public void setCenterOffset(Point2D centerOffset) {
        super.setCenterOffset(centerOffset);
        getOwner().refreshCellForElement(getDisplayedModelElement());
    }

    @Override
    public void setWindowLocation(int windowLocation) {
        super.setWindowLocation(windowLocation);
        getOwner().refreshCellForElement(getDisplayedModelElement());
    }

    @Override
    public void setWindowWidth(int windowWidth) {
        super.setWindowWidth(windowWidth);
        getOwner().refreshCellForElement(getDisplayedModelElement());
    }

}
