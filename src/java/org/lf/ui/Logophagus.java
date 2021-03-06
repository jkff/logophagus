package org.lf.ui;

import com.thoughtworks.xstream.XStream;
import org.lf.plugins.PluginManager;
import org.lf.plugins.ProgramContext;
import org.lf.plugins.display.ViewScrollableLogPlugin;
import org.lf.plugins.tree.filelog.OpenLogFromFilePlugin;
import org.lf.plugins.tree.filtersubstr.FilterByCriteriaPlugin;
import org.lf.plugins.tree.highlight.HighlightRegexpPlugin;
import org.lf.plugins.tree.merge.MergeLogsPlugin;
import org.lf.plugins.tree.sidebyside.ViewSideBySidePlugin;
import org.lf.plugins.tree.splitbyfield.SplitByFieldPlugin;
import org.lf.services.ProgramProperties;
import org.lf.ui.components.pluginPanel.PluginPanel;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogPlugin;
import org.lf.ui.components.plugins.scrollablelog.extension.bookmarks.BookmarksPlugin;
import org.lf.ui.components.plugins.scrollablelog.extension.clipboard.CopyToClipboardPlugin;
import org.lf.ui.components.plugins.scrollablelog.extension.gotoparent.GoToParentLogPlugin;
import org.lf.ui.components.plugins.scrollablelog.extension.gototime.GoToTimePlugin;
import org.lf.ui.components.plugins.scrollablelog.extension.search.SearchPlugin;
import org.lf.ui.components.tree.PluginTree;
import org.lf.ui.components.tree.TreeContext;
import org.lf.ui.persistence.TreePersistencePlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;

public class Logophagus extends JFrame {
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

        pluginManager.addPlugin(new SearchPlugin());
        pluginManager.addPlugin(new BookmarksPlugin());
        pluginManager.addPlugin(new GoToParentLogPlugin());
        pluginManager.addPlugin(new GoToTimePlugin());
        pluginManager.addPlugin(new CopyToClipboardPlugin());

        pluginManager.addPlugin(new TreePersistencePlugin());
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
        PluginPanel pluginPanel = new PluginPanel(context.getDisplayPluginRepository());
        pluginPanel.setLayout(new BorderLayout());

        PluginTree pluginTree = context.getPluginTree();
        pluginTree.addTreeSelectionListener(pluginPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerSize(5);
        splitPane.setContinuousLayout(true);
        splitPane.setRightComponent(pluginPanel);

        JPanel treePanel = new JPanel(new BorderLayout());
        treePanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 4));
        treePanel.add(new JScrollPane(pluginTree));
        treePanel.setVisible(true);

        splitPane.setLeftComponent(treePanel);
        splitPane.setDividerLocation(250);

        this.setMinimumSize(new Dimension(800, 600));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setJMenuBar(createMenu(pluginTree));

        this.getContentPane().add(splitPane);
    }

    private JMenuBar createMenu(final PluginTree pluginTree) {
        JMenuBar res = new JMenuBar();
        
        JMenu menuFile = new JMenu("File");
        menuFile.getPopupMenu().setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JMenuItem fileOpen = new JMenuItem(new OpenLogFromFilePlugin().getOpenFileAction(new TreeContext(pluginTree)));

        JMenuItem fileClose = new JMenuItem(new AbstractAction("Close") {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        JMenuItem fileSaveState = new JMenuItem(new AbstractAction("Save state") {
            public void actionPerformed(ActionEvent evt) {
                XStream xs = context.getXstream();
                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(ProgramProperties.getUIStateFile());
                    xs.toXML(pluginTree.getRoot(), os);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(os != null) try { os.close(); } catch (IOException e) {}
                }
            }
        });
        JMenuItem fileRestoreState = new JMenuItem(new AbstractAction("Restore state") {
            public void actionPerformed(ActionEvent evt) {
                XStream xs = context.getXstream();
                InputStream is = null;
                try {
                    is = new FileInputStream(ProgramProperties.getUIStateFile());
                    pluginTree.setRoot(xs.fromXML(is));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(is != null) try { is.close(); } catch (IOException e) {}
                }
            }

            @Override
            public boolean isEnabled() {
                return ProgramProperties.getUIStateFile().exists();
            }
        });

        menuFile.add(fileOpen);
        menuFile.add(fileClose);
        menuFile.add(fileSaveState);
        menuFile.add(fileRestoreState);

        res.add(menuFile);

        return res;
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


