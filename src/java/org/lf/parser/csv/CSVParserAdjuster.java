package org.lf.parser.csv;

import org.lf.logs.Format;
import org.lf.parser.Parser;
import org.lf.parser.ParserAdjuster;
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
    private final JTextField quoteCharacterFiled;
    private final JTextField escapeCharacterFiled;
    private final JButton setFormatButton;
    private final JButton clearFormatButton;

    private Format format = null;

    public CSVParserAdjuster() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        initMaps();

        Box rdBox = Box.createHorizontalBox();

        JLabel rdLabel = new JLabel("Enter record delimiter");
        rdBox.add(rdLabel);
        rdBox.add(Box.createHorizontalStrut(12));
        String s = characterToName.containsKey(CSVParser.DEFAULT_RECORD_DELIMITER) ?
                characterToName.get(CSVParser.DEFAULT_RECORD_DELIMITER) :
                Character.toString(CSVParser.DEFAULT_RECORD_DELIMITER)   ;
        recordDelimiterField = new JTextField(s, 2);
        recordDelimiterField.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent caretEvent) {
                updateComponents();
            }
        });
        rdBox.add(recordDelimiterField);

        rdBox.add(Box.createHorizontalGlue());


        Box fdBox = Box.createHorizontalBox();
        JLabel fdLabel = new JLabel("Enter field delimiter");
        fdBox.add(fdLabel);
        fdBox.add(Box.createHorizontalStrut(12));
        s = characterToName.containsKey(CSVParser.DEFAULT_FIELD_DELIMITER) ?
                characterToName.get(CSVParser.DEFAULT_FIELD_DELIMITER) :
                Character.toString(CSVParser.DEFAULT_FIELD_DELIMITER)   ;
        fieldDelimiterField = new JTextField(s, 2);
        fieldDelimiterField.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent caretEvent) {
                updateComponents();
            }
        });
        fdBox.add(fieldDelimiterField);
        fdBox.add(Box.createHorizontalGlue());

        Box qcBox = Box.createHorizontalBox();
        JLabel qcLabel = new JLabel("Enter quote character");
        qcBox.add(qcLabel);
        qcBox.add(Box.createHorizontalStrut(12));
        s = characterToName.containsKey(CSVParser.DEFAULT_QUOTE_CHARACTER) ?
                characterToName.get(CSVParser.DEFAULT_QUOTE_CHARACTER) :
                Character.toString(CSVParser.DEFAULT_QUOTE_CHARACTER)   ;
        quoteCharacterFiled = new JTextField(s, 2);
        quoteCharacterFiled.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent caretEvent) {
                updateComponents();
            }
        });
        qcBox.add(quoteCharacterFiled);
        qcBox.add(Box.createHorizontalGlue());

        Box ecBox = Box.createHorizontalBox();
        JLabel ecLabel = new JLabel("Enter escape character");
        ecBox.add(ecLabel);
        ecBox.add(Box.createHorizontalStrut(12));
        s = characterToName.containsKey(CSVParser.DEFAULT_ESCAPE_CHARACTER) ?
                characterToName.get(CSVParser.DEFAULT_ESCAPE_CHARACTER) :
                Character.toString(CSVParser.DEFAULT_ESCAPE_CHARACTER)   ;
        escapeCharacterFiled = new JTextField(s, 2);
        escapeCharacterFiled.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent caretEvent) {
                updateComponents();
            }
        });
        ecBox.add(escapeCharacterFiled);
        ecBox.add(Box.createHorizontalGlue());

        GUIUtils.makeSameWidth(new JComponent[]{rdLabel, fdLabel, qcLabel, ecLabel});
        GUIUtils.makeSameWidth(new JComponent[]{recordDelimiterField, fieldDelimiterField,
                quoteCharacterFiled, escapeCharacterFiled});

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



        this.add(rdBox);
        this.add(Box.createVerticalStrut(5));
        this.add(fdBox);
        this.add(Box.createVerticalStrut(5));
        this.add(qcBox);
        this.add(Box.createVerticalStrut(5));
        this.add(ecBox);
        this.add(Box.createVerticalStrut(5));
        this.add(buttonBox);

        GUIUtils.makePreferredSize(this);
        this.revalidate();
    }


    @Override
    public Parser getParser() {
        String temp = recordDelimiterField.getText().trim();
        char rd;
        char fd;
        char ec;
        char qc;

        if (temp.length() == 1)
            rd = temp.toCharArray()[0];
        else
            rd = nameToCharacter.get(temp);

        temp = fieldDelimiterField.getText().trim();
        if (temp.length() == 1)
            fd = temp.toCharArray()[0];
        else
            fd = nameToCharacter.get(temp);

        temp = escapeCharacterFiled.getText().trim();
        if (temp.length() == 1)
            ec = temp.toCharArray()[0];
        else
            ec = nameToCharacter.get(temp);

        temp = quoteCharacterFiled.getText().trim();
        if (temp.length() == 1)
            qc = temp.toCharArray()[0];
        else
            qc = nameToCharacter.get(temp);

        return new CSVParser(format, rd, fd, qc, ec);
    }

    @Override
    protected void updateComponents() {
        if (parent != null)
            this.parent.setOKButtonEnable(true);

        if (format == null) {
            clearFormatButton.setEnabled(false);
            setFormatButton.setEnabled(true);
            if (parent != null)
                this.parent.setOKButtonEnable(false);
        } else {
            clearFormatButton.setEnabled(true);
            setFormatButton.setEnabled(false);
        }

        if (parent == null) return;
        String temp = recordDelimiterField.getText().trim();
        if (temp.length() != 1 && !nameToCharacter.containsKey(temp)) {
            this.parent.setOKButtonEnable(false);
            return;
        }
        temp = fieldDelimiterField.getText().trim();
        if (temp.length() != 1 && !nameToCharacter.containsKey(temp)) {
            this.parent.setOKButtonEnable(false);
            return;
        }
        temp = escapeCharacterFiled.getText().trim();
        if (temp.length() != 1 && !nameToCharacter.containsKey(temp)) {
            this.parent.setOKButtonEnable(false);
            return;
        }
        temp = quoteCharacterFiled.getText().trim();
        if (temp.length() != 1 && !nameToCharacter.containsKey(temp))
            this.parent.setOKButtonEnable(false);
    }

    private void resetFormat() {
        format = null;
        updateComponents();        
    }

    private void setFormat(Format f) {
        format = f;
        updateComponents();
    }
    
    private void initMaps() {
        this.nameToCharacter.put("\\n", '\n');
        this.nameToCharacter.put("\\t", '\t');
        this.nameToCharacter.put("\\r", '\r');

        this.characterToName.put('\n', "\\n");
        this.characterToName.put('\t', "\\t");
        this.characterToName.put('\r', "\\r");
    }
}
