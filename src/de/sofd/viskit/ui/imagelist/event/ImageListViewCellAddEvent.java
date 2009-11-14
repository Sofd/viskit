package de.sofd.viskit.ui.imagelist.event;

import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;

/**
 * Event indicating that a new {@link ImageListViewCell} has just been created
 * inside a {@link JImageListView}, probably because a new model element was
 * added to the list view's model (or the whole model was replaced with another one),
 * and thus a new cell had to be created for the new model element.
 *
 * @author olaf
 */
public class ImageListViewCellAddEvent extends ImageListViewEvent {

    private final ImageListViewCell cell;

    public ImageListViewCellAddEvent(JImageListView source, ImageListViewCell cell) {
        super(source);
        this.cell = cell;
    }

    public ImageListViewCell getCell() {
        return cell;
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
