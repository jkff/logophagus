package org.lf.ui.components.plugins.scrollablelog.extension.gototime;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.lf.parser.Position;
import org.lf.ui.components.plugins.scrollablelog.ScrollableLogModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * Created on: 05.06.2010 18:19:30
 */
public class GoToTimeDialog extends JDialog {
    private ScrollableLogModel model;
    private JProgressBar progressBar;

    public GoToTimeDialog(ScrollableLogModel model) {
        this.model = model;

        Box contentBox = Box.createVerticalBox();
        contentBox.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        this.setContentPane(contentBox);

        Box inputBox = Box.createHorizontalBox();
        contentBox.add(inputBox);
        inputBox.add(new JLabel("Time (yyyy-MM-dd HH:mm:ss):"));
        final JTextField timeField = new JTextField();
        timeField.setText("1970-01-01 00:00:00");
        inputBox.add(timeField);

        Box buttonsBox = Box.createHorizontalBox();
        contentBox.add(buttonsBox);

        buttonsBox.add(new JButton(new AbstractAction("OK") {
            public void actionPerformed(ActionEvent e) {
                GoToTimeDialog.this.go(
                        DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
                                .parseDateTime(timeField.getText()));
            }
        }));
        buttonsBox.add(new JButton(new AbstractAction("Cancel") {
            public void actionPerformed(ActionEvent e) {
                GoToTimeDialog.this.setVisible(false);
            }
        }));

        this.progressBar = new JProgressBar();
        this.progressBar.setValue(0);
        contentBox.add(this.progressBar);


        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);

        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.pack();
        this.setModal(true);
        this.setResizable(false);
    }

    private void go(final DateTime time) {
        progressBar.setIndeterminate(true);
        new Thread() {
            @Override
            public void run() {
                try {
                    Position pos = model.getLog().findNearestBeforeTime(time);
                    if (pos == null) {
                        setVisible(false);
                        JOptionPane.showMessageDialog(null, "Failed to find record near given time");
                        return;
                    }
                    model.shiftTo(pos, new Runnable() {
                        @Override
                        public void run() {
                            setVisible(false);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
