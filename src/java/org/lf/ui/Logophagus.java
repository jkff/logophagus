package org.lf.ui;

import org.lf.plugins.analysis.filelog.FileBackedLogPlugin;
import org.lf.plugins.analysis.filtersubstr.FilterBySubstringPlugin;
import org.lf.plugins.analysis.highlight.HighlightRegexPlugin;
import org.lf.plugins.analysis.merge.MergeLogsPlugin;
import org.lf.plugins.analysis.sidebyside.SideBySidePlugin;
import org.lf.plugins.display.ViewFieldSplittedLogPlugin;
import org.lf.plugins.display.ViewScrollableLogPlugin;
import org.lf.plugins.display.ViewSideBySidePlugin;
import org.lf.services.AnalysisPluginRepository;
import org.lf.services.DisplayPluginRepository;
import org.lf.ui.components.menu.LogophagusMenuBar;
import org.lf.ui.components.pluginPanel.PluginPanel;
import org.lf.ui.components.popup.TreeRightClickPopup;
import org.lf.ui.components.tree.TreeSelectionController;
import org.lf.ui.model.AnalysisPluginsTreeModel;
import org.lf.ui.model.NodeData;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class Logophagus extends JFrame {
    private AnalysisPluginsTreeModel pluginsTreeModel;

    private JMenuBar menuBar;
    private JTree pluginsTreeView;
    private PluginPanel pluginPanel;

    private Logophagus() {
        super("Logophagus");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "OS look-and-feel theme is not available. Will use default instead", "Warning", JOptionPane.ERROR_MESSAGE);
        }

        this.pluginsTreeModel = new AnalysisPluginsTreeModel();
        this.initComponents();
        this.setVisible(true);
        this.pack();
    }

    private void initComponents() {
        this.setMinimumSize(new Dimension(800, 600));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setJMenuBar(getLogophagusMenuBar());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerSize(5);
        splitPane.setContinuousLayout(true);
        splitPane.setRightComponent(getPluginPanel());

        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 4));
        treePanel.add(new JScrollPane(getAnalysisPluginTreeView()));
        treePanel.setVisible(true);

        splitPane.setLeftComponent(treePanel);
        splitPane.setDividerLocation(250);
        splitPane.setVisible(true);
        this.add(splitPane);
    }

    private JMenuBar getLogophagusMenuBar() {
        if (menuBar != null) return menuBar;
        menuBar = new LogophagusMenuBar(pluginsTreeModel);
        return menuBar;
    }

    private PluginPanel getPluginPanel() {
        if (pluginPanel != null) return pluginPanel;
        pluginPanel = new PluginPanel();
        pluginPanel.setLayout(new BorderLayout());
        return pluginPanel;
    }

    private JTree getAnalysisPluginTreeView() {
        if (pluginsTreeView != null) return pluginsTreeView;
        pluginsTreeView = new JTree(pluginsTreeModel);
        pluginsTreeView.setRootVisible(false);
        pluginsTreeView.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        pluginsTreeView.addMouseListener(new TreeMouseListener());
        pluginsTreeView.addTreeSelectionListener(getPluginPanel());
        pluginsTreeView.setCellRenderer(new AnalysisPluginTreeCellRenderer());
        new TreeSelectionController(pluginsTreeView);
        return pluginsTreeView;
    }

    public static void main(String[] args) {
        AnalysisPluginRepository.register(new FileBackedLogPlugin());
        AnalysisPluginRepository.register(new FilterBySubstringPlugin());
        AnalysisPluginRepository.register(new SideBySidePlugin());
//            AnalysisPluginRepository.register(SplitByFieldValuesPlugin.class);
        AnalysisPluginRepository.register(new HighlightRegexPlugin());
        AnalysisPluginRepository.register(new MergeLogsPlugin());

        DisplayPluginRepository.register(new ViewScrollableLogPlugin());
        DisplayPluginRepository.register(new ViewSideBySidePlugin());
        DisplayPluginRepository.register(new ViewFieldSplittedLogPlugin());

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Logophagus();
            }
        });
    }

    private class TreeMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            TreePath selPath = pluginsTreeView.getPathForLocation(e.getX(), e.getY());
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (e.isControlDown()) {
                    pluginsTreeView.addSelectionPath(selPath);
                } else {
                    pluginsTreeView.setSelectionPath(selPath);
                }
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                boolean isAtSelection = false;
                if (pluginsTreeView.getSelectionPaths() != null)
                    for (TreePath cur : pluginsTreeView.getSelectionPaths())
                        if (cur.equals(selPath)) {
                            isAtSelection = true;
                            break;
                        }
                if (!isAtSelection)
                    pluginsTreeView.setSelectionPath(selPath);
                JPopupMenu popMenu = new TreeRightClickPopup(pluginsTreeModel, pluginsTreeView.getSelectionPaths());
                popMenu.show(pluginsTreeView, e.getX(), e.getY());
            }
        }
    }

    private class AnalysisPluginTreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean sel,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {
            NodeData nodeValue = (NodeData) ((DefaultMutableTreeNode) value).getUserObject();
            JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (nodeValue != null)
                label.setIcon(nodeValue.icon);
            return label;
        }

    }

}


