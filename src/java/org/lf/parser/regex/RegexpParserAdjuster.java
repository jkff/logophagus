package org.lf.parser.regex;

import org.lf.logs.Format;
import org.lf.parser.Parser;
import org.lf.ui.components.common.ControllableListView;
import org.lf.ui.components.common.ParserAdjuster;
import org.lf.ui.util.GUIUtils;
import org.lf.util.Triple;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static org.lf.util.CollectionFactory.triple;

public class RegexpParserAdjuster extends ParserAdjuster {
    private ControllableListView<Triple<String, Integer, Format>> formatsView;

    public RegexpParserAdjuster() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        Box formatsBox = Box.createVerticalBox();
        JLabel info = new JLabel("Formats");
        formatsBox.add(info);
        formatsBox.add(Box.createVerticalStrut(5));
        formatsView = new ControllableListView<Triple<String, Integer, Format>>(new TripleRenderer());
        formatsView.getListModel().addListDataListener(new ListDataListener() {
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

        formatsView.setAddButtonActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                RegexpFormatDialog formatDialog = new RegexpFormatDialog();
                Triple<String, Integer, Format> format = formatDialog.showDialog();
                if (format == null) return;
                formatsView.getListModel().add(triple(format.first, format.second, format.third));
            }
        });
        formatsBox.add(formatsView);

        this.add(formatsBox);
        GUIUtils.makePreferredSize(this);
        updateComponents();
        this.revalidate();

    }

    @Override
    public Parser getParser() {
        if (!isAdjustmentValid()) return null;

        int size = formatsView.getListModel().getSize();
        String[] patterns = new String[size];
        Format[] formats = new Format[size];
        int[] linesPerRecord = new int[size];
        for (int i = 0; i < size; ++i) {
            Triple<String, Integer, Format> cur = formatsView.getListModel().getElementAt(i);
            patterns[i] = cur.first;
            linesPerRecord[i] = cur.second;
            formats[i] = cur.third;
        }
        return new RegexpParser(patterns, formats, linesPerRecord);
    }

    private class TripleRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList jList, Object o, int i, boolean b, boolean b1) {
            JLabel result = (JLabel) super.getListCellRendererComponent(jList, o, i, b, b1);
            if (o != null)
                result.setText(formatsView.getListModel().getElementAt(i).first);
            return result;
        }
    }

    private void updateComponents() {
        if (formatsView.getListModel().getSize() != 0)
            setAdjustmentValid(true);
        else
            setAdjustmentValid(false);
    }
}