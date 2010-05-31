package org.lf.ui.components.dialog;

import org.jetbrains.annotations.Nullable;
import org.lf.ui.util.GUIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SearchSetupDialog extends JDialog {
    private boolean okPressed = false;
    private final JTextField textField;
    private final JRadioButton substring;
    private final JRadioButton regexp;
    private final JRadioButton forward;
    private final JRadioButton backward;
    private final JCheckBox caseBox;

    public class SearchContext {
        public final boolean substringNotRegexp;
        public final boolean forwardNotBackward;
        public final boolean caseSensitive;
        public final String text;

        public SearchContext(boolean caseSensitive, boolean forwardNotBackward, boolean substringNotRegexp, String text) {
            this.caseSensitive = caseSensitive;
            this.forwardNotBackward = forwardNotBackward;
            this.substringNotRegexp = substringNotRegexp;
            this.text = text;
        }
    }

    public SearchSetupDialog(Window owner, ModalityType modality) {
        this(owner, null, modality);
    }

    public SearchSetupDialog(Window owner, String initialText, ModalityType modality) {
        super(owner, "Search", modality);

        Box contentBox = Box.createVerticalBox();
        contentBox.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        this.setContentPane(contentBox);


        textField = new JTextField(initialText);
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okPressed = true;
                setVisible(false);
            }
        });
        if (initialText != null) {
            textField.setSelectionStart(0);
            textField.setSelectionEnd(initialText.length());
        }

        ButtonGroup bgSR = new ButtonGroup();
        ButtonGroup bgFB = new ButtonGroup();

        JPanel radioButtonPanel = new JPanel(new GridLayout(1, 2, 15, 5));

        JPanel SRPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        JPanel FBPanel = new JPanel(new GridLayout(2, 1, 0, 5));

        substring = new JRadioButton("Substring", true);
        regexp = new JRadioButton("Regexp");
        forward = new JRadioButton("Forward", true);
        backward = new JRadioButton("Backward");

        bgSR.add(substring);
        bgSR.add(regexp);

        bgFB.add(forward);
        bgFB.add(backward);

        SRPanel.add(substring);
        SRPanel.add(regexp);

        FBPanel.add(forward);
        FBPanel.add(backward);

        radioButtonPanel.add(SRPanel);
        radioButtonPanel.add(FBPanel);

        caseBox = new JCheckBox("Case sensitive", true);

        JButton ok = new JButton("Ok");
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okPressed = true;
                setVisible(false);
            }
        });

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okPressed = false;
                setVisible(false);
            }
        });

        GUIUtils.makeSameWidth(ok, cancel);
        Box okCancelPanel = Box.createHorizontalBox();
        okCancelPanel.add(ok);
        okCancelPanel.add(Box.createHorizontalStrut(5));
        okCancelPanel.add(cancel);

        this.add(textField);
        this.add(Box.createVerticalStrut(5));
        this.add(caseBox);
        this.add(Box.createVerticalStrut(5));
        this.add(radioButtonPanel);
        this.add(Box.createVerticalStrut(5));
        this.add(okCancelPanel);

        Dimension dim = owner == null ? Toolkit.getDefaultToolkit().getScreenSize() : owner.getSize();
        setLocationRelativeTo(owner);
        setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);

        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.pack();
        this.setModal(true);
        this.setResizable(false);
        this.setVisible(false);
    }

    @Nullable
    public SearchContext showSetupDialog() {
        setVisible(true);
        if (!okPressed)
            return null;
        SearchContext result = new SearchContext(
                caseBox.isSelected(), forward.isSelected(), substring.isSelected(), textField.getText());
        dispose();
        return result;
    }

    public SearchContext showSetupDialog(SearchContext initialContext) {
        caseBox.setSelected(initialContext.caseSensitive);
        forward.setSelected(initialContext.forwardNotBackward);
        backward.setSelected(!initialContext.forwardNotBackward);
        substring.setSelected(initialContext.substringNotRegexp);
        regexp.setSelected(!initialContext.substringNotRegexp);
        textField.setText(initialContext.text);
        textField.setSelectionStart(0);
        textField.setSelectionEnd(initialContext.text.length());
        this.repaint();
        return showSetupDialog();
    }

    public boolean isOkPressed() {
        return okPressed;
    }
}
