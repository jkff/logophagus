package org.lf;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.lf.parser.*;
import org.lf.util.Filter;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			Log fileLog = new FileBackedLog("test3", new LineParser());
			
			Log testLog = new FilteredLog(
                    new Filter<Record>() {
                        public boolean accepts(Record record) {
                            return record.toString().contains("123");
                        }
                    },
                    fileLog);

            Log log = testLog;

			//testLog.getStart();
			Position cur = log.getStart();
			
			LineNumberReader in = new LineNumberReader(new InputStreamReader(System.in));
			
			while(true) {
				Position tmp = cur;
				for(int i = 0; i < 10; ++i) {
					Record rec = log.readRecord(tmp);
					System.out.print(rec);
					tmp = log.next(tmp);
				}
	
				System.out.println("Done");
				System.out.println("Input a command (next/prev/start/end)");
				String command = in.readLine();
				if("next".equals(command)) {
					for(int i = 0; i < 10; ++i) cur = log.next(cur);
				} else if("prev".equals(command)) {
					for(int i = 0; i < 10; ++i) cur = log.prev(cur);
				} else if("start".equals(command)) {
					cur = log.getStart();
				} else if("end".equals(command)) {
					cur = log.getEnd();
					for(int i = 0; i < 10; ++i) {
						cur = log.prev(cur);
					}
				}	
			}
		}catch (Exception e) {
			System.out.print(e.getMessage());
		}
	}
}