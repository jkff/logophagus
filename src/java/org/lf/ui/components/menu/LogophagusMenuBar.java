package org.lf.ui.components.menu;

import org.lf.plugins.analysis.filelog.FileBackedLogPlugin;
import org.lf.ui.model.AnalysisPluginsTreeModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;


public class LogophagusMenuBar extends JMenuBar implements Observer {
    private AnalysisPluginsTreeModel logsTreeModel;

    public LogophagusMenuBar(AnalysisPluginsTreeModel logsTreeModel) {
        this.logsTreeModel = logsTreeModel;
        initComponents();
    }

    private void initComponents() {
        JMenu menuFile = new JMenu("File");
        menuFile.getPopupMenu().setBorder(BorderFactory.createLineBorder(Color.GRAY));
        JMenuItem fileOpen = new JMenuItem("Open");
        fileOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                logsTreeModel.applyPluginForPath(new FileBackedLogPlugin(), null);
            }
        });

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

    @Override
    public void update(Observable arg0, Object arg1) {
        //nothing for the first time
    }

}
