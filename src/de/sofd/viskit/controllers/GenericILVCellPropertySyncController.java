package de.sofd.viskit.controllers;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.ListSelectionModel;

import de.sofd.viskit.ui.imagelist.ImageListViewCell;
import de.sofd.viskit.ui.imagelist.JImageListView;

/**
 * Generic cell property sync controller that can synchronize any set bean
 * properties of cells ({@link ImageListViewCell}s) between some set of
 * JImageLists.
 * 
 * @author olaf
 * 
 */
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
        // check for undefined properties
        for (String p : propertiesToSynchronize) {
            if (null == getCellPropertyDescriptor(p)) {
                throw new IllegalArgumentException("undefined cell property: " + p);
            }
        }
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
                try {
                    for (JImageListView destList : getLists()) {
                        if (destList != sourceList) {
                            int si = destList.getSelectedIndex();
                            if (si != -1) {
                                ImageListViewCell destCell = destList.getCell(si);
                                for (String propName : propertiesToSynchronize) {
                                    PropertyDescriptor pd = getCellPropertyDescriptor(propName);
                                    Object propValue = pd.getReadMethod().invoke(sourceCell);
                                    pd.getWriteMethod().invoke(destCell, propValue);
                                }
                            }
                        }
                    }
                } catch (InvocationTargetException e1) {
                    throw new IllegalStateException(e1.getMessage(), e1);
                } catch (IllegalAccessException e2) {
                    throw new IllegalStateException(e2.getMessage(), e2);
                }
            }
        }
    }


    protected static PropertyDescriptor getCellPropertyDescriptor(String propName) {
        if (cellPropsByName == null) {
            try {
                cellPropsByName = new HashMap<String, PropertyDescriptor>();
                BeanInfo cellBeanInfo = Introspector.getBeanInfo(ImageListViewCell.class);
                for (PropertyDescriptor pd : cellBeanInfo.getPropertyDescriptors()) {
                    cellPropsByName.put(pd.getName(), pd);
                }
            } catch (IntrospectionException e) {
                throw new IllegalStateException(e.getLocalizedMessage(), e);
            }
        }
        return cellPropsByName.get(propName);
    }

    protected static Map<String, PropertyDescriptor> cellPropsByName;
    
}
