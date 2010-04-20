package org.lf.ui.components.dialog;

import com.sun.istack.internal.Nullable;
import org.lf.io.MappedFile;
import org.lf.parser.Parser;
import org.lf.parser.ParserAdjuster;
import org.lf.parser.csv.CSVParserAdjuster;
import org.lf.parser.regex.RegexpParserAdjuster;
import org.lf.ui.util.GUIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;

import static org.lf.util.CollectionFactory.newHashMap;

public class ParserSetup extends JDialog {
    private final Map<String, Class> parserNameToClass;
    private final Box adjusterBox;
    private final JComboBox parserChooser;
    private ParserAdjuster parserAdjuster;
    private Boolean isOKPressed = false;
    private JButton okButton;


    public ParserSetup(String fileName) throws IOException {
        super((JFrame)null, "Parser setup");

        Box mainPanel = Box.createVerticalBox();
        mainPanel.setBorder( BorderFactory.createEmptyBorder(12,12,12,12));
        this.setContentPane(mainPanel);

        MappedFile file = new MappedFile(fileName);
        JPanel rawPreviewPanel = new RawFilePreview(file);

        parserNameToClass  = newHashMap();
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


        Box okCancelBox = Box.createHorizontalBox();
        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                isOKPressed = true;
                dispose();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                isOKPressed = false;
                dispose();
            }
        });
        okCancelBox.add(okButton);
        okCancelBox.add(Box.createHorizontalStrut(12));
        okCancelBox.add(cancelButton);
        
        adjusterBox = Box.createHorizontalBox();
        try {
            this.parserAdjuster = (ParserAdjuster)parserNameToClass.get(parserChooser.getSelectedItem()).newInstance();
            this.parserAdjuster.setParent(this);
            adjusterBox.add(parserAdjuster);
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        adjusterBox.add(Box.createHorizontalGlue());


        GUIUtils.createRecommendedButtonMargin(new JButton[]{okButton, cancelButton});
        GUIUtils.makeSameWidth(new JComponent[] {okButton, cancelButton});        

        this.add(rawPreviewPanel);
        this.add(Box.createVerticalStrut(5));        
        this.add(parserSelectionBox);
        this.add(Box.createVerticalStrut(5));
        this.add(adjusterBox);
        this.add(Box.createVerticalStrut(12));
        this.add(okCancelBox);

        this.setModal(true);
        this.setPreferredSize(new Dimension(500, 600));
        this.pack();
        this.setVisible(false);
    }
    
    @Nullable
    public Parser showSetupDialog() {
        this.setVisible(true);
        if (!isOKPressed) return null;
        return parserAdjuster.getParser();
    }

    public void setOKButtonEnable(boolean isEnable) {
        okButton.setEnabled(isEnable);
    }

    private class ParserChooserListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            try {
                parserAdjuster = (ParserAdjuster)parserNameToClass.get(parserChooser.getSelectedItem()).newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            adjusterBox.removeAll();
            adjusterBox.add(parserAdjuster);
            parserAdjuster.setParent(ParserSetup.this);            
            adjusterBox.add(Box.createHorizontalGlue());
            adjusterBox.revalidate();
        }
    }
}