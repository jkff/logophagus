package org.lf;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.lf.plugins.all.FileBackedLogPlugin.FileBackedLogPlugin;
import org.lf.plugins.all.FilterBySubstringPlugin.FilterBySubstringPlugin;
import org.lf.plugins.all.SideBySidePlugin.SideBySidePlugin;
import org.lf.plugins.all.ViewLogAsTablePlugin.ViewLogAsTablePlugin;
import org.lf.plugins.all.ViewSideBySidePlugin.ViewSideBySidePlugin;
import org.lf.plugins.interfaces.*;
import org.lf.services.AnalysisPluginRepository;
import org.lf.services.DisplayPluginRepository;
import org.lf.services.PluginException;


public class GUILogTreeView extends JFrame implements TreeSelectionListener {
	private JTree jTree;
	DefaultMutableTreeNode rootNode;
	DefaultTreeModel treeModel;
	JPanel pluginPanel;
	
	private GUILogTreeView() {
		super("Logophagus");		
		setMinimumSize(new Dimension(700,300));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(generateMenuBar());
		
		rootNode = new DefaultMutableTreeNode("All logs");
		treeModel = new DefaultTreeModel(rootNode);
		
		jTree = new JTree(treeModel);
		jTree.setRootVisible(false);

		jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		
		jTree.addTreeSelectionListener(this);
		
		
		jTree.addMouseListener(new MouseAdapter() {
		     public void mousePressed(final MouseEvent e) {
		         if(e.getButton() == MouseEvent.BUTTON3 ){
		        	 JPopupMenu popMenu = generatePopUp();
		        	 popMenu.show(jTree, e.getX(), e.getY());
		         }
		     }
		});
		 
		
		JPanel treePanel = new JPanel(new BorderLayout());
		treePanel.add(new JScrollPane(jTree));

		pluginPanel = new JPanel(new BorderLayout());

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setRightComponent(pluginPanel);
		splitPane.setLeftComponent(treePanel);
		splitPane.setDividerLocation(250); 

		splitPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));

		add(splitPane);
		
		setVisible(true);
	}
	
	private JPopupMenu generatePopUp() {
		final TreePath[] selPaths = jTree.getSelectionPaths();

		JPopupMenu popMenu = new JPopupMenu();
		final List<Object> analysisArgs = new LinkedList<Object>();

		if (selPaths != null) { 
			for (int i=0; i < selPaths.length; ++i){
				DefaultMutableTreeNode cur = (DefaultMutableTreeNode)(selPaths[i].getLastPathComponent());
				NodeData data = (NodeData)(cur.getUserObject());
				analysisArgs.add(data.data);
			}
		}

		AnalysisPluginRepository apr = AnalysisPluginRepository.getInstance();
		List<AnalysisPlugin> availablePlugins = apr.getApplicableAnalysisPlugins(selPaths == null ? new Object[]{}:analysisArgs.toArray());

		for (int i=0; i < availablePlugins.size(); ++i){
			final AnalysisPlugin aPlugin = availablePlugins.get(i);
			JMenuItem itemPlugin = new JMenuItem(selPaths==null? "Add "+ aPlugin.getName():"Apply "+aPlugin.getName());
			itemPlugin.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					Object res = aPlugin.applyTo(analysisArgs.toArray());
					if (res != null) {
						DisplayPluginRepository dpr = DisplayPluginRepository.getInstance();
						List<DisplayPlugin> availabaleDisplays = dpr.getApplicableDisplayPlugins(res);
						addNode((analysisArgs.size() == 1 ? (DefaultMutableTreeNode)(selPaths[0].getLastPathComponent()) : rootNode) , new DefaultMutableTreeNode(new NodeData(res, availabaleDisplays.get(0).createView(res), aPlugin,availabaleDisplays.get(0))));
					}
				}
			});
			popMenu.add(itemPlugin);
		}

		if (selPaths != null) {
			popMenu.addSeparator();
			JMenuItem itemDelete = new JMenuItem("Delete");
			itemDelete.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					pluginPanel.removeAll();
					for (int i=0; i< selPaths.length; ++i){
						treeModel.removeNodeFromParent(((DefaultMutableTreeNode)selPaths[i].getLastPathComponent()));
					}
				}
			});

			popMenu.add(itemDelete);
		}
		return popMenu;
	}

	private JMenuBar generateMenuBar(){
		JMenuBar menuBar = new JMenuBar();
		
		JMenu menuFile = new JMenu("File");
		JMenuItem fileOpen = new JMenuItem("Open");
		fileOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				AnalysisPlugin logPlugin = new FileBackedLogPlugin();
				Object log = logPlugin.applyTo(new Object[]{});
				DisplayPlugin disp = DisplayPluginRepository.getInstance().getApplicableDisplayPlugins(log).get(0);
				addNode(rootNode, new DefaultMutableTreeNode(new NodeData(log, disp.createView(log), logPlugin, disp)));				
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
	
    private DefaultMutableTreeNode addNode(DefaultMutableTreeNode parent, DefaultMutableTreeNode child) {
        if (parent == null) 
            parent = rootNode;
        treeModel.insertNodeInto(child, parent, parent.getChildCount());
        jTree.scrollPathToVisible(new TreePath(child.getPath()));
        return child;
    }

	public void valueChanged(TreeSelectionEvent arg0) {
		pluginPanel.removeAll();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)jTree.getLastSelectedPathComponent();
		if (node != null) {
			NodeData nodeData = (NodeData)node.getUserObject(); 
			if (node != null){
				pluginPanel.add(nodeData.jComponent);
			}
		}
		pluginPanel.updateUI();

	}

	private class NodeData {
		final public Object data;
		final public JComponent jComponent;
		final public AnalysisPlugin aPlugin;
		final public DisplayPlugin dPlugin;

		NodeData(Object data, JComponent jComponent, AnalysisPlugin aPlugin, DisplayPlugin dPlugin){
			this.data = data;
			this.jComponent = jComponent;
			this.aPlugin = aPlugin;
			this.dPlugin = dPlugin;
		}
		
		public String toString() {
			return aPlugin.getName();
		}

	}

	public static void main(String[] args) {
		AnalysisPluginRepository apr = AnalysisPluginRepository.getInstance();
		DisplayPluginRepository dpr = DisplayPluginRepository.getInstance();
		try {
			apr.registerAnalysisPlugin(FileBackedLogPlugin.class);
			apr.registerAnalysisPlugin(FilterBySubstringPlugin.class);
			apr.registerAnalysisPlugin(SideBySidePlugin.class);
			dpr.registerDisplayPlugin(ViewLogAsTablePlugin.class);
			dpr.registerDisplayPlugin(ViewSideBySidePlugin.class);
		} catch (PluginException e) {
			System.out.println("Can't register plugin:" + FilterBySubstringPlugin.class);
			e.printStackTrace();
		}
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new GUILogTreeView();	
			}
		});
	}

}