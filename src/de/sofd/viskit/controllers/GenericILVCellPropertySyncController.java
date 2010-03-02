package de.sofd.viskit.controllers;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ListSelectionModel;

import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;


public class GenericILVCellPropertySyncController extends ImageListViewCellPropertySyncControllerBase {

    private Set<String> propertiesToSynchronize = new HashSet<String>();
    
    public GenericILVCellPropertySyncController(String[] propertiesToSynchronize) {
        super();
        setPropertiesToSynchronize(propertiesToSynchronize);
    }

    public GenericILVCellPropertySyncController(String[] propertiesToSynchronize, JImageListView... lists) {
        super(lists);
        setPropertiesToSynchronize(propertiesToSynchronize);
    }

    public void setPropertiesToSynchronize(String[] ps) {
        this.propertiesToSynchronize.clear();
        for (String p : ps) {
            this.propertiesToSynchronize.add(p);
        }
    }
    
    public void setPropertiesToSynchronize(Set<String> propertiesToSynchronize) {
        this.propertiesToSynchronize = new HashSet<String>(propertiesToSynchronize);
    }
    
    @Override
    protected void onCellPropertyChange(PropertyChangeEvent e) {
        String sourcePropName = e.getPropertyName();
        if (!propertiesToSynchronize.contains(sourcePropName)) {
            return;
        }
        ImageListViewCell sourceCell = (ImageListViewCell) e.getSource();
        JImageListView sourceList = sourceCell.getOwner();
        ListSelectionModel sm = sourceList.getSelectionModel();
        if (!sm.isSelectionEmpty()) {
            int sourceCellIndex = sourceList.getIndexOf(sourceCell);
            if (sm.isSelectedIndex(sourceCellIndex)) {
                for (JImageListView destList : getLists()) {
                    if (destList != sourceList) {
                        int si = destList.getSelectedIndex();
                        if (si != -1) {
                            ImageListViewCell destCell = destList.getCell(si);
                            getCellBeanInfo().getPropertyDescriptors();   // TODO: continue...
                            
                        }
                    }
                }
            }
        }
    }

    protected BeanInfo getCellBeanInfo() {
        if (cellBeanInfo == null) {
            try {
                cellBeanInfo = Introspector.getBeanInfo(ImageListViewCell.class);
            } catch (IntrospectionException e) {
                throw new IllegalStateException(e.getLocalizedMessage(), e);
            }
        }
        return cellBeanInfo;
    }

    protected static BeanInfo cellBeanInfo;
    
}
