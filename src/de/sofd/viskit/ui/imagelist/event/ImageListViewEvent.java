package de.sofd.viskit.ui.imagelist.event;

import java.util.EventObject;

import de.sofd.viskit.ui.imagelist.ImageListView;

/**
 * Base class for events that indicate changes happening to a
 * {@link ImageListView}.
 * 
 * @author Olaf Klischat
 */
public class ImageListViewEvent extends EventObject {

    public ImageListViewEvent(ImageListView source) {
        super(source);
    }
    
    @Override
    public ImageListView getSource() {
        return (ImageListView) super.getSource();
    }
    
}
