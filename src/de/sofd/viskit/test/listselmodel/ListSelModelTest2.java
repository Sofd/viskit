package de.sofd.viskit.test.listselmodel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class ListSelModelTest2 {

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ListSelModelTest2();
            }
        });
    }

    public ListSelModelTest2() {
        JFrame f = new JFrame("ListSelectionModel test");
        final JList list = new JList();
        DefaultListModel listModel = new DefaultListModel();
        for (int i = 0; i < 20; i++) {
            listModel.addElement("origElt"+i);
        }
        list.setModel(listModel);
        final ListSelectionModel sm = new DebugListSelectionModel();
        sm.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                System.out.println("        ListSelectionEvent: [" + e.getFirstIndex() + ", " + e.getLastIndex() + "] isAdj=" + e.getValueIsAdjusting());
            }
        });
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
        final JTextField lowerEntry = new JTextField("5") {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(50, super.getMaximumSize().height);
            }
        };
        final JTextField upperEntry = new JTextField("17") {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(50, super.getMaximumSize().height);
            }
        };
        toolbar.add(new JLabel("lower:"));
        toolbar.add(lowerEntry);
        toolbar.add(new JLabel("upper:"));
        toolbar.add(upperEntry);
        toolbar.add(new AbstractAction("rm") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int lower = Integer.parseInt(lowerEntry.getText());
                    int upper = Integer.parseInt(upperEntry.getText());
                    DefaultListModel listModel = (DefaultListModel) list.getModel();
                    listModel.removeRange(lower, upper);
                } catch (NumberFormatException ex) {
                    //ignore
                }
            }
        });
        toolbar.add(new AbstractAction("add") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int lower = Integer.parseInt(lowerEntry.getText());
                    int upper = Integer.parseInt(upperEntry.getText());
                    DefaultListModel listModel = (DefaultListModel) list.getModel();
                    int n = upper - lower + 1;
                    for (int i = 0; i < n; i++) {
                        listModel.insertElementAt("added"+(upper-i), lower);
                    }
                } catch (NumberFormatException ex) {
                    //ignore
                }
            }
        });
        
        toolbar.add(new JLabel("idx:"));
        final JTextField idxEntry = new JTextField("17") {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(50, super.getMaximumSize().height);
            }
        };
        toolbar.add(idxEntry);
        toolbar.add(new AbstractAction("sel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int idx = Integer.parseInt(idxEntry.getText());
                    list.getSelectionModel().setSelectionInterval(idx, idx);
                    //list.getSelectionModel().setLeadSelectionIndex(idx);
                } catch (NumberFormatException ex) {
                    //ignore
                }
            }
        });

        f.getContentPane().add(toolbar, BorderLayout.NORTH);

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(700, 700);
        f.setVisible(true);
    }
    
}
