package de.sofd.viskit.controllers;

import java.util.Collection;

import de.sofd.viskit.ui.imagelist.ImageListView;

/**
 * Base interface for controllers that work on a mutable set of
 * {@link ImageListView}s.
 * 
 * @author olaf
 */
public interface MultiImageListViewController {
 
    public abstract ImageListView[] getLists();

    public abstract boolean containsList(ImageListView l);

    public abstract boolean addList(ImageListView l);

    public abstract boolean removeList(ImageListView l);

    public abstract void setLists(ImageListView[] lists);

    public abstract void setLists(Collection<ImageListView> lists);

}