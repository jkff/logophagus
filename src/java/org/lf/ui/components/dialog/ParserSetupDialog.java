package org.lf.ui.components.dialog;

import org.jetbrains.annotations.Nullable;
import org.lf.parser.Parser;
import org.lf.parser.csv.CSVParserAdjuster;
import org.lf.parser.line.LineParserAdjuster;
import org.lf.parser.regex.RegexpParserAdjuster;
import org.lf.ui.components.common.ParserAdjuster;
import org.lf.ui.util.GUIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Map;

import static org.lf.util.CollectionFactory.newHashMap;

public class ParserSetupDialog extends JDialog implements PropertyChangeListener {
    private final Map<String, Class> parserNameToClass;
    private final Box adjusterBox;
    private final JComboBox parserChooser;
    private ParserAdjuster parserAdjuster = null;
    private boolean clickedOkNotCancel = false;
    private JButton okButton;


    public ParserSetupDialog(Window owner) throws IOException {
        super(owner, "Parser setup");

        Box mainPanel = Box.createVerticalBox();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        this.setContentPane(mainPanel);

        parserNameToClass = newHashMap();

        parserNameToClass.put("Line parser", LineParserAdjuster.class);
        parserNameToClass.put("CSV parser", CSVParserAdjuster.class);
        parserNameToClass.put("Regexp parser", RegexpParserAdjuster.class);
        this.parserChooser = new JComboBox(parserNameToClass.keySet().toArray(new String[0]));
        this.parserChooser.addActionListener(new ParserChooserListener());

        Box parserSelectionBox = Box.createHorizontalBox();
        parserSelectionBox.add(new JLabel("Select parser"));
        parserSelectionBox.add(Box.createHorizontalStrut(12));
        parserSelectionBox.add(parserChooser);
        parserSelectionBox.add(Box.createHorizontalGlue());
        GUIUtils.makePreferredSize(parserChooser);
        GUIUtils.makePreferredSize(parserSelectionBox);


        Box okCancelBox = Box.createHorizontalBox();
        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                clickedOkNotCancel = true;
                dispose();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                clickedOkNotCancel = false;
                dispose();
            }
        });
        okCancelBox.add(okButton);
        okCancelBox.add(Box.createHorizontalStrut(12));
        okCancelBox.add(cancelButton);
        GUIUtils.createRecommendedButtonMargin(okButton, cancelButton);
        GUIUtils.makeSameWidth(okButton, cancelButton);

        GUIUtils.makePreferredSize(okCancelBox);

        adjusterBox = Box.createHorizontalBox();

        this.add(parserSelectionBox);
        this.add(Box.createVerticalStrut(5));
        this.add(adjusterBox);
        this.add(Box.createVerticalStrut(12));
        this.add(okCancelBox);

        this.setModal(true);
        this.setVisible(false);
        this.setResizable(false);
    }

    @Nullable
    public Parser showSetupDialog() {
        update();
        this.pack();
        this.setVisible(true);
        if (clickedOkNotCancel)
            return parserAdjuster.getParser();
        return null;
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        okButton.setEnabled(parserAdjuster.isAdjustmentValid());
    }

    private void update() {
        if (parserAdjuster != null)
            parserAdjuster.removePropertyChangeListener(this);
        try {
            //TODO don't create new instance if same one had been created
            parserAdjuster = (ParserAdjuster)
                    parserNameToClass.get(((String) parserChooser.getSelectedItem()))
                                     .newInstance();
            parserAdjuster.addPropertyChangeListener("adjustmentValid", this);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        okButton.setEnabled(parserAdjuster.isAdjustmentValid());
        adjusterBox.removeAll();
        adjusterBox.add(parserAdjuster);
        adjusterBox.revalidate();
        this.pack();
    }

    private class ParserChooserListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            update();
        }
    }
}