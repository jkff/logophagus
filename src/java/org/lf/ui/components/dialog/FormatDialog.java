package org.lf.ui.components.dialog;

import org.joda.time.format.DateTimeFormat;
import org.lf.logs.Field;
import org.lf.logs.Format;
import org.lf.ui.util.GUIUtils;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FormatDialog extends JDialog {
    private final ControllableListView<String> fieldsView;
    private final JComboBox timePretenders;
    private final JTextField timeTemplateField;
    private final JButton okButton;
    private boolean isOkPressed = false;

    public FormatDialog() {
        super((JFrame)null, "Format creation");
        Box contentBox = Box.createVerticalBox();
        contentBox.setBorder( BorderFactory.createEmptyBorder(12,12,12,12));
        this.setContentPane(contentBox);

        JLabel forFieldsLabel = new JLabel("Add fields from record considering their order:");
        Box box2 = Box.createHorizontalBox();
        box2.add(forFieldsLabel);
        box2.add(Box.createHorizontalGlue());

        fieldsView = new ControllableListView<String>();
        fieldsView.setAddButtonActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String fieldName = JOptionPane.showInputDialog(null, "Enter field name",
                        "Enter field name", JOptionPane.QUESTION_MESSAGE );
                fieldsView.getListModel().add(fieldName);
            }
        });

        fieldsView.getListModel().addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent listDataEvent) {
                updateComponents();
            }
            @Override
            public void intervalRemoved(ListDataEvent listDataEvent) {
                updateComponents();
            }
            @Override
            public void contentsChanged(ListDataEvent listDataEvent) {
                updateComponents();
            }
        });

        this.add(box2);
        this.add(Box.createVerticalStrut(5));
        this.add(fieldsView);

        this.add(Box.createVerticalStrut(12));


        JLabel forTimeLabel = new JLabel("Select time field:");
        Box box3 = Box.createHorizontalBox();
        box3.add(forTimeLabel);
        box3.add(Box.createHorizontalGlue());

        timePretenders = new JComboBox(new AdapterComboBoxModel(fieldsView.getListModel()));
        GUIUtils.fixMaxHeightSize(timePretenders);
        this.add(box3);
        this.add(Box.createVerticalStrut(5));
        this.add(timePretenders);

        this.add(Box.createVerticalStrut(12));

        JLabel forTimeTemplateLabel = new JLabel("Specify time format:");
        Box box4 = Box.createHorizontalBox();
        box4.add(forTimeTemplateLabel);
        box4.add(Box.createHorizontalGlue());


        timeTemplateField = new JTextField();
        timeTemplateField.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent caretEvent) {
                updateComponents();
            }
        });
        
        GUIUtils.fixMaxHeightSize(timeTemplateField);

        this.add(box4);
        this.add(Box.createVerticalStrut(5));
        this.add(timeTemplateField);

        this.add(Box.createVerticalStrut(12));


        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                isOkPressed = true;
                dispose();
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

        GUIUtils.makeSameWidth(new JComponent[]{okButton, cancelButton});
        Box box5 = Box.createHorizontalBox();
        box5.add(okButton);
        box5.add(Box.createHorizontalStrut(12));
        box5.add(cancelButton);
        GUIUtils.fixMaxHeightSize(box5);
        
        this.add(box5);

        this.pack();
        this.setModal(true);
        updateComponents();
        this.setVisible(false);
    }

    public Format showDialog() {
        setVisible(true);
        if (!isOkPressed) return null;
        Field[] fields = new Field[fieldsView.getListModel().getSize()];
        for(int i = 0; i < fields.length ; ++i)
            fields[i] = new Field(fieldsView.getListModel().getElementAt(i));

        int timeIndex = timePretenders.getSelectedIndex() == 0 || timePretenders.getSelectedIndex()== -1 ?
                -1: timePretenders.getSelectedIndex() - 1;

        return new  Format(fields, timeIndex, timeIndex == -1? null : DateTimeFormat.forPattern(timeTemplateField.getText()));
    }


    private void updateComponents() {
        okButton.setEnabled(true);
        timePretenders.setEnabled(true);
        timeTemplateField.setEnabled(true);

        if (fieldsView.getListModel().getSize() == 0) {
            okButton.setEnabled(false);
            timePretenders.setEnabled(false);
            timeTemplateField.setEnabled(false);
        } else {
            Object selected = ((AdapterComboBoxModel)timePretenders.getModel()).selectedObject;
            if (selected == null || selected == AdapterComboBoxModel.defaultElement)
                timeTemplateField.setEnabled(false);
            else
                if (timeTemplateField.getText() == null || timeTemplateField.getText().length() == 0)
                    okButton.setEnabled(false);
                else {
                    try {
                        if (DateTimeFormat.forPattern(timeTemplateField.getText()) == null)
                            okButton.setEnabled(false);
                    } catch(IllegalArgumentException e) {
                        okButton.setEnabled(false);
                    }
                }

        }

    }

    private class AdapterComboBoxModel implements ComboBoxModel {
        private final ListModel listModel;
        Object selectedObject = null;
        public final static String defaultElement = "<<NO TIME FIELD>>";

        AdapterComboBoxModel(ListModel listModel) {
            this.listModel = listModel;
        }
        
        @Override
        public void setSelectedItem(Object o) {
            this.selectedObject = o;
            updateComponents();
        }

        @Override
        public Object getSelectedItem() {
            return selectedObject;
        }

        @Override
        public int getSize() {
            return listModel.getSize() + 1;
        }

        @Override
        public Object getElementAt(int i) {
            return i == 0 ?  defaultElement: listModel.getElementAt(i - 1);
        }

        @Override
        public void addListDataListener(ListDataListener listDataListener) {
            listModel.addListDataListener(listDataListener);
        }

        @Override
        public void removeListDataListener(ListDataListener listDataListener) {
            listModel.removeListDataListener(listDataListener);
        }
    }
}
