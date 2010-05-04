package org.lf.parser.csv;

import org.lf.logs.Format;
import org.lf.parser.Parser;
import org.lf.ui.components.common.ParserAdjuster;
import org.lf.ui.components.dialog.FormatDialog;
import org.lf.ui.util.GUIUtils;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import static org.lf.util.CollectionFactory.newHashMap;

public class CSVParserAdjuster extends ParserAdjuster {
    private final Map<String, Character> nameToCharacter = newHashMap();
    private final Map<Character, String> characterToName = newHashMap();

    private final JTextField recordDelimiterField;
    private final JTextField fieldDelimiterField;
    private final JTextField quoteCharacterField;
    private final JTextField escapeCharacterField;
    private final JButton setFormatButton;
    private final JButton clearFormatButton;

    private Format format = null;

    public CSVParserAdjuster() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        initMaps();

        String[] labels = new String[]{
                "Enter record delimiter",
                "Enter field delimiter",
                "Enter quote character",
                "Enter escape character"
        };

        LabelWithField[] fieldViews = new LabelWithField[labels.length];
        for (int i = 0; i < fieldViews.length; ++i) {
            fieldViews[i] = new LabelWithField(labels[i], 2);
        }

        String s = characterToName.containsKey(CSVParser.DEFAULT_RECORD_DELIMITER) ?
                characterToName.get(CSVParser.DEFAULT_RECORD_DELIMITER) :
                Character.toString(CSVParser.DEFAULT_RECORD_DELIMITER);
        recordDelimiterField = fieldViews[0].field;
        recordDelimiterField.setText(s);


        s = characterToName.containsKey(CSVParser.DEFAULT_FIELD_DELIMITER) ?
                characterToName.get(CSVParser.DEFAULT_FIELD_DELIMITER) :
                Character.toString(CSVParser.DEFAULT_FIELD_DELIMITER);
        fieldDelimiterField = fieldViews[1].field;
        fieldDelimiterField.setText(s);

        s = characterToName.containsKey(CSVParser.DEFAULT_QUOTE_CHARACTER) ?
                characterToName.get(CSVParser.DEFAULT_QUOTE_CHARACTER) :
                Character.toString(CSVParser.DEFAULT_QUOTE_CHARACTER);
        quoteCharacterField = fieldViews[2].field;
        quoteCharacterField.setText(s);

        s = characterToName.containsKey(CSVParser.DEFAULT_ESCAPE_CHARACTER) ?
                characterToName.get(CSVParser.DEFAULT_ESCAPE_CHARACTER) :
                Character.toString(CSVParser.DEFAULT_ESCAPE_CHARACTER);
        escapeCharacterField = fieldViews[3].field;
        escapeCharacterField.setText(s);

        GUIUtils.makeSameWidth(fieldViews[0].label, fieldViews[1].label, fieldViews[2].label, fieldViews[3].label);
        GUIUtils.makeSameWidth(recordDelimiterField, fieldDelimiterField,
                quoteCharacterField, escapeCharacterField);

        setFormatButton = new JButton("Set format");
        setFormatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Format f = new FormatDialog().showDialog();
                if (f != null) setFormat(f);
            }
        });

        clearFormatButton = new JButton("Clear format");
        clearFormatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                resetFormat();
            }
        });

        Box buttonBox = Box.createHorizontalBox();
        buttonBox.add(setFormatButton);
        buttonBox.add(Box.createHorizontalStrut(12));
        buttonBox.add(clearFormatButton);


        for (LabelWithField cur : fieldViews) {
            cur.field.addCaretListener(new CaretListener() {
                @Override
                public void caretUpdate(CaretEvent caretEvent) {
                    update();
                }
            });
            this.add(cur);
            this.add(Box.createVerticalStrut(5));
        }

        this.add(buttonBox);

        GUIUtils.makePreferredSize(this);
        update();
        this.revalidate();
    }


    @Override
    public Parser getParser() {
        if (!isValidAdjustment()) return null;
        JTextField[] fields = new JTextField[]{
                recordDelimiterField,
                fieldDelimiterField,
                quoteCharacterField,
                escapeCharacterField
        };
        char[] fieldValues = new char[4];

        for (int i = 0; i < fields.length; ++i) {
            String temp = fields[i].getText();
            if (temp.length() == 1)
                fieldValues[i] = temp.toCharArray()[0];
            else
                fieldValues[i] = nameToCharacter.get(temp);
        }

        return new CSVParser(format, fieldValues[0], fieldValues[1], fieldValues[2], fieldValues[3]);
    }

    protected void update() {
        boolean isValidAdjust = true;

        JTextField[] fields = new JTextField[]{
                recordDelimiterField,
                fieldDelimiterField,
                quoteCharacterField,
                escapeCharacterField
        };

        for (JTextField field : fields) {
            String temp = field.getText();
            if (temp.length() != 1 && !nameToCharacter.containsKey(temp)) {
                isValidAdjust = false;
                break;
            }
        }

        if (format == null) {
            clearFormatButton.setEnabled(false);
            setFormatButton.setEnabled(true);
            isValidAdjust = false;
        } else {
            clearFormatButton.setEnabled(true);
            setFormatButton.setEnabled(false);
        }
        setValidAdjust(isValidAdjust);
    }

    private void resetFormat() {
        format = null;
        update();
    }

    private void setFormat(Format f) {
        format = f;
        update();
    }

    private void initMaps() {
        this.nameToCharacter.put("\\n", '\n');
        this.nameToCharacter.put("\\t", '\t');
        this.nameToCharacter.put("\\r", '\r');

        this.characterToName.put('\n', "\\n");
        this.characterToName.put('\t', "\\t");
        this.characterToName.put('\r', "\\r");
    }

    private class LabelWithField extends JPanel {
        private JLabel label;
        private JTextField field;

        public LabelWithField(String labelText, int fieldLength) {
            Box box = Box.createHorizontalBox();
            label = new JLabel(labelText);
            box.add(label);
            box.add(Box.createHorizontalStrut(12));
            field = new JTextField(fieldLength);
            box.add(field);
            box.add(Box.createHorizontalGlue());
            this.add(box);
            GUIUtils.makePreferredSize(box);
        }
    }

}
