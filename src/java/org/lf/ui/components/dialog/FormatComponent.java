package org.lf.ui.components.dialog;

import org.joda.time.format.DateTimeFormat;
import org.lf.ui.components.common.ControllableListView;
import org.lf.ui.util.GUIUtils;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class FormatComponent extends JPanel {
    private final ControllableListView<String> fieldsView;
    private final JComboBox timePretenders;
    private final JTextField timeTemplateField;
    private boolean validFormat = false;
    private final PropertyChangeSupport changeSupport;

    public FormatComponent() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel forFieldsLabel = new JLabel("Add fields from record considering their order:");
        Box box2 = Box.createHorizontalBox();
        box2.add(forFieldsLabel);
        box2.add(Box.createHorizontalGlue());

        fieldsView = new ControllableListView<String>();
        fieldsView.setAddButtonActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String fieldName = JOptionPane.showInputDialog(null, "Enter field name",
                        "Enter field name", JOptionPane.QUESTION_MESSAGE);
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
        this.changeSupport = new PropertyChangeSupport(this);
        updateComponents();
    }

    public void addChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public boolean isValidFormat() {
        return validFormat;
    }

    public int getFieldCount() {
        return fieldsView.getListModel().getSize();
    }

    public String getFieldName(int i) {
        return fieldsView.getListModel().getElementAt(i);
    }

    public int getTimeIndex() {
        return (timePretenders.getSelectedIndex() == 0 || timePretenders.getSelectedIndex() == -1) ?
                -1 : timePretenders.getSelectedIndex() - 1;
    }

    public String getTimeTemplate() {
        return timeTemplateField.getText();
    }

    private void updateComponents() {
        boolean isValidFormatOld = validFormat;
        validFormat = true;
        timePretenders.setEnabled(true);
        timeTemplateField.setEnabled(true);

        if (fieldsView.getListModel().getSize() == 0) {
            validFormat = false;
            timePretenders.setEnabled(false);
            timeTemplateField.setEnabled(false);
        } else {
            Object selected = ((AdapterComboBoxModel) timePretenders.getModel()).selectedObject;
            if (selected == null || selected == AdapterComboBoxModel.defaultElement)
                timeTemplateField.setEnabled(false);
            else if (timeTemplateField.getText() == null || timeTemplateField.getText().length() == 0)
                validFormat = false;
            else {
                try {
                    if (DateTimeFormat.forPattern(timeTemplateField.getText()) == null)
                        validFormat = false;
                } catch (IllegalArgumentException e) {
                    validFormat = false;
                }
            }

        }
        changeSupport.firePropertyChange("validFormat", isValidFormatOld, validFormat);
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
            return i == 0 ? defaultElement : listModel.getElementAt(i - 1);
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

