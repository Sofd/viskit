package de.sofd.viskit.ui.imagelist.event;

import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;

/**
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

}
