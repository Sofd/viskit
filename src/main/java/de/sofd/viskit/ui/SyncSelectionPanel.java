/*
 * SelectionPanel.java
 *
 */

package de.sofd.viskit.ui;

import javax.swing.JToggleButton;

/**
 *
 * @author sofd GmbH
 *
 */
public class SyncSelectionPanel extends javax.swing.JPanel {

    /** Creates new form SelectionPanel */
    public SyncSelectionPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        syncSelectionToggleButton = new javax.swing.JToggleButton();
        keepRelativeToggleButton = new javax.swing.JToggleButton();

        setBorder(javax.swing.BorderFactory.createEtchedBorder());

        syncSelectionToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/sofd/viskit/ui/link_obj.gif"))); // NOI18N
        syncSelectionToggleButton.setToolTipText("Synchronize Selection");

        keepRelativeToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/sofd/viskit/ui/targetinternal_obj.gif"))); // NOI18N
        keepRelativeToggleButton.setToolTipText("Keep Relative Indices");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(syncSelectionToggleButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(keepRelativeToggleButton))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(syncSelectionToggleButton)
            .addComponent(keepRelativeToggleButton)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JToggleButton keepRelativeToggleButton;
    protected javax.swing.JToggleButton syncSelectionToggleButton;
    // End of variables declaration//GEN-END:variables

    public JToggleButton getKeepRelativeToggleButton() {
        return keepRelativeToggleButton;
    }

    public JToggleButton getSyncSelectionToggleButton() {
        return syncSelectionToggleButton;
    }

}
