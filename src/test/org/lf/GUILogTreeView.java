package org.lf;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.lf.parser.FileBackedLog;
import org.lf.parser.FilteredLog;
import org.lf.parser.LineParser;
import org.lf.parser.Log;
import org.lf.parser.Record;
import org.lf.util.Filter;

public class GUILogTreeView extends JFrame implements TreeSelectionListener {
	private JTree jTree;
	DefaultMutableTreeNode rootNode;
	DefaultTreeModel treeModel;
	JPanel pluginPanel;
	
	private GUILogTreeView() {
		super("Log table");		
		setMinimumSize(new Dimension(700,300));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(createMenuBar());
		
	
		rootNode = new DefaultMutableTreeNode("All logs");
		treeModel = new DefaultTreeModel(rootNode);
		
		jTree = new JTree(treeModel);
		jTree.setRootVisible(false);

		jTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		jTree.addTreeSelectionListener(this);
		
		
		jTree.addMouseListener(new MouseAdapter() {
		     public void mousePressed(final MouseEvent e) {
		         final TreePath selPath = jTree.getPathForLocation(e.getX(), e.getY());
		         if(e.getClickCount() == 2) {
		        	 if (selPath==null){
		        		openFile();
		        	 } else {
		        		 new FilterSetUp((NodePlugin)selPath.getLastPathComponent());
		        	 }
		         } else if(e.getButton()==MouseEvent.BUTTON3){
		        	 
		        	 JMenuItem itemAdd = new JMenuItem("Add");

		        	 itemAdd.addActionListener(new ActionListener() {
		        		 public void actionPerformed(ActionEvent arg0) {
		        			 final TreePath selPath = jTree.getPathForLocation(e.getX(), e.getY());
		        			 if (selPath==null){
		        				 openFile();
		        			 } else {
		        				 new FilterSetUp((NodePlugin)selPath.getLastPathComponent());
		        			 }
		        		 }
		        	 });
		        	 
		        	 JMenuItem itemDelete = new JMenuItem("Delete");
		        	 itemDelete.addActionListener(new ActionListener() {
		        		 public void actionPerformed(ActionEvent arg0) {
		        			 final TreePath selPath = jTree.getPathForLocation(e.getX(), e.getY());
		        			 pluginPanel.removeAll();
		        			 pluginPanel.updateUI();
		        			 treeModel.removeNodeFromParent((NodePlugin)selPath.getLastPathComponent());
		        		 }
		        	 });
		        	 JPopupMenu popMenu = new JPopupMenu();
		        	 popMenu.add(itemAdd);
		        	 if (selPath != null) {
		        		 popMenu.add(itemDelete);
		        	 }
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
	
	private JMenuBar createMenuBar(){
		JMenuBar menuBar = new JMenuBar();
		
		JMenu menuFile = new JMenu("File");
		JMenuItem fileOpen = new JMenuItem("Open");
		fileOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openFile();
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
	
	private class FilterSetUp extends JFrame implements ActionListener{
		private NodePlugin parent;
		private JTextField text;
		
		FilterSetUp(NodePlugin parent) {
			super("Write filter string");
			this.parent = parent;
			this.addWindowListener(closeWindow);
			JPanel panel = new JPanel();
			panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
			JLabel label = new JLabel("Enter filter string:");
			text = new JTextField(20);
			
			JButton button = new  JButton("Ok");
			button.setActionCommand("ok");
			
			button.addActionListener(this);

			panel.add(label,BorderLayout.PAGE_START);
			panel.add(text,BorderLayout.CENTER);
			panel.add(button,BorderLayout.PAGE_END);
			
			add(panel);
			
			this.setAlwaysOnTop(true);
			this.setMinimumSize(new Dimension(300,120));
			this.setVisible(true);
			
		}

		public void actionPerformed(final ActionEvent e) {
			if (e.getActionCommand().equals("ok")){
				Log log = new FilteredLog(new Filter<Record>() {
					public boolean accepts(Record record) {
						return record.toString().contains(text.getText());
					}
					public String toString(){
						return text.getText();
					}
				}, parent.getLog());
				addNode( parent, new NodePlugin(log, new ScrollableLogTable(log)) );
				this.dispose();
			}
	
		}
	}
	
	private static WindowListener closeWindow = new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
            e.getWindow().dispose();
        }
    };
    
	void openFile(){
	    JFileChooser fileOpen = new JFileChooser();
        fileOpen.showOpenDialog(GUILogTreeView.this);
		File f = fileOpen.getSelectedFile();
		try {
			if (f != null ){
				Log log = new FileBackedLog(f.getAbsolutePath(), new LineParser());
				addNode(rootNode , new NodePlugin(log, new ScrollableLogTable(log)));
			}
		}catch (FileNotFoundException e){
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

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
		NodePlugin node = (NodePlugin)jTree.getLastSelectedPathComponent();
        if (node != null){
        	pluginPanel.add((JPanel)node.getPlugin());
        	pluginPanel.updateUI();
        }
	}



	public static void main(String[] args) {

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new GUILogTreeView();	
			}
		});
	}


	private class NodePlugin extends DefaultMutableTreeNode{
		
		private Log log;
		private LogPlugin plugin;
		
		public NodePlugin(Log log, LogPlugin plugin){
			super();
			this.log = log;
			this.plugin=plugin;
		}
		
		@Override
		public String toString() {
			return log.toString();
		}

		public Log getLog() {
			return log;
		}
		
		public LogPlugin getPlugin() {
			return plugin;
		}
		
	}


}