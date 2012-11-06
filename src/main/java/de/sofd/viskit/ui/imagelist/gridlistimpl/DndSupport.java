package de.sofd.viskit.ui.imagelist.gridlistimpl;

import java.awt.datatransfer.Transferable;

import javax.swing.TransferHandler.TransferSupport;

import de.sofd.viskit.ui.imagelist.ImageListView;

/**
 * Ad-Hoc interface definition to be implemented for making DnD work for a
 * JGridImageListView. Must be set on the list by calling
 * {@link JGridImageListView#setDndSupport(DndSupport)}.
 * 
 * TODO: This is Swing/AWT-specific atm. (resembles TransferHandler).
 * Generalize to make it usable for all ImageListView implementations.
 * 
 * @author olaf
 */
public interface DndSupport {
    Transferable dragStart(ImageListView source, int action);
    int getSourceActions(ImageListView source);

    /**
     * The TransferSupport should only be needed for #isDataFlavorSupported,
     * really. May introduce our own abstraction interface instead.
     * 
     * @param source
     * @param ts
     * @return
     */
    boolean canImport(ImageListView source, TransferSupport ts, int index, boolean isInsert);
    boolean importData(ImageListView source, TransferSupport ts, int index, boolean isInsert);
    void exportDone(ImageListView source, Transferable data, int action);
}
