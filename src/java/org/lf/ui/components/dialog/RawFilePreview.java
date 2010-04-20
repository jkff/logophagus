package org.lf.ui.components.dialog;

import org.lf.io.MappedFile;
import org.lf.parser.ScrollableInputStream;
import org.lf.ui.util.GUIUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.SortedMap;

public class RawFilePreview extends JPanel {
    private static final long MAX_BYTES_TO_READ = 100*1024;
    private final byte[] cachedTextBytes;
    private final JTextArea textArea;
    private Charset currentCharset;
    private SortedMap<String, Charset> charsetMap;

    public RawFilePreview(MappedFile mappedFile) throws IOException {
        charsetMap = Charset.availableCharsets();
        this.currentCharset = charsetMap.get(System.getProperty("file.encoding"));

        JComboBox charsetChooser = new JComboBox(charsetMap.keySet().toArray(new String[0]));
        charsetChooser.setSelectedItem(currentCharset.name());
        charsetChooser.addActionListener(new CharserChooserListener());

        Box charsetPanel = Box.createHorizontalBox();
        charsetPanel.add(new JLabel("Select file charset"));
        charsetPanel.add(Box.createHorizontalStrut(12));
        charsetPanel.add(charsetChooser);
        charsetPanel.add(Box.createHorizontalGlue());


        ScrollableInputStream sis = mappedFile.getInputStreamFrom(0L);

        this.cachedTextBytes = new byte[(int)(mappedFile.length() < MAX_BYTES_TO_READ ? mappedFile.length() : MAX_BYTES_TO_READ)];
        sis.read(cachedTextBytes);
        this.textArea = new JTextArea(new String(cachedTextBytes));
        this.textArea.setEditable(false);
        Box textAreaPanel = Box.createHorizontalBox();
        textAreaPanel.add(new JScrollPane(textArea));


        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(charsetPanel);
        this.add(Box.createVerticalStrut(5));
        this.add(textAreaPanel);

        GUIUtils.makePreferredSize(charsetChooser);
        GUIUtils.makePreferredSize(textArea);

        this.revalidate();
        this.setVisible(true);
    }

    private class CharserChooserListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            JComboBox cb = (JComboBox) ae.getSource();
            String selectedCharset = (String) cb.getSelectedItem();
            if (selectedCharset == null) return;
            currentCharset = charsetMap.get(selectedCharset);
            textArea.setText(new String(cachedTextBytes, currentCharset));
        }
    }

    public Charset getCharset() {
        return currentCharset;        
    }

}
