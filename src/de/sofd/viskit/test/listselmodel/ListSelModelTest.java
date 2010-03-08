package de.sofd.viskit.test.listselmodel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;


public class ListSelModelTest {

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ListSelModelTest();
            }
        });
    }

    public ListSelModelTest() {
        JFrame f = new JFrame("ListSelectionModel test");
        final JList list = new JList();
        DefaultListModel listModel = new DefaultListModel();
        for (int i = 0; i < 20; i++) {
            listModel.addElement("origElt"+i);
        }
        list.setModel(listModel);
        BoundedListSelectionModel sm = new BoundedListSelectionModel();
        list.setSelectionModel(sm);
        JScrollPane sp = new JScrollPane(list);
        f.getContentPane().add(sp, BorderLayout.CENTER);

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new AbstractAction("add") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int idx = list.getSelectedIndex();
                if (idx != -1) {
                    DefaultListModel listModel = (DefaultListModel) list.getModel();
                    listModel.add(idx, "addedEltAt"+idx);
                }
            }
        });
        toolbar.add(new AbstractAction("rm") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int idx = list.getSelectedIndex();
                if (idx != -1) {
                    DefaultListModel listModel = (DefaultListModel) list.getModel();
                    listModel.remove(idx);
                }
            }
        });
        toolbar.add(new AbstractAction("set") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int idx = list.getSelectedIndex();
                if (idx != -1) {
                    DefaultListModel listModel = (DefaultListModel) list.getModel();
                    listModel.set(idx, "setEltAt"+idx);
                }
            }
        });
        toolbar.add(new AbstractAction("replaceModel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultListModel listModel = new DefaultListModel();
                for (int i = 0; i < 20; i++) {
                    listModel.addElement("replacedElt"+i);
                }
                list.setModel(listModel);
            }
        });
        toolbar.add(new AbstractAction("dumpSelection") {
            @Override
            public void actionPerformed(ActionEvent e) {
                ListSelectionModel sm = list.getSelectionModel();
                int min = sm.getMinSelectionIndex();
                if (min == -1) {
                    System.out.println("[nothing selected]");
                } else {
                    int max = sm.getMaxSelectionIndex();
                    for (int i = min; i <= max; i++) {
                        if (sm.isSelectedIndex(i)) {
                            System.out.print(""+i+" ");
                        }
                    }
                    System.out.println();
                }
            }
        });
        f.getContentPane().add(toolbar, BorderLayout.NORTH);

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(500, 700);
        f.setVisible(true);
    }
    
}
