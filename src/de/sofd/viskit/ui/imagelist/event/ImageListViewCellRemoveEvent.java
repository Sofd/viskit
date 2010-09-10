package de.sofd.viskit.ui.imagelist.event;

import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.ImageListView;

/**
 * Event indicating that a {@link ImageListViewCell} is about to be removed from
 * a {@link ImageListView}, probably because a model element was
 * removed from the list view's model (or the whole model was replaced with another one),
 * and thus the cell belonging to that model element is to be removed.
 *
 * @author olaf
 */
public class ImageListViewCellRemoveEvent extends ImageListViewEvent {

    private final ImageListViewCell cell;

    public ImageListViewCellRemoveEvent(ImageListView source, ImageListViewCell cell) {
        super(source);
        this.cell = cell;
    }

    public ImageListViewCell getCell() {
        return cell;
    }

}
