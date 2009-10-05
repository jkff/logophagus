package test;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import parser.*;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
		Log fileLog = new FileBackedLog("text", new LineParser());
		
		// Log testLog = new SubstringFilteredLog(fileLog, "ERROR");
		
		//testLog.getStart();
		Position cur = fileLog.getStart();
		
		LineNumberReader in = new LineNumberReader(new InputStreamReader(System.in));
		
		while(true) {
			Position tmp = cur;
			for(int i = 0; i < 10; ++i) {
				Record rec = fileLog.readRecord(tmp);
				System.out.println(rec);
				tmp = fileLog.next(tmp);
			}

			
			System.out.println("Input a command (next/prev/start/end)");
			String command = in.readLine();
			if("next".equals(command)) {
				for(int i = 0; i < 10; ++i) cur = fileLog.next(cur);
			} else if("prev".equals(command)) {
				for(int i = 0; i < 10; ++i) cur = fileLog.prev(cur);
			} else if("start".equals(command)) {
				cur = fileLog.getStart();
			} else if("end".equals(command)) {
				cur = fileLog.getEnd();
			}
		}		
	}

}
