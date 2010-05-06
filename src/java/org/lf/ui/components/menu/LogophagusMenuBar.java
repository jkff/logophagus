package org.lf.ui.components.menu;

import org.lf.plugins.analysis.filelog.FileBackedLogPlugin;
import org.lf.ui.components.tree.AnalysisPluginTree;
import org.lf.ui.components.tree.TreeContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LogophagusMenuBar extends JMenuBar {
    private final AnalysisPluginTree tree;


    public LogophagusMenuBar(AnalysisPluginTree tree) {
        this.tree = tree;
        initComponents();
    }

    private void initComponents() {
        JMenu menuFile = new JMenu("File");
        menuFile.getPopupMenu().setBorder(BorderFactory.createLineBorder(Color.GRAY));
        JMenuItem fileOpen = new JMenuItem(new FileBackedLogPlugin().getActionFor(new TreeContext(tree)).getAction());


        JMenuItem fileClose = new JMenuItem("Close");
        fileClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        menuFile.add(fileOpen);
        menuFile.add(fileClose);
        this.add(menuFile);
    }

}
