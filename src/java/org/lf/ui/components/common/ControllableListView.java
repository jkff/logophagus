package org.lf.ui.components.common;

import org.lf.ui.util.GUIUtils;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControllableListView<T> extends JPanel implements ListDataListener {
    private final JList list;
    private final ControllableListModel<T> listModel = new ControllableListModel<T>();
    private final JButton addButton;
    private final JButton upButton;
    private final JButton downButton;
    private final JButton removeButton;

    public ControllableListView() {
        this(null);
    }

    public ControllableListView(ListCellRenderer listRenderer) {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        list = new JList(listModel);
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                updateControls();
            }
        });
        if (listRenderer != null)
            list.setCellRenderer(listRenderer);
        list.setVisible(true);

        addButton = new JButton("+");

        removeButton = new JButton("-");
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int selIndex = list.getSelectedIndex();
                if (selIndex == -1) return;
                listModel.remove(selIndex);
                list.setSelectedIndex(selIndex);
            }
        });


        upButton = new JButton("Up");
        upButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                listModel.moveUp(list.getSelectedIndex());
                list.setSelectedIndex(list.getSelectedIndex() - 1);
            }
        });

        downButton = new JButton("Down");
        downButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                listModel.moveDown(list.getSelectedIndex());
                list.setSelectedIndex(list.getSelectedIndex() + 1);
            }
        });


        Box upDownListBox = Box.createHorizontalBox();
        Box upDownBox = Box.createVerticalBox();
        Box addRemoveBox = Box.createHorizontalBox();

        upDownBox.add(upButton);
        upDownBox.add(Box.createVerticalStrut(5));
        upDownBox.add(downButton);

        upDownListBox.add(upDownBox);
        GUIUtils.makePreferredSize(upDownBox);
        upDownListBox.add(Box.createHorizontalStrut(5));
        JScrollPane scrollList = new JScrollPane(list);
        upDownListBox.add(scrollList);

        addRemoveBox.add(Box.createHorizontalGlue());
        addRemoveBox.add(addButton);
        addRemoveBox.add(Box.createHorizontalStrut(12));
        addRemoveBox.add(removeButton);
        GUIUtils.fixMaxHeightSize(addRemoveBox);

        this.add(upDownListBox);
        this.add(Box.createVerticalStrut(5));
        this.add(addRemoveBox);

        GUIUtils.createRecommendedButtonMargin(addButton, removeButton);
        GUIUtils.makeSameWidth(addButton, removeButton);
        GUIUtils.makeSameWidth(upButton, downButton);

        this.revalidate();

        listModel.addListDataListener(this);
        updateControls();
    }

    public void setAddButtonActionListener(ActionListener ae) {
        if (addButton.getActionListeners().length != 0)
            addButton.removeActionListener(addButton.getActionListeners()[0]);
        addButton.addActionListener(ae);
    }

    public ControllableListModel<T> getListModel() {
        return listModel;
    }

    @Override
    public void intervalAdded(ListDataEvent listDataEvent) {
        updateControls();
    }

    @Override
    public void intervalRemoved(ListDataEvent listDataEvent) {
        updateControls();
    }

    @Override
    public void contentsChanged(ListDataEvent listDataEvent) {
        updateControls();

    }

    private void updateControls() {
        removeButton.setEnabled(true);
        upButton.setEnabled(true);
        downButton.setEnabled(true);
        if (listModel.getSize() == 0) {
            removeButton.setEnabled(false);
            upButton.setEnabled(false);
            downButton.setEnabled(false);
        } else {
            int curIndex = list.getSelectedIndex();
            if (curIndex == 0 || curIndex == -1)
                upButton.setEnabled(false);
            if (curIndex == listModel.getSize() - 1 || curIndex == -1)
                downButton.setEnabled(false);
        }
    }


}
