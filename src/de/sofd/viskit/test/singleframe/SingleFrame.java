/*
 * SingleFrame.java
 *
 * Created on Oct 17, 2009, 9:18:06 PM
 */

package de.sofd.viskit.test.singleframe;

import de.sofd.viskit.ui.imagelist.JImageListView;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.ListModel;

/**
 *
 * @author olaf
 */
public class SingleFrame extends javax.swing.JFrame {

    private final List<JImageListView> lists;

    public SingleFrame() {
        this(new ArrayList<ListModel>());
    }

    public SingleFrame(List<ListModel> listModels) {
        initComponents();
        listsPanel.setLayout(new GridLayout(1, listModels.size(), 10, 0));
        lists = new ArrayList<JImageListView>();
        for (ListModel lm : listModels) {
            ListViewPanel lvp = new ListViewPanel();
            lvp.getListView().setModel(lm);
            listsPanel.add(lvp);
            lists.add(lvp.getListView());
        }
        selectionSynchronizationController.setLists(lists.toArray(new JImageListView[listModels.size()]));
        scaleModeSynchronizationController.setLists(lists.toArray(new JImageListView[listModels.size()]));
        roiToolApplicationController.setLists(lists.toArray(new JImageListView[listModels.size()]));
    }

    public List<JImageListView> getEmbeddedImageListViews() {
        return Collections.unmodifiableList(lists);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        selectionSynchronizationController = new de.sofd.viskit.controllers.ImageListViewSelectionSynchronizationController();
        scaleModeSynchronizationController = new de.sofd.viskit.controllers.ImageListViewScaleModeSynchronizationController();
        roiToolApplicationController = new de.sofd.viskit.controllers.ImageListViewRoiToolApplicationController();
        listsPanel = new javax.swing.JPanel();
        controlsPanel = new javax.swing.JPanel();
        syncSelectionsCheckbox = new javax.swing.JCheckBox();
        syncScaleModesCheckbox = new javax.swing.JCheckBox();
        keepRelativeSelIndicesCheckbox = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        roiToolPanel = new de.sofd.viskit.ui.RoiToolPanel();

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, roiToolPanel, org.jdesktop.beansbinding.ObjectProperty.create(), roiToolApplicationController, org.jdesktop.beansbinding.BeanProperty.create("roiToolPanel"));
        bindingGroup.addBinding(binding);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Single Frame Test");

        javax.swing.GroupLayout listsPanelLayout = new javax.swing.GroupLayout(listsPanel);
        listsPanel.setLayout(listsPanelLayout);
        listsPanelLayout.setHorizontalGroup(
            listsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 976, Short.MAX_VALUE)
        );
        listsPanelLayout.setVerticalGroup(
            listsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 406, Short.MAX_VALUE)
        );

        syncSelectionsCheckbox.setText("synchronize selections");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, selectionSynchronizationController, org.jdesktop.beansbinding.ELProperty.create("${enabled}"), syncSelectionsCheckbox, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        syncScaleModesCheckbox.setText("synchronize scale modes");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, scaleModeSynchronizationController, org.jdesktop.beansbinding.ELProperty.create("${enabled}"), syncScaleModesCheckbox, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);

        keepRelativeSelIndicesCheckbox.setText("keep relative indices");

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, selectionSynchronizationController, org.jdesktop.beansbinding.ELProperty.create("${keepRelativeSelectionIndices}"), keepRelativeSelIndicesCheckbox, org.jdesktop.beansbinding.BeanProperty.create("selected"));
        bindingGroup.addBinding(binding);
        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, syncSelectionsCheckbox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), keepRelativeSelIndicesCheckbox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("ROI"));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(roiToolPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(roiToolPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout controlsPanelLayout = new javax.swing.GroupLayout(controlsPanel);
        controlsPanel.setLayout(controlsPanelLayout);
        controlsPanelLayout.setHorizontalGroup(
            controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(controlsPanelLayout.createSequentialGroup()
                        .addComponent(syncSelectionsCheckbox)
                        .addGap(18, 18, 18)
                        .addComponent(syncScaleModesCheckbox))
                    .addGroup(controlsPanelLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(keepRelativeSelIndicesCheckbox)))
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(334, Short.MAX_VALUE))
        );
        controlsPanelLayout.setVerticalGroup(
            controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(controlsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(controlsPanelLayout.createSequentialGroup()
                        .addGroup(controlsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(syncSelectionsCheckbox)
                            .addComponent(syncScaleModesCheckbox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(keepRelativeSelIndicesCheckbox))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(64, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(controlsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(listsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(listsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(controlsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        bindingGroup.bind();

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel controlsPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JCheckBox keepRelativeSelIndicesCheckbox;
    private javax.swing.JPanel listsPanel;
    private de.sofd.viskit.controllers.ImageListViewRoiToolApplicationController roiToolApplicationController;
    private de.sofd.viskit.ui.RoiToolPanel roiToolPanel;
    private de.sofd.viskit.controllers.ImageListViewScaleModeSynchronizationController scaleModeSynchronizationController;
    private de.sofd.viskit.controllers.ImageListViewSelectionSynchronizationController selectionSynchronizationController;
    private javax.swing.JCheckBox syncScaleModesCheckbox;
    private javax.swing.JCheckBox syncSelectionsCheckbox;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables

}
