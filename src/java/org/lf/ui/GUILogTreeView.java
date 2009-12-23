package org.lf.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.lf.plugins.AnalysisPlugin;
import org.lf.plugins.display.*;
import org.lf.plugins.analysis.*;
import org.lf.plugins.*;
import org.lf.services.AnalysisPluginRepository;
import org.lf.services.DisplayPluginRepository;
import org.lf.services.PluginException;
import org.lf.ui.control.PopupOnClickTree;


public class GUILogTreeView extends JFrame {
	private JTree jTree;
	private PluginPanel pluginPanel = new PluginPanel();
	private DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("root");
	private DefaultTreeModel pluginTreeModel = new DefaultTreeModel(rootNode);
	
	private GUILogTreeView() {
		super("Logophagus");		
		setMinimumSize(new Dimension(700,300));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(generateMenuBar());
		
		jTree = new JTree(pluginTreeModel);
		jTree.setRootVisible(false);
		
		jTree.addTreeSelectionListener(pluginPanel);
		
		jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
				
		// TODO: Split PopupOnClickTree into a generic
        // ShowPopupOnRightClick class that, on right click,
        // shows a popup generated by someone else,
        // and a MakePopupForSelection class that can generate a
        // popup according to things selected in the tree.
<<<<<<< local
		jTree.addMouseListener(new ShowPopupOnRightClick(jTree,
                new MakePopupForSelection(jTree.getSelectionPaths())));
=======
//		jTree.addMouseListener(new ShowPopupOnRightClick(jTree,
//                new MakePopupForSelection(jtree.getSelectionPaths()));
>>>>>>> other
		 
		
		JPanel treePanel = new JPanel(new BorderLayout());
		treePanel.add(new JScrollPane(jTree));

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setRightComponent(pluginPanel);
		splitPane.setLeftComponent(treePanel);
		splitPane.setDividerLocation(250); 

		splitPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));

		add(splitPane);
		
		setVisible(true);
	}
	

	private JMenuBar generateMenuBar(){
		JMenuBar menuBar = new JMenuBar();
		
		JMenu menuFile = new JMenu("File");
		JMenuItem fileOpen = new JMenuItem("Open");
		fileOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				AnalysisPlugin logPlugin = new FileBackedLogPlugin();
				Object log = logPlugin.applyTo(new Object[]{});
				DisplayPlugin disp = DisplayPluginRepository.getApplicablePlugins(log).get(0);
				pluginTreeModel.insertNodeInto(rootNode, new DefaultMutableTreeNode(new NodeData(log, disp.createView(log))), rootNode.getChildCount());				
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
		menuBar.add(menuFile);
		return menuBar;
	}
	
	public static void main(String[] args) {
		try {
			AnalysisPluginRepository.register(FileBackedLogPlugin.class);
			AnalysisPluginRepository.register(FilterBySubstringPlugin.class);
			AnalysisPluginRepository.register(SideBySidePlugin.class);
			DisplayPluginRepository.register(ViewLogAsTablePlugin.class);
			DisplayPluginRepository.register(ViewSideBySidePlugin.class);
		} catch (PluginException e) {
			System.out.println("Can't register plugin");
			e.printStackTrace();
		}
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new GUILogTreeView();	
			}
		});
	}

}