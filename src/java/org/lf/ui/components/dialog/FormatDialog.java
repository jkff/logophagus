package org.lf.ui.components.dialog;

import org.joda.time.format.DateTimeFormat;
import org.lf.logs.Field;
import org.lf.logs.Format;
import org.lf.ui.util.GUIUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class FormatDialog extends JDialog implements PropertyChangeListener {
    private final FormatComponent formatComponent;
    private final JButton okButton;
    private boolean isOkPressed = false;

    public FormatDialog() {
        super((JFrame) null, "Format creation");
        Box contentBox = Box.createVerticalBox();
        contentBox.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        this.setContentPane(contentBox);

        this.formatComponent = new FormatComponent();
        this.formatComponent.addChangeListener(this);

        this.add(formatComponent);

        okButton = new JButton("OK");
        okButton.setEnabled(formatComponent.isValidFormat());
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                isOkPressed = true;
                setVisible(false);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                isOkPressed = false;
                dispose();
            }
        });

        GUIUtils.makeSameWidth(okButton, cancelButton);
        Box okCancelBox = Box.createHorizontalBox();
        okCancelBox.add(okButton);
        okCancelBox.add(Box.createHorizontalStrut(12));
        okCancelBox.add(cancelButton);
        GUIUtils.fixMaxHeightSize(okCancelBox);

        this.add(okCancelBox);

        this.pack();
        this.setModal(true);
        this.setVisible(false);
    }

    public Format showDialog() {
        setVisible(true);
        if (!isOkPressed) return null;
        Field[] fields = new Field[formatComponent.getFieldCount()];
        for (int i = 0; i < fields.length; ++i)
            fields[i] = new Field(formatComponent.getFieldName(i));

        int timeIndex = formatComponent.getTimeIndex();

        return new Format(fields, timeIndex, timeIndex == -1 ? null : DateTimeFormat.forPattern(formatComponent.getTimeTemplate()));
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        okButton.setEnabled(formatComponent.isValidFormat());
    }
}
