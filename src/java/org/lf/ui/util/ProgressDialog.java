package org.lf.ui.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProgressDialog extends JDialog {
    private final AtomicBoolean canceled = new AtomicBoolean(false);
    private final JProgressBar progressBar;

    public ProgressDialog(Window owner, String title, String note, ModalityType modality) {
        super(owner, title, modality);

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JLabel noteLabel = new JLabel(note);
        noteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        progressBar = new JProgressBar(0, 1000);
        progressBar.setValue(0);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        final JButton cancelButton = new JButton(new AbstractAction("Cancel") {
            public void actionPerformed(ActionEvent e) {
                canceled.set(true);
            }
        });
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        BoxLayout layout = new BoxLayout(getContentPane(), BoxLayout.Y_AXIS);

        getContentPane().setLayout(layout);
        getContentPane().add(noteLabel);
        getContentPane().add(progressBar);
        getContentPane().add(cancelButton);
        pack();

        Dimension dim = owner==null ? Toolkit.getDefaultToolkit().getScreenSize() : owner.getSize();
        setLocationRelativeTo(owner);
        setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);
    }

    public boolean isCanceled() {
        return canceled.get();
    }

    public void setProgress(double donePart) {
        progressBar.setValue((int)(1000*donePart));
    }
}
