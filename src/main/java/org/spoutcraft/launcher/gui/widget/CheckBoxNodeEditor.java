package org.spoutcraft.launcher.gui.widget;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor {

	CheckBoxNodeRenderer	renderer		= new CheckBoxNodeRenderer();
	ChangeEvent						changeEvent	= null;
	JTree									tree;

	public CheckBoxNodeEditor(JTree tree) {
		this.tree = tree;
	}

	@Override
	public Object getCellEditorValue() {
		JToggleButton checkbox = renderer.getLeafRenderer();
		int groupdId = -1;
		if (checkbox instanceof JRadioButton) {
			groupdId = (Integer) ((JRadioButton) checkbox).getClientProperty("groupId");
		}
		CheckBoxNode checkBoxNode = new CheckBoxNode(checkbox.getText(), checkbox.isSelected(), groupdId);
		return checkBoxNode;
	}

	@Override
	public boolean isCellEditable(EventObject event) {
		boolean returnValue = false;
		if (event instanceof MouseEvent) {
			MouseEvent mouseEvent = (MouseEvent) event;
			TreePath path = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
			if (path != null) {
				Object node = path.getLastPathComponent();
				if ((node != null) && (node instanceof DefaultMutableTreeNode)) {
					DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
					Object userObject = treeNode.getUserObject();
					returnValue = ((treeNode.isLeaf()) && (userObject instanceof CheckBoxNode));
				}
			}
		}
		return returnValue;
	}

	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row) {

		final Component editor = renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);

		// editor always selected / focused
		final ItemListener itemListener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent itemEvent) {
				if (stopCellEditing()) {
					fireEditingStopped();
				}
			}
		};
		if (editor instanceof JToggleButton) {
			((JToggleButton) editor).addItemListener(itemListener);
		}

		return editor;
	}
}