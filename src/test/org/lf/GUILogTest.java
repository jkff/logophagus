package test.org.lf;

import org.lf.parser.*;
import org.lf.util.Filter;

import java.awt.*;
import javax.swing.*;
import java.io.IOException;


public class GUILogTest  {
	private Log log1;
	private Log log2;
	
	GUILogTest(){
		try {
			Log base = new FileBackedLog("test3", new LineParser());

			log1 = new FilteredLog(new Filter<Record>() {
				public boolean accepts(Record record) {
					return record.toString().contains("123");
				}
			}, base);
			
			log2 = new FilteredLog(new Filter<Record>() {
				public boolean accepts(Record record) {
					return record.toString().contains("124");
				}
			}, base);
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		JFrame jfrm = new JFrame("Log table");
		jfrm.setLayout(new GridLayout(1,2));
		jfrm.setMinimumSize(new Dimension(700,300));
		jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		ScrollableLogTable logGUI1 = new ScrollableLogTable(log1);
		ScrollableLogTable logGUI2 = new ScrollableLogTable(log2);
		jfrm.add(logGUI1);
		jfrm.add(logGUI2);
		jfrm.setVisible(true);
		
	}
	public static void main(String[] args) {

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new GUILogTest();	
			}
		});
	}
}
