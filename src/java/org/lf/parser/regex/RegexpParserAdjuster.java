package org.lf.parser.regex;

import org.lf.logs.Format;
import org.lf.parser.Parser;
import org.lf.ui.components.common.ControllableListView;
import org.lf.ui.components.common.ParserAdjuster;
import org.lf.ui.components.dialog.FormatDialog;
import org.lf.ui.util.GUIUtils;
import org.lf.util.Pair;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static org.lf.util.CollectionFactory.pair;

public class RegexpParserAdjuster extends ParserAdjuster {
    private ControllableListView<Pair<String, Format>> formatsView;

    public RegexpParserAdjuster() {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        Box formatsBox = Box.createVerticalBox();
        JLabel info = new JLabel("Formats");
        formatsBox.add(info);
        formatsBox.add(Box.createVerticalStrut(5));
        formatsView = new ControllableListView<Pair<String, Format>>(new PairRenderer());
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
                String pattern = JOptionPane.showInputDialog(null, "Enter record pattern:",
                        "Format setup", JOptionPane.QUESTION_MESSAGE);
                if (pattern == null) return;
                Format format = new FormatDialog().showDialog();
                if (format == null) return;
                formatsView.getListModel().add(pair(pattern, format));
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
        if (!isValidAdjust()) return null;

        int size = formatsView.getListModel().getSize();
        String[] patterns = new String[size];
        Format[] formats = new Format[size];
        for (int i = 0; i < size; ++i) {
            Pair<String, Format> cur = formatsView.getListModel().getElementAt(i);
            patterns[i] = cur.first;
            formats[i] = cur.second;
        }
        return new RegexpParser(patterns, formats, '\n', 1);
    }

    private class PairRenderer extends DefaultListCellRenderer {

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
            setValidAdjust(true);
        else
            setValidAdjust(false);
    }
}