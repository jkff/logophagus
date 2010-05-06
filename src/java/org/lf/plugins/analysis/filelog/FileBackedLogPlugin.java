package org.lf.plugins.analysis.filelog;

import com.sun.istack.internal.Nullable;
import org.joda.time.format.DateTimeFormat;
import org.lf.io.GzipRandomAccessIO;
import org.lf.io.MappedFile;
import org.lf.io.RandomAccessFileIO;
import org.lf.logs.Field;
import org.lf.logs.FileBackedLog;
import org.lf.logs.Format;
import org.lf.logs.Log;
import org.lf.parser.Parser;
import org.lf.parser.line.LineParser;
import org.lf.parser.regex.RegexpParser;
import org.lf.plugins.Attributes;
import org.lf.plugins.Entity;
import org.lf.plugins.analysis.AnalysisPlugin;
import org.lf.plugins.analysis.Bookmarks;
import org.lf.plugins.analysis.TreeAction;
import org.lf.services.ProgramProperties;
import org.lf.ui.components.tree.AnalysisPluginNode;
import org.lf.ui.components.tree.NodeData;
import org.lf.ui.components.tree.TreeContext;
import org.lf.ui.util.ProgressDialog;
import org.lf.util.ProgressListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class FileBackedLogPlugin implements AnalysisPlugin {

    @Override
    public TreeAction getActionFor(final TreeContext context) {
        if (context.selectedNodes.length != 0) return null;
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Entity entity = getEntity();
                if (entity == null) return;
                NodeData nodeData = new NodeData(entity, getIcon());
                context.root.add(new AnalysisPluginNode(nodeData));
            }
        };

        action.putValue(Action.NAME, getName());
        return new TreeAction(action);
    }

    public String getName() {
        return "Open log from file";
    }

    @Override
    public Icon getIcon() {
        return new ImageIcon(ProgramProperties.iconsPath + "log.gif");
    }

    @Nullable
    private Entity getEntity() {
        JFileChooser fileOpen = new JFileChooser(ProgramProperties.getWorkingDir());
        int state = fileOpen.showOpenDialog(null);
        if (state != JFileChooser.APPROVE_OPTION)
            return null;
        File f = fileOpen.getSelectedFile();
        if (!f.isFile())
            return null;
        ProgramProperties.setWorkingDir(f.getParentFile());

        try {
            ProgramProperties.save();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Parser parser;
//        try {
//            ParserSetupDialog psd = new ParserSetupDialog(Frame.getFrames()[0]);
//            parser = psd.showSetupDialog();
//            psd.dispose();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        if (parser == null) return null;
        try {
            RandomAccessFileIO io;

            if (f.getName().endsWith(".gz") || f.getName().endsWith("zip")) {
                final GzipRandomAccessIO cio = new GzipRandomAccessIO(f.getAbsolutePath(), 1 << 20);
                final ProgressDialog d = new ProgressDialog(
                        Frame.getFrames()[0],
                        "Indexing compressed file for random access", "",
                        Dialog.ModalityType.APPLICATION_MODAL);
                d.setSize(400, 100);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            cio.init(new ProgressListener<Double>() {
                                public boolean reportProgress(final Double donePart) {
                                    d.setProgress(donePart);
                                    if (donePart == 1.0)
                                        d.setVisible(false);
                                    boolean isCanceled = d.isCanceled();
                                    if (isCanceled)
                                        d.setVisible(false);
                                    return !isCanceled;
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                d.setVisible(true);
                if (d.isCanceled())
                    return null;
                io = cio;
            } else {
                io = new MappedFile(f.getAbsolutePath());
            }


//            Log log = new FileBackedLog(io, new CSVParser(new Format(fields, -1, null)));
//            [2200-01-02 06:27:46,148] DEBUG [pool-798] Search performed in 0 with 507 hits
            String[] regexes = new String[]{"\\[([^\\]]++)\\]\\s++(\\w++)\\s++\\[([^\\]]++)\\]\\s++(.++)"};
            Field[] fields = new Field[]{
                    new Field("Time"),
                    new Field("Level"),
                    new Field("pool"),
                    new Field("Message")
            };
            Format singleFormat = new Format(fields, 0,
                    DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss,SSS"));
            parser = new RegexpParser(regexes, new Format[]{singleFormat}, '\n', 1);
//            parser = new LineParser();
            Log log = new FileBackedLog(io, parser);

            Attributes atr = new Attributes();
            atr.addAttribute(new Bookmarks(null, log));
            return new Entity(atr, log);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
