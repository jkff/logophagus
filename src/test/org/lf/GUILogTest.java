package test.org.lf;

import org.lf.parser.*;
import org.lf.util.Filter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;
import java.util.LinkedList;

public class GUILogTest extends JPanel implements ActionListener,
		PropertyChangeListener {

	private JButton startButton1;

	private JButton endButton1;

	private JButton prevButton1;

	private JButton nextButton1;

	private JProgressBar progressBar1;

	private JTextArea taskOutput1;

	private JButton startButton2;

	private JButton endButton2;

	private JButton prevButton2;

	private JButton nextButton2;

	private JProgressBar progressBar2;

	private JTextArea taskOutput2;

	private Task task1;

	private Task task2;

	private static Log log1;

	private static Log log2;

	private static LinkedList<Record> result1;

	private static LinkedList<Record> result2;

	private static Position cur1;

	private static Position cur2;

	class Task extends SwingWorker<Void, Void> {
		private String command;

		private int window;

		public Task(String command, int window) {
			this.command = command;
			this.window = window;

		}

		/*
		 * Main task. Executed in background thread.
		 */
		@Override
		public Void doInBackground() {
			boolean directionForward = true;

			Position cur = (this.window == 1 ? cur1 : cur2);
			Log log = (window == 1 ? log1 : log2);
			try {
				if ("next".equals(command)) {
					directionForward = true;
				} else if ("prev".equals(command)) {
					directionForward = false;
				} else if ("start".equals(command)) {
					cur = log.getStart();
				} else if ("end".equals(command)) {
					cur = log.getEnd();
				}

				if (this.window == 1) {
					result1.clear();
				} else {
					result2.clear();
				}

				int progress1 = 0;

				setProgress(progress1);
				Position tmp = cur;
				for (int i = 0; i < 200; ++i) {
					if (!tmp.equals(directionForward ? log.next(tmp) : log
							.prev(tmp))) {
						tmp = (directionForward ? log.next(tmp) : log.prev(tmp));
						progress1 += 1;
						setProgress(progress1);
						if (directionForward) {

							(window == 1 ? result1 : result2).addLast(log
									.readRecord(tmp));
						} else {
							(window == 1 ? result1 : result2).addFirst(log
									.readRecord(tmp));
						}
					} else {
						directionForward = !directionForward;
						tmp = cur;
						--i;
					}

				}
				if (window == 1) {
					cur1 = tmp;
				} else {
					cur2 = tmp;
				}

			} catch (Exception e) {
				System.out.print(e.getMessage());
			}
			return null;
		}

		/*
		 * Executed in event dispatching thread
		 */
		@Override
		public void done() {
			(this.window == 1 ? startButton1 : startButton2).setEnabled(true);
			(this.window == 1 ? endButton1 : endButton2).setEnabled(true);
			(this.window == 1 ? prevButton1 : prevButton2).setEnabled(true);
			(this.window == 1 ? nextButton1 : nextButton2).setEnabled(true);

			for (Record res : (this.window == 1 ? result1 : result2)) {
				(this.window == 1 ? taskOutput1 : taskOutput2).append(res
						.toString());
			}
			(this.window == 1 ? taskOutput1 : taskOutput2).append("Done!\n");
		}
	}

	public GUILogTest() {
		super(new BorderLayout());

		// Create UI.
		startButton1 = new JButton("Start1");
		startButton1.setActionCommand("start1");
		startButton1.addActionListener(this);

		endButton1 = new JButton("End1");
		endButton1.setActionCommand("end1");
		endButton1.addActionListener(this);

		prevButton1 = new JButton("Prev1");
		prevButton1.setActionCommand("prev1");
		prevButton1.addActionListener(this);

		nextButton1 = new JButton("Next1");
		nextButton1.setActionCommand("next1");
		nextButton1.addActionListener(this);

		startButton2 = new JButton("Start2");
		startButton2.setActionCommand("start2");
		startButton2.addActionListener(this);

		endButton2 = new JButton("End2");
		endButton2.setActionCommand("end2");
		endButton2.addActionListener(this);

		prevButton2 = new JButton("Prev2");
		prevButton2.setActionCommand("prev2");
		prevButton2.addActionListener(this);

		nextButton2 = new JButton("Next2");
		nextButton2.setActionCommand("next2");
		nextButton2.addActionListener(this);

		progressBar1 = new JProgressBar(0, 100);
		progressBar1.setValue(0);
		progressBar1.setStringPainted(true);

		progressBar2 = new JProgressBar(0, 100);
		progressBar2.setValue(0);
		progressBar2.setStringPainted(true);

		taskOutput1 = new JTextArea(5, 20);
		taskOutput1.setMargin(new Insets(5, 5, 5, 5));
		taskOutput1.setEditable(false);

		taskOutput2 = new JTextArea(5, 20);
		taskOutput2.setMargin(new Insets(5, 5, 5, 5));
		taskOutput2.setEditable(false);

		JPanel panel = new JPanel();
		panel.add(startButton1);
		panel.add(endButton1);
		panel.add(prevButton1);
		panel.add(nextButton1);
		panel.add(progressBar1);

		panel.add(startButton2);
		panel.add(endButton2);
		panel.add(prevButton2);
		panel.add(nextButton2);
		panel.add(progressBar2);

		add(panel, BorderLayout.PAGE_START);
		add(new JScrollPane(taskOutput1), BorderLayout.WEST);
		add(new JScrollPane(taskOutput2), BorderLayout.EAST);

		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

	}

	/**
	 * Invoked when the user presses button.
	 */
	public void actionPerformed(ActionEvent evt) {

		// Instances of javax.swing.SwingWorker are not reusuable, so
		// we create new instances as needed.
		String command = evt.getActionCommand();

		if (command.endsWith("1")) {
			nextButton1.setEnabled(false);
			prevButton1.setEnabled(false);
			startButton1.setEnabled(false);
			endButton1.setEnabled(false);
			
			task1 = new Task(command.substring(0, command.length() - 1), 1);
			task1.addPropertyChangeListener(this);
			task1.execute();
		}
		if (command.endsWith("2")) {
			nextButton2.setEnabled(false);
			prevButton2.setEnabled(false);
			startButton2.setEnabled(false);
			endButton2.setEnabled(false);
			
			task2 = new Task(command.substring(0, command.length() - 1), 2);
			task2.addPropertyChangeListener(this);
			task2.execute();
		}

	}

	/**
	 * Invoked when task's progress property changes.
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("progress")) {
			if (evt.getSource().equals(this.task1)) {
				int progress = (Integer) evt.getNewValue();;
				progressBar1.setValue(progress);
			}
			if (evt.getSource().equals(this.task2)) {
				int progress = (Integer) evt.getNewValue();
				progressBar2.setValue(progress);
			}
		}
	}

	/**
	 * Create the GUI and show it. As with all GUI code, this must run on the
	 * event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("Log View");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		JComponent newContentPane = new GUILogTest();
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {

		try {
			log1 = new FileBackedLog("test3", new LineParser());

			log2 = new FilteredLog(new Filter<Record>() {
				public boolean accepts(Record record) {
					return record.toString().contains("123");
				}
			}, log1);

			result1 = new LinkedList<Record>();
			cur1 = log1.getStart();
			result2 = new LinkedList<Record>();
			cur2 = log2.getStart();

		} catch (Exception e) {
			System.out.print(e.getMessage());
		}

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}
