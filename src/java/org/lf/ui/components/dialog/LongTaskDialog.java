package org.lf.ui.components.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicBoolean;

public class LongTaskDialog extends JDialog {
    private final AtomicBoolean canceled = new AtomicBoolean(false);
    private final JLabel noteLabel;
    private final String note;

    public LongTaskDialog(Window owner, String title, String note, Dialog.ModalityType modality) {
        super(owner, title, modality);

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        this.note = note;

        this.noteLabel = new JLabel(note);
        noteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        final JButton cancelButton = new JButton(new AbstractAction("Cancel") {
            public void actionPerformed(ActionEvent e) {
                canceled.set(true);
            }
        });
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        BoxLayout layout = new BoxLayout(getContentPane(), BoxLayout.Y_AXIS);

        getContentPane().setLayout(layout);
        getContentPane().add(noteLabel);
        getContentPane().add(cancelButton);
        pack();

        Dimension dim = owner == null ? Toolkit.getDefaultToolkit().getScreenSize() : owner.getSize();
        setLocationRelativeTo(owner);
        setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);


        setVisible(false);
    }

    public boolean isCanceled() {
        return canceled.get();
    }

    public void setProgressText(String progress) {
        noteLabel.setText(note + (progress.isEmpty() ? "..." : (" - " + progress)));
    }
}
