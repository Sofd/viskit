package de.sofd.viskit.controllers;

import java.util.Collection;

import de.sofd.viskit.ui.imagelist.JImageListView;

/**
 * Base interface for controllers that work on a mutable set of
 * {@link JImageListView}s.
 * 
 * @author olaf
 */
public interface MultiImageListViewController {
 
    public abstract JImageListView[] getLists();

    public abstract boolean containsList(JImageListView l);

    public abstract boolean addList(JImageListView l);

    public abstract boolean removeList(JImageListView l);

    public abstract void setLists(JImageListView[] lists);

    public abstract void setLists(Collection<JImageListView> lists);

}