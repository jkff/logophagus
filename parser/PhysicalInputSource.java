package parser;

import java.nio.*;
import java.nio.channels.*;
import java.util.LinkedList;
import java.util.*;
import java.util.StringTokenizer;
import java.io.*;


public class PhysicalInputSource {
	private FileChannel myFC;
	private String myString;
	private long myBlockSize = 200;
	private long myPosition;
	private long myMaxFilePosition;
	private LinkedList<String> myFieldNames = new LinkedList<String>();
	
	private void load(long start, long end) throws IOException {
		MappedByteBuffer mbb = myFC.map(FileChannel.MapMode.READ_ONLY , start, end);
		mbb.order(ByteOrder.nativeOrder());
		myString = mbb.asCharBuffer().toString();
	}
	
	public PhysicalInputSource(String fileName) throws IOException {
		myPosition = 0;
		RandomAccessFile inFile = new RandomAccessFile(fileName ,"r");
		myMaxFilePosition = inFile.length();
		myBlockSize = myMaxFilePosition > myBlockSize ? myBlockSize : myMaxFilePosition ;
		myFC = inFile.getChannel();
		load(myPosition, myBlockSize);
		//System.out.println(myString);
	}
	
	public FileBackedLog getLog(){
		StringTokenizer records = new StringTokenizer(myString,"\n");
		StringTokenizer fieldsData = new StringTokenizer(records.nextToken()," \t");
		if (myFieldNames.size() == 0) {
			while (fieldsData.hasMoreElements()){
				myFieldNames.add(fieldsData.nextToken());
			}
		}
		
		LinkedList<Map<String,String>> logData = new LinkedList<Map<String,String>>();;
		while (records.hasMoreElements()){
			fieldsData = new StringTokenizer(records.nextToken()," \t");
			Map<String,String> fields = new HashMap<String,String>();
			for (String name:myFieldNames){
				if (fieldsData.hasMoreElements()){
					fields.put(name, fieldsData.nextToken());
				}
			}
			
			if (fields.size() == myFieldNames.size()){
				logData.add(fields);
			} else {
				break;
			}
		}
		
		//System.out.println(logData.toString());
		FileBackedLog r = new FileBackedLog(this);
		r.myLog = logData;
		return r; 
	}

	boolean shiftToNext(){
		if ((myPosition + myBlockSize) < myMaxFilePosition){
			//for the first time
			myPosition = myPosition+myBlockSize;   
			load(myPosition, myPosition + myBlockSize );
			return true;
		} else {
			return false;
		}
	}
	
	boolean shiftToPrev(){
		if (myPosition == 0) {
			return false;
		} else {
			if ((myPosition - myBlockSize) < 0){
				//for the first time
				myPosition = 0;   
			} else {
				myPosition = myPosition - myBlockSize;   				
			}
			load(myPosition, myPosition + myBlockSize );
			return true;
			
		}
	}

	void jumpToBegin(){
		load(0, myBlockSize);
	}

	void jumpToEnd(){
		load((myMaxFilePosition - myBlockSize), myMaxFilePosition);
		
	}
}