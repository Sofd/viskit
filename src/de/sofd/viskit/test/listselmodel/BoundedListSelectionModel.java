package de.sofd.viskit.test.listselmodel;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;


public class BoundedListSelectionModel implements ListSelectionModel {

    protected DefaultListSelectionModel backend = new DefaultListSelectionModel();

    protected int lowerBound = 5;
    
    public void addListSelectionListener(ListSelectionListener l) {
        backend.addListSelectionListener(l);
    }

    public void addSelectionInterval(int index0, int index1) {
        System.out.println("addSelectionInterval("+index0+", "+index1+")");
        backend.addSelectionInterval(index0, index1);
    }
    
    public void clearSelection() {
        System.out.println("clearSelection()");
        backend.clearSelection();
    }

    public int getAnchorSelectionIndex() {
        return backend.getAnchorSelectionIndex();
    }

    public int getLeadSelectionIndex() {
        return backend.getLeadSelectionIndex();
    }

    public int getMaxSelectionIndex() {
        return backend.getMaxSelectionIndex();
    }

    public int getMinSelectionIndex() {
        return backend.getMinSelectionIndex();
    }

    public int getSelectionMode() {
        return backend.getSelectionMode();
    }

    public boolean getValueIsAdjusting() {
        return backend.getValueIsAdjusting();
    }

    public int hashCode() {
        return backend.hashCode();
    }

    public void insertIndexInterval(int index, int length, boolean before) {
        System.out.println("insertIndexInterval("+index+", "+length+","+before+")");
        backend.insertIndexInterval(index, length, before);
    }

    public boolean isSelectedIndex(int index) {
        return backend.isSelectedIndex(index);
    }

    public boolean isSelectionEmpty() {
        return backend.isSelectionEmpty();
    }

    public void removeIndexInterval(int index0, int index1) {
        System.out.println("removeIndexInterval("+index0+", "+index1+")");
        backend.removeIndexInterval(index0, index1);
    }

    public void removeListSelectionListener(ListSelectionListener l) {
        backend.removeListSelectionListener(l);
    }

    public void removeSelectionInterval(int index0, int index1) {
        backend.removeSelectionInterval(index0, index1);
    }

    public void setAnchorSelectionIndex(int anchorIndex) {
        System.out.println("setAnchorSelectionIndex("+anchorIndex+")");
        backend.setAnchorSelectionIndex(anchorIndex);
    }

    public void setLeadSelectionIndex(int leadIndex) {
        System.out.println("setLeadSelectionIndex("+leadIndex+")");
        backend.setLeadSelectionIndex(leadIndex);
    }

    public void setSelectionInterval(int index0, int index1) {
        System.out.println("setSelectionInterval("+index0+", "+index1+")");
        if (index0 >= lowerBound) {
            backend.setSelectionInterval(index0, index1);
        }
    }

    public void setSelectionMode(int selectionMode) {
        backend.setSelectionMode(selectionMode);
    }

    public void setValueIsAdjusting(boolean isAdjusting) {
        System.out.println("     setValueIsAdjusting("+isAdjusting+")");
        backend.setValueIsAdjusting(isAdjusting);
    }
    
}
