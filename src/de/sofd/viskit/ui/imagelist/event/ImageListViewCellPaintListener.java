package de.sofd.viskit.ui.imagelist.event;

import de.sofd.viskit.ui.imagelist.JImageListView;

/**
 * Listener for receiving {@link ImageListViewCellPaintEvent}s from a
 * {@link JImageListView}.
 * 
 * @author olaf
 */
public interface ImageListViewCellPaintListener {
    void onCellPaint(ImageListViewCellPaintEvent e);
}
