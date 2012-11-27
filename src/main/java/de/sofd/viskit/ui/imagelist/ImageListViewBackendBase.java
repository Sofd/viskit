package de.sofd.viskit.ui.imagelist;


/**
 * Abstract base class for most ImageListViewBackends. Just catches the owning
 * list (passed into {@link #initialize(ImageListView)}) and stores it into
 * {@link #getOwner()}.
 * 
 * @author olaf
 */
public abstract class ImageListViewBackendBase implements ImageListViewBackend {

    private /*final*/ ImageListView owner;
    
    @Override
    public void initialize(ImageListView owner) {
        this.owner = owner;
    }

    public ImageListView getOwner() {
        return owner;
    }

}
