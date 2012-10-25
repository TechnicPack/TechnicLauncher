/*
 * ModsDialog.java
 *
 * Created on __DATE__, __TIME__
 */

package org.spoutcraft.launcher.gui;

/**
 *
 * @author  __USER__
 */
public class ModsDialog extends javax.swing.JDialog {

	/** Creates new form ModsDialog */
	public ModsDialog(java.awt.Frame parent, boolean modal) {
		super(parent, modal);
		initComponents();
	}

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        qualityGroup = new javax.swing.ButtonGroup();
        blurBox = new javax.swing.JCheckBox();
        blurSlider = new javax.swing.JSlider();
        highQuality = new javax.swing.JRadioButton();
        lowQuality = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        blurBox.setLabel("Enable Blur");
        blurBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                blurBoxMousePressed(evt);
            }
        });

        blurSlider.setForeground(new java.awt.Color(25, 25, 25));
        blurSlider.setMajorTickSpacing(10);
        blurSlider.setMinorTickSpacing(1);
        blurSlider.setPaintTicks(true);
        blurSlider.setValue(35);
        blurSlider.setEnabled(false);
        blurSlider.setOpaque(false);

        highQuality.setText("jRadioButton1");

        lowQuality.setText("jRadioButton2");
        lowQuality.setAutoscrolls(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(blurBox)
                    .addComponent(lowQuality, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(highQuality, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                    .addComponent(blurSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(blurSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(blurBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(highQuality)
                    .addComponent(lowQuality))
                .addContainerGap(234, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void blurBoxMousePressed(java.awt.event.MouseEvent evt) {
		//blurSlider
	}

	                       

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				ModsDialog dialog = new ModsDialog(new javax.swing.JFrame(),
						true);
				dialog.addWindowListener(new java.awt.event.WindowAdapter() {
					public void windowClosing(java.awt.event.WindowEvent e) {
						System.exit(0);
					}
				});
				dialog.setVisible(true);
			}
		});
	}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JCheckBox blurBox;
    private javax.swing.JSlider blurSlider;
    private javax.swing.JRadioButton highQuality;
    private javax.swing.JRadioButton lowQuality;
    private javax.swing.ButtonGroup qualityGroup;
    // End of variables declaration//GEN-END:variables

}