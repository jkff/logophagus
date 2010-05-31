package org.lf.parser.regex;

import org.joda.time.format.DateTimeFormat;
import org.lf.logs.Field;
import org.lf.logs.Format;
import org.lf.ui.components.dialog.FormatComponent;
import org.lf.ui.util.GUIUtils;
import org.lf.util.Triple;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexpFormatDialog extends JDialog implements PropertyChangeListener {
    private final FormatComponent formatComponent;
    private final JButton okButton;
    private final JTextField regexpTextField;
    private final SpinnerModel spinnerModel;


    private boolean isOkPressed = false;

    public RegexpFormatDialog() {
        super((JFrame) null, "Format creation");
        Box contentBox = Box.createVerticalBox();
        contentBox.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        this.setContentPane(contentBox);

        JLabel regexpTextLabel = new JLabel("Enter pattern:");
        regexpTextField = new JTextField();
        regexpTextField.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent caretEvent) {
                updateComponents();
            }
        });

        Box regexpBox = Box.createHorizontalBox();
        regexpBox.add(regexpTextLabel);
        regexpBox.add(regexpTextField);

        this.add(regexpBox);
        this.add(Box.createVerticalStrut(12));

        JLabel spinnerLabel = new JLabel("Set lines per record: ");
        spinnerModel = new SpinnerNumberModel(
                1, //initial value
                1, //min
                100, //max
                1  //step
        );
        JSpinner spinner = new JSpinner(spinnerModel);

        Box spinnerBox = Box.createHorizontalBox();
        spinnerBox.add(spinnerLabel);
        spinnerBox.add(spinner);

        this.add(spinnerBox);
        this.add(Box.createVerticalStrut(12));

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

    private void updateComponents() {
        boolean validRegexp = (regexpTextField.getText().length() != 0);
        if (validRegexp)
            try {
                Pattern.compile(regexpTextField.getText());
            } catch (PatternSyntaxException pse) {
                validRegexp = false;
            }

        okButton.setEnabled(formatComponent.isValidFormat() && validRegexp);
    }

    public Triple<String, Integer, Format> showDialog() {
        setVisible(true);
        if (!isOkPressed) return null;
        Field[] fields = new Field[formatComponent.getFieldCount()];
        for (int i = 0; i < fields.length; ++i)
            fields[i] = new Field(formatComponent.getFieldName(i));

        int timeIndex = formatComponent.getTimeIndex();

        Format resFormat = new Format(fields, timeIndex, timeIndex == -1 ? null : DateTimeFormat.forPattern(formatComponent.getTimeTemplate()));
        return new Triple<String, Integer, Format>(regexpTextField.getText(), (Integer) spinnerModel.getValue(), resFormat);
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updateComponents();
    }

}
