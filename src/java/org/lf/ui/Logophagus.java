package org.lf.ui;

import org.lf.plugins.analysis.filelog.FileBackedLogPlugin;
import org.lf.plugins.analysis.filtersubstr.FilterBySubstringPlugin;
import org.lf.plugins.analysis.highlight.HighlightRegexPlugin;
import org.lf.plugins.analysis.merge.MergeLogsPlugin;
import org.lf.plugins.analysis.sidebyside.SideBySidePlugin;
import org.lf.plugins.analysis.splitbyfield.SplitByFieldValuesPlugin;
//import org.lf.plugins.analysis.splitbyfield.SplitByFieldValuesPlugin;
import org.lf.plugins.display.ViewFieldSplittedLogPlugin;
import org.lf.plugins.display.ViewScrollableLogPlugin;
import org.lf.plugins.display.ViewSideBySidePlugin;
import org.lf.services.AnalysisPluginRepository;
import org.lf.services.DisplayPluginRepository;
import org.lf.services.PluginException;
import org.lf.ui.components.menu.LogophagusMenuBar;
import org.lf.ui.components.pluginPanel.PluginPanel;
import org.lf.ui.components.popup.TreeRightClickPopup;
import org.lf.ui.components.tree.LogsHierarchyView;
import org.lf.ui.model.LogsHierarchy;
import org.lf.ui.model.NodeData;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class Logophagus extends JFrame {
    private LogsHierarchy logsHierarchy;

    private JMenuBar menuBar;
    private LogsHierarchyView logsTree;
    private PluginPanel pluginPanel;

    private Logophagus() {
        super("Logophagus");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "OS look-and-feel theme is not available. Will use default instead", "Warning", JOptionPane.ERROR_MESSAGE);
        }

        logsHierarchy = new LogsHierarchy();
        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        this.setMinimumSize(new Dimension(800,600));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setJMenuBar(getLogophagusMenuBar());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerSize(5);
        splitPane.setContinuousLayout(true);
        splitPane.setRightComponent(getPluginPanel());

        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.setBorder(BorderFactory.createEmptyBorder(12,12,12,4));
        treePanel.add(new JScrollPane(getLogsHierarchyView()));
        treePanel.setVisible(true);

        splitPane.setLeftComponent(treePanel);
        splitPane.setDividerLocation(250);
        this.setContentPane(splitPane);
    }

    private JMenuBar getLogophagusMenuBar() {
        if (menuBar != null) return menuBar;
        menuBar = new LogophagusMenuBar(logsHierarchy);
        return menuBar;
    }

    private PluginPanel getPluginPanel() {
        if (pluginPanel != null) return pluginPanel;
        pluginPanel = new PluginPanel();
        pluginPanel.setLayout(new BorderLayout());
        return pluginPanel;
    }

    private LogsHierarchyView getLogsHierarchyView() {
        if (logsTree != null) return logsTree;
        logsTree = new LogsHierarchyView(logsHierarchy);
        logsTree.setRootVisible(false);
        logsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        logsTree.addMouseListener(new TreeMouseListener());
        logsTree.addTreeSelectionListener(getPluginPanel());
        TreeCellRenderer renderer = new MyTreeCellRenderer();
        
//        renderer.setOpenIcon(new ImageIcon(ProgramProperties.iconsPath +"folder_files.gif"));
//        renderer.setClosedIcon(new ImageIcon(ProgramProperties.iconsPath +"folder_files.gif"));
//        renderer.setLeafIcon(new ImageIcon(ProgramProperties.iconsPath +"file.gif"));
        logsTree.setCellRenderer(renderer);
        logsTree.setAutoscrolls(true);
        logsTree.addContainerListener(new ContainerAdapter() {
            @Override
            public void componentAdded(ContainerEvent ce) {
                logsTree.setSelectionPath(logsHierarchy.getLastNewPath());
                logsTree.scrollPathToVisible(logsHierarchy.getLastNewPath());
            }
        });
        return logsTree;
    }

    public static void main(String[] args) {
        try {
            AnalysisPluginRepository.register(FileBackedLogPlugin.class);
            AnalysisPluginRepository.register(FilterBySubstringPlugin.class);
            AnalysisPluginRepository.register(SideBySidePlugin.class);
            AnalysisPluginRepository.register(SplitByFieldValuesPlugin.class);
            AnalysisPluginRepository.register(HighlightRegexPlugin.class);
            AnalysisPluginRepository.register(MergeLogsPlugin.class);

            DisplayPluginRepository.register(ViewScrollableLogPlugin.class);
            DisplayPluginRepository.register(ViewSideBySidePlugin.class);
            DisplayPluginRepository.register(ViewFieldSplittedLogPlugin.class);

        } catch (PluginException e) {
            System.out.println("Can't register plugin:" + e);
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Logophagus().pack();
            }
        });
    }

    private class TreeMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            TreePath selPath = logsTree.getPathForLocation(e.getX(), e.getY());
            if(e.getButton() == MouseEvent.BUTTON1) {
                if (e.isControlDown()) {
                    logsTree.addSelectionPath(selPath);
                } else {
                    logsTree.setSelectionPath(selPath);
                }
            } else if(e.getButton() == MouseEvent.BUTTON3) {
                boolean isAtSelection = false;
                if (logsTree.getSelectionPaths() != null )
                    for (TreePath cur : logsTree.getSelectionPaths()) {
                        if (cur.equals(selPath)) {
                            isAtSelection = true;
                            break;
                        }
                    }
                if (!isAtSelection)
                    logsTree.setSelectionPath(selPath);
                JPopupMenu popMenu = new TreeRightClickPopup(logsHierarchy, logsTree.getSelectionPaths());
                popMenu.show(logsTree, e.getX(), e.getY());
            }
        }
    }

    private class MyTreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
            NodeData nodeValue = (NodeData)((DefaultMutableTreeNode)value).getUserObject();
            JLabel label = (JLabel)super.getTreeCellRendererComponent(tree, value, sel,expanded,leaf,row,hasFocus);
            if (nodeValue != null)
                label.setIcon(nodeValue.icon);
            return label;
        }
        
    }
    
}