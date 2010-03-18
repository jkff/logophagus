package org.lf.ui.components.dialog;


import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;

public class FieldSelectDialog extends JDialog {

    public FieldSelectDialog() {
        super((JFrame)null, "Setup", true);
        this.add(new JComboBox());
        this.add(new JButton("Ok"));
        this.add(new JButton("Cancel"));

    }


}
