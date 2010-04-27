package org.lf.plugins.context;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.text.Position;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

public class TreeContext {
    private final JTree tree;

    public TreeContext(JTree tree) {
        this.tree = tree;
    }

    public int getRowCount() {
        return tree.getRowCount();
    }

    public int getRowForLocation(int i, int i1) {
        return tree.getRowForLocation(i, i1);
    }

    public int getRowForPath(TreePath treePath) {
        return tree.getRowForPath(treePath);
    }

    public int getRowHeight() {
        return tree.getRowHeight();
    }

    public int getScrollableBlockIncrement(Rectangle rectangle, int i, int i1) {
        return tree.getScrollableBlockIncrement(rectangle, i, i1);
    }

    public boolean getScrollableTracksViewportHeight() {
        return tree.getScrollableTracksViewportHeight();
    }

    public boolean getScrollableTracksViewportWidth() {
        return tree.getScrollableTracksViewportWidth();
    }

    public int getScrollableUnitIncrement(Rectangle rectangle, int i, int i1) {
        return tree.getScrollableUnitIncrement(rectangle, i, i1);
    }

    public int getSelectionCount() {
        return tree.getSelectionCount();
    }

    public boolean getScrollsOnExpand() {
        return tree.getScrollsOnExpand();
    }

    public TreeSelectionModel getSelectionModel() {
        return tree.getSelectionModel();
    }

    public TreePath getSelectionPath() {
        return tree.getSelectionPath();
    }

    public TreePath[] getSelectionPaths() {
        return tree.getSelectionPaths();
    }

    public int[] getSelectionRows() {
        return tree.getSelectionRows();
    }

    public boolean getShowsRootHandles() {
        return tree.getShowsRootHandles();
    }

    public int getToggleClickCount() {
        return tree.getToggleClickCount();
    }

    public String getToolTipText(MouseEvent mouseEvent) {
        return tree.getToolTipText(mouseEvent);
    }

    public TreeSelectionListener[] getTreeSelectionListeners() {
        return tree.getTreeSelectionListeners();
    }

    public TreeWillExpandListener[] getTreeWillExpandListeners() {
        return tree.getTreeWillExpandListeners();
    }

    public AccessibleContext getAccessibleContext() {
        return tree.getAccessibleContext();
    }

    public TreePath getAnchorSelectionPath() {
        return tree.getAnchorSelectionPath();
    }

    public TreeCellRenderer getCellRenderer() {
        return tree.getCellRenderer();
    }

    public TreeCellEditor getCellEditor() {
        return tree.getCellEditor();
    }

    public TreePath getClosestPathForLocation(int i, int i1) {
        return tree.getClosestPathForLocation(i, i1);
    }

    public int getClosestRowForLocation(int i, int i1) {
        return tree.getClosestRowForLocation(i, i1);
    }

    public boolean getDragEnabled() {
        return tree.getDragEnabled();
    }

    public JTree.DropLocation getDropLocation() {
        return tree.getDropLocation();
    }

    public DropMode getDropMode() {
        return tree.getDropMode();
    }

    public boolean getExpandsSelectedPaths() {
        return tree.getExpandsSelectedPaths();
    }

    public Enumeration<TreePath> getExpandedDescendants(TreePath treePath) {
        return tree.getExpandedDescendants(treePath);
    }

    public TreePath getEditingPath() {
        return tree.getEditingPath();
    }

    public boolean getInvokesStopCellEditing() {
        return tree.getInvokesStopCellEditing();
    }

    public Object getLastSelectedPathComponent() {
        return tree.getLastSelectedPathComponent();
    }

    public TreePath getLeadSelectionPath() {
        return tree.getLeadSelectionPath();
    }

    public int getLeadSelectionRow() {
        return tree.getLeadSelectionRow();
    }

    public int getMaxSelectionRow() {
        return tree.getMaxSelectionRow();
    }

    public int getMinSelectionRow() {
        return tree.getMinSelectionRow();
    }

    public TreeModel getModel() {
        return tree.getModel();
    }

    public TreePath getNextMatch(String s, int i, Position.Bias bias) {
        return tree.getNextMatch(s, i, bias);
    }

    public Rectangle getPathBounds(TreePath treePath) {
        return tree.getPathBounds(treePath);
    }

    public TreePath getPathForLocation(int i, int i1) {
        return tree.getPathForLocation(i, i1);
    }

    public TreePath getPathForRow(int i) {
        return tree.getPathForRow(i);
    }

    public TreeExpansionListener[] getTreeExpansionListeners() {
        return tree.getTreeExpansionListeners();
    }

    public int getVisibleRowCount() {
        return tree.getVisibleRowCount();
    }

    public boolean isCollapsed(int i) {
        return tree.isCollapsed(i);
    }

    public boolean isEditable() {
        return tree.isEditable();
    }

    public boolean isCollapsed(TreePath treePath) {
        return tree.isCollapsed(treePath);
    }

    public boolean isEditing() {
        return tree.isEditing();
    }

    public boolean isExpanded(int i) {
        return tree.isExpanded(i);
    }

    public boolean isExpanded(TreePath treePath) {
        return tree.isExpanded(treePath);
    }

    public boolean isFixedRowHeight() {
        return tree.isFixedRowHeight();
    }

    public boolean isLargeModel() {
        return tree.isLargeModel();
    }

    public boolean isPathEditable(TreePath treePath) {
        return tree.isPathEditable(treePath);
    }

    public boolean isPathSelected(TreePath treePath) {
        return tree.isPathSelected(treePath);
    }

    public boolean isRootVisible() {
        return tree.isRootVisible();
    }

    public boolean isRowSelected(int i) {
        return tree.isRowSelected(i);
    }

    public boolean isSelectionEmpty() {
        return tree.isSelectionEmpty();
    }

    public boolean isVisible(TreePath treePath) {
        return tree.isVisible(treePath);
    }

    public void addTreeSelectionListener(TreeSelectionListener treeSelectionListener) {
        tree.addTreeSelectionListener(treeSelectionListener);
    }
}
