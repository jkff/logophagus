package org.lf.plugins.tree.filelog;

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
import org.lf.parser.regex.RegexpParser;
import org.lf.plugins.Attributes;
import org.lf.plugins.Entity;
import org.lf.plugins.tree.Bookmarks;
import org.lf.plugins.tree.TreePlugin;
import org.lf.services.ProgramProperties;
import org.lf.ui.components.dialog.ParserSetupDialog;
import org.lf.ui.components.dialog.ProgressDialog;
import org.lf.ui.components.tree.NodeData;
import org.lf.ui.components.tree.TreeContext;
import org.lf.util.HierarchicalAction;
import org.lf.util.ProgressListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class OpenLogFromFilePlugin implements TreePlugin {

    @Override
    public HierarchicalAction getActionFor(final TreeContext context) {
        if (context.selectedNodes.length != 0) return null;
        return new HierarchicalAction(new AbstractAction(getName()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Entity entity = getEntity();
                if (entity == null) return;
                NodeData nodeData = new NodeData(entity, getIconFilename());
                context.addChildToRoot(nodeData, true);
            }
        });
    }

    public String getName() {
        return "Open log from file";
    }

    @Override
    public String getIconFilename() {
        return "log.gif";
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

        Parser parser = null;
        try {
            ParserSetupDialog psd = new ParserSetupDialog(Frame.getFrames()[0]);
            parser = psd.showSetupDialog();
            psd.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (parser == null) return null;
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
//            String[] regexes = new String[]
//                    {
//                            "\\[([^\\]]++)\\]\\s++(\\w++)\\s++\\[([^\\]]++)\\]\\s++(.++)",
//                            "\\s*(log4j:)\\s*(.*)",
//                            "\\s*(java.lang.NumberFormat.*)\\n\\s*(at.*)\\s*"
//                    };
//
//            Field[] fields1 = new Field[]{
//                    DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss,SSS"));
//            parser = new RegexpParser(regexes, new Format[]{singleFormat}, '\n', 1);
//
//            Field[] fields2 = new Field[]{
//                    new Field("From"),
//                    new Field("Message")
//            };
//
//            Field[] multiFields = new Field[]{
//                    new Field("Exception"),
//                    new Field("At Message")
//            };
//
//            Format format1 = new Format(fields1, 0,
//
//            Format format2 = new Format(fields2, -1, null);
//
//            Format multiFormat = new Format(multiFields, -1, null);
//
//            parser = new RegexpParser(regexes, new Format[]{format1, format2, multiFormat}, '\n', new int[]{1, 1, 2});
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
