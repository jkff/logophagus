package org.lf;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.lf.parser.*;
import org.lf.util.Filter;
import java.util.LinkedList;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			Log fileLog = new FileBackedLog("test3", new LineParser());
			
			Log testLog = new FilteredLog(
                    fileLog, new Filter<Record>() {
                        public boolean accepts(Record record) {
                            return record.toString().contains("123");
                        }
                    }
            );

            Log log = testLog;
			
			LineNumberReader in = new LineNumberReader(new InputStreamReader(System.in));
			
			LinkedList<Record> result = new LinkedList<Record>();
			Position cur = log.getStart();
			boolean directionForward = true;
			
			while(true) {
				Position tmp = cur;
				for(int i = 0; i < 10; ++i) {
					if (!tmp.equals(directionForward ? log.next(tmp) : log.prev(tmp)) ){
						tmp = (directionForward ? log.next(tmp) : log.prev(tmp));
						System.out.print(".");
						if (directionForward){
							result.addLast(log.readRecord(tmp));
						} else {
							result.addFirst(log.readRecord(tmp));
						}
					} else {
						directionForward = !directionForward;
						tmp = cur;
						--i;	
					}
					
				}
				System.out.println();
				for(Record r :result){
					System.out.print(r);
				}
				
				result.clear();
				
				System.out.println("Done");
				System.out.println("Input a command (next/prev/start/end)");
				String command = in.readLine();
				if("next".equals(command)) {
					for(int i = 0; i < 10; ++i) 
						cur = log.next(cur);
				} else if("prev".equals(command)) {
					for(int i = 0; i < 10; ++i) 
						cur = log.prev(cur);
				} else if("start".equals(command)) {
					cur = log.getStart();
				} else if("end".equals(command)) {
					cur = log.getEnd();
				}	
			}
		}catch (Exception e) {
			System.out.print(e.getMessage());
		}
	}
}