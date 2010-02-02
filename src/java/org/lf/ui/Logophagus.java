package org.lf.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.tree.TreeSelectionModel;

import org.lf.plugins.display.*;
import org.lf.plugins.analysis.*;
import org.lf.services.AnalysisPluginRepository;
import org.lf.services.DisplayPluginRepository;
import org.lf.services.PluginException;
import org.lf.ui.components.menu.LogophagusMenuBar;
import org.lf.ui.components.popup.TreeRightClickPopup;
import org.lf.ui.components.tree.LogsHierarchyView;
import org.lf.ui.components.pluginPanel.PluginPanel;
import org.lf.ui.model.LogsHierarchy;


public class Logophagus extends JFrame {
	private LogsHierarchy logsHierarchy;
	
	private JMenuBar menuBar;
	private LogsHierarchyView logsTree;
	private PluginPanel pluginPanel;
	
	private Logophagus() {
		super("Logophagus");
		logsHierarchy = new LogsHierarchy();
		initComponents();
	}
	
	private void initComponents() {
		this.setMinimumSize(new Dimension(700,300));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setJMenuBar(getLogophagusMenuBar());		

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setDividerSize(5);
		splitPane.setContinuousLayout(true);
		splitPane.setRightComponent(getPluginPanel());
		splitPane.setLeftComponent(getLogsHierarchyView());
		splitPane.setDividerLocation(250); 
		splitPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
		add(splitPane);
		setVisible(true);
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
		logsTree.addMouseListener(new MouseAdapter() {
		     public void mousePressed(final MouseEvent e) {
		         if(e.getButton() == MouseEvent.BUTTON3 ){
		        	 JPopupMenu popMenu = new TreeRightClickPopup(logsHierarchy, logsTree.getSelectionPaths());
		        	 popMenu.show(logsTree, e.getX(), e.getY());
		         }
		     }
		});
		logsTree.addTreeSelectionListener(getPluginPanel());
		return logsTree;
	}

	public static void main(String[] args) {
		try {
			AnalysisPluginRepository.register(FileBackedLogPlugin.class);
			AnalysisPluginRepository.register(FilterBySubstringPlugin.class);
			AnalysisPluginRepository.register(SideBySidePlugin.class);
            AnalysisPluginRepository.register(HighlightRegexPlugin.class);
			DisplayPluginRepository.register(ViewLogAsTablePlugin.class);
			DisplayPluginRepository.register(ViewSideBySidePlugin.class);

		} catch (PluginException e) {
			System.out.println("Can't register plugin:" + e);
			e.printStackTrace();
		}
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Logophagus();	
			}
		});
	}

	
}