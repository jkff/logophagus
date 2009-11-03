package test.org.lf;

import org.lf.parser.*;
import org.lf.util.Filter;

import java.awt.*;
import javax.swing.*;

import java.io.FileNotFoundException;
import java.io.IOException;


public class GUILogTest  {
	private Log log1;
	private Log log2;
	
	GUILogTest(){
//		try {
//			Log base = new FileBackedLog("test3", new LineParser());
//
//			log1 = new FilteredLog(new Filter<Record>() {
//				public boolean accepts(Record record) {
//					return record.toString().contains("12");
//				}
//			}, base);
//			
//			log2 = new FilteredLog(new Filter<Record>() {
//				public boolean accepts(Record record) {
//					return record.toString().contains("124");
//				}
//			}, base);
//			
//			log2 = base;
			JFrame jfrm = new JFrame("Log table");
			jfrm.setLayout(new GridLayout(1,0));
			jfrm.setMinimumSize(new Dimension(700,300));
			jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			jfrm.add(new GUILogTreeView());
			jfrm.setVisible(true);
		
//		}catch (FileNotFoundException e){
//			e.printStackTrace();
//		}
//		catch (IOException e) {
//			e.printStackTrace();
//		}


		
	}
	public static void main(String[] args) {

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new GUILogTest();	
			}
		});
	}
}
