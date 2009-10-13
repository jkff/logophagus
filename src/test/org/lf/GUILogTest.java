package test.org.lf;

import java.org.lf.parser.*;
import java.org.lf.util.Filter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;
import java.util.LinkedList;


public class GUILogTest extends JPanel
                             implements ActionListener, 
                                        PropertyChangeListener {

    private JButton startButton;
    private JButton endButton;
    private JButton prevButton;
    private JButton nextButton;
    private JProgressBar progressBar;
    private JTextArea taskOutput;
    
    private Task task;
    private static Log log;
    private static LinkedList<Record> result;
    private static Position cur;
	
    class Task extends SwingWorker<Void, Void> {
        private String command;
    	public Task() {
			super();
			command = new String("start");
		}

    	public Task(String command) {
			super();
			this.command = command;
		}

    	/*
         * Main task. Executed in background thread.
         */
        @Override
        public Void doInBackground() {
        	boolean directionForward = true;
			
        	try{
    	    	if(command =="next") {    
	    	    	directionForward = true;
	    		} else if("prev"==command) {
	    			directionForward = false;
	    		} else if("start"==command) {
	    			cur = log.getStart();
	    		} else if("end"==command) {
	    			cur = log.getEnd();
	    		}

	        	
	        	int progress = 0;
	        	setProgress(progress);
				Position tmp = cur;
				for(int i = 0; i < 10; ++i) {
					if (!tmp.equals(directionForward ? log.next(tmp) : log.prev(tmp)) ){
						tmp = (directionForward ? log.next(tmp) : log.prev(tmp));
						progress+=10;
						setProgress(progress);
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
				cur = tmp;
				
			}catch (Exception e) {
				System.out.print(e.getMessage());
			}
        	return null;
        }

        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
            startButton.setEnabled(true);
            endButton.setEnabled(true);
            prevButton.setEnabled(true);
            nextButton.setEnabled(true);

            setCursor(null);

            for (Record res: result){
            	taskOutput.append(res.toString());
            }
            taskOutput.append("Done!\n");
        }
    }

    public GUILogTest() {
        super(new BorderLayout());

        //Create UI.
        startButton = new JButton("Start");
        startButton.setActionCommand("start");
        startButton.addActionListener(this);

        endButton = new JButton("End");
        endButton.setActionCommand("end");
        endButton.addActionListener(this);

        prevButton = new JButton("Prev");
        prevButton.setActionCommand("prev");
        prevButton.addActionListener(this);

        nextButton = new JButton("Next");
        nextButton.setActionCommand("next");
        nextButton.addActionListener(this);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        taskOutput = new JTextArea(5, 20);
        taskOutput.setMargin(new Insets(5,5,5,5));
        taskOutput.setEditable(false);

        JPanel panel = new JPanel();
        panel.add(startButton);
        panel.add(endButton);
        panel.add(prevButton);
        panel.add(nextButton);
        panel.add(progressBar);
        
        add(panel, BorderLayout.PAGE_START);
        add(new JScrollPane(taskOutput), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    }

    /**
     * Invoked when the user presses button.
     */
    public void actionPerformed(ActionEvent evt) {
        result.clear();

    	setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    	nextButton.setEnabled(false);
		prevButton.setEnabled(false);
		startButton.setEnabled(false);
		endButton.setEnabled(false);
		
        //Instances of javax.swing.SwingWorker are not reusuable, so
        //we create new instances as needed.
		if("next"==evt.getActionCommand()) {
	    	task = new Task("next");
    	} else if("prev"==evt.getActionCommand()) {
			task = new Task("prev");
    	} else if("start"==evt.getActionCommand()) {
			task = new Task("start");
		} else if("end"==evt.getActionCommand()) {
			task = new Task("end");		        
		}
    	
        task.addPropertyChangeListener(this);
        task.execute();
    }

    /**
     * Invoked when task's progress property changes.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);
        } 
    }


    /**
     * Create the GUI and show it. As with all GUI code, this must run
     * on the event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Log View");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new GUILogTest();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

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

            log = testLog;
			result = new LinkedList<Record>();
			cur = log.getStart();
			
		}catch (Exception e) {
			System.out.print(e.getMessage());
		}

    	javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
