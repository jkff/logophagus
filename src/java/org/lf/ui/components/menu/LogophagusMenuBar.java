package org.lf.ui.components.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.lf.plugins.analysis.filelog.FileBackedLogPlugin;
import org.lf.ui.model.LogsHierarchy;



public class LogophagusMenuBar extends JMenuBar implements Observer{
    private LogsHierarchy logsTree;

    public LogophagusMenuBar(LogsHierarchy logsTree) {
        this.logsTree = logsTree;
        this.logsTree.addObserver(this);
        initComponents();
    }

    private void initComponents() {
        JMenu menuFile = new JMenu("File");
        JMenuItem fileOpen = new JMenuItem("Open");
        fileOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                logsTree.applyPluginForPath(new FileBackedLogPlugin(), null);
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
