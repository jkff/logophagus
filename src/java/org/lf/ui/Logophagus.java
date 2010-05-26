package org.lf.ui;

import org.lf.plugins.PluginManager;
import org.lf.plugins.ProgramContext;
import org.lf.plugins.display.ViewScrollableLogPlugin;
import org.lf.plugins.tree.filelog.OpenLogFromFilePlugin;
import org.lf.plugins.tree.filtersubstr.FilterByCriteriaPlugin;
import org.lf.plugins.tree.highlight.HighlightRegexpPlugin;
import org.lf.plugins.tree.merge.MergeLogsPlugin;
import org.lf.plugins.tree.sidebyside.ViewSideBySidePlugin;
import org.lf.plugins.tree.splitbyfield.SplitByFieldPlugin;
import org.lf.ui.components.menu.LogophagusMenuBar;
import org.lf.ui.components.pluginPanel.PluginPanel;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogPlugin;
import org.lf.ui.components.tree.PluginTree;

import javax.swing.*;
import java.awt.*;

public class Logophagus extends JFrame {
    private JMenuBar menuBar;
    private PluginTree pluginsTreeView;
    private PluginPanel pluginPanel;

    private ProgramContext context;

    private Logophagus() {
        super("Logophagus");

        context = new ProgramContext();

        PluginManager pluginManager = new PluginManager(context);

        pluginManager.addPlugin(new ViewScrollableLogPlugin());
        pluginManager.addPlugin(new ScrollableLogPlugin());
        pluginManager.addPlugin(new OpenLogFromFilePlugin());
        pluginManager.addPlugin(new FilterByCriteriaPlugin());
        pluginManager.addPlugin(new ViewSideBySidePlugin());
        pluginManager.addPlugin(new SplitByFieldPlugin());
        pluginManager.addPlugin(new HighlightRegexpPlugin());
        pluginManager.addPlugin(new MergeLogsPlugin());
//        pluginManager.addPlugin(new ViewFieldSplittedLogPlugin());

        pluginManager.init();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "OS look-and-feel theme is not available. Will use default instead",
                    "Warning", JOptionPane.ERROR_MESSAGE);
        }

        this.initComponents();
        this.pack();
        this.setVisible(true);
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
        treePanel.add(new JScrollPane(getPluginTree()));
        treePanel.setVisible(true);

        splitPane.setLeftComponent(treePanel);
        splitPane.setDividerLocation(250);
        splitPane.setVisible(true);
        this.getContentPane().add(splitPane);
    }

    private JMenuBar getLogophagusMenuBar() {
        if (menuBar != null) return menuBar;
        menuBar = new LogophagusMenuBar(getPluginTree());
        return menuBar;
    }

    private PluginPanel getPluginPanel() {
        if (pluginPanel != null) return pluginPanel;
        pluginPanel = new PluginPanel(context.getDisplayPluginRepository());
        pluginPanel.setLayout(new BorderLayout());
        return pluginPanel;
    }

    private PluginTree getPluginTree() {
        if (pluginsTreeView != null) return pluginsTreeView;
        pluginsTreeView = new PluginTree(context.getTreePluginRepository());
        pluginsTreeView.addTreeSelectionListener(getPluginPanel());
        return pluginsTreeView;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Logophagus();
            }
        });
    }
}


