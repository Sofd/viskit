package de.sofd.viskit.ui.imagelist.event;

import de.sofd.viskit.ui.imagelist.JImageListView;
import java.util.EventObject;

/**
 * Base class for events that indicate changes happening to a
 * {@link JImageListView}.
 * 
 * @author Olaf Klischat
 */
public class ImageListViewEvent extends EventObject {

    public ImageListViewEvent(JImageListView source) {
        super(source);
    }
    
    @Override
    public JImageListView getSource() {
        return (JImageListView) super.getSource();
    }
    
}
