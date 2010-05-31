package org.lf.plugins.tree.filelog;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.jetbrains.annotations.Nullable;
import org.lf.io.GzipRandomAccessIO;
import org.lf.io.MappedFile;
import org.lf.io.RandomAccessFileIO;
import org.lf.io.zlib.IndexMemento;
import org.lf.logs.FileBackedLog;
import org.lf.logs.Log;
import org.lf.parser.Parser;
import org.lf.plugins.Attributes;
import org.lf.plugins.Entity;
import org.lf.plugins.Plugin;
import org.lf.plugins.ProgramContext;
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
import java.io.FileNotFoundException;
import java.io.IOException;

public class OpenLogFromFilePlugin implements TreePlugin, Plugin {
    @Override
    public void init(ProgramContext context) {
        context.getTreePluginRepository().register(this);

        context.getXstream().registerConverter(new SingleValueConverter() {
            public String toString(Object obj) {
                return ((MappedFile)obj).getFile().getAbsolutePath();
            }
            public Object fromString(String str) {
                try {
                    return new MappedFile(str);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            public boolean canConvert(Class type) {
                return MappedFile.class.isAssignableFrom(type);
            }
        });

        context.getXstream().registerConverter(new Converter() {
            @Override
            public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
                GzipRandomAccessIO io = (GzipRandomAccessIO) source;
                writer.startNode("filename");
                writer.setValue(io.getFile().getAbsolutePath());
                writer.endNode();
                writer.startNode("chunkSize");
                writer.setValue(""+io.getChunkSize());
                writer.endNode();
                writer.startNode("index");
                context.convertAnother(io.getIndexMemento());
                writer.endNode();
            }

            @Override
            public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
                reader.moveDown();
                String filename = reader.getValue();
                reader.moveUp();
                reader.moveDown();
                int chunkSize = Integer.parseInt(reader.getValue());
                reader.moveUp();
                reader.moveDown();
                IndexMemento indexMemento = (IndexMemento) context.convertAnother(null, IndexMemento.class);
                reader.moveUp();
                GzipRandomAccessIO io = new GzipRandomAccessIO(filename, chunkSize);
                io.initFromIndexMemento(indexMemento);
                return io;
            }

            @Override
            public boolean canConvert(Class type) {
                return GzipRandomAccessIO.class.isAssignableFrom(type);
            }
        });
    }

    @Override
    public HierarchicalAction getActionFor(final TreeContext context) {
        if (context.selectedNodes.length != 0)
            return null;
        return new HierarchicalAction(getOpenFileAction(context));
    }

    public AbstractAction getOpenFileAction(final TreeContext context) {
        return new AbstractAction(getName()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Entity entity = selectEntity();
                if (entity == null) return;
                NodeData nodeData = new NodeData(entity, getIconFilename());
                context.addChildToRoot(nodeData, true);
            }
        };
    }

    public String getName() {
        return "Open log from file";
    }

    @Override
    public String getIconFilename() {
        return "log.gif";
    }

    @Nullable
    private static Entity selectEntity() {
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

        if (parser == null)
            return null;
        try {
            RandomAccessFileIO io;

            if (f.getName().endsWith(".gz") || f.getName().endsWith("zip")) {
                try {
                    final GzipRandomAccessIO cio = new GzipRandomAccessIO(f.getAbsolutePath(), 1 << 20);
                    if (!initWithProgressDialog(cio))
                        return null;
                    io = cio;
                } catch(Throwable e) {
                    JOptionPane.showMessageDialog(null, "Gzip support is broken: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            } else {
                io = new MappedFile(f.getAbsolutePath());
            }


//            Log log = new FileBackedLog(io, new CSVParser(new Format(fields, -1, null)));
//            [2200-01-02 06:27:46,148] DEBUG [pool-798] Search performed in 0 with 507 hits
//            String[] regexes = {
//                            "\\[([^\\]]+)\\]\\s+(\\w+)\\s+\\[([^\\]]+)\\]\\s+(.+)",
//                    "\\[([^\\]]++)\\]\\s++(\\w++)\\s++\\[([^\\]]++)\\]\\s++(.++)",
//                    };

//            Field[] fields = {
//                    new Field("Time"),
//                    new Field("Level"),
//                    new Field("Thread"),
//                    new Field("Message")};
//            Format format1 = new Format(fields1, 0,
//            Format format = new Format(fields, 0, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss,SSS"));

//            parser = new RegexpParser(regexes, new Format[]{format}, '\n', new int[]{1});

//            Format multiFormat = new Format(multiFields, -1, null);
//
//            parser = new RegexpParser(regexes, new Format[]{format1}, '\n', new int[]{1});
            Log log = new FileBackedLog(io, parser);

            Attributes atr = new Attributes();
            atr.addAttribute(new Bookmarks(null, log));
            return new Entity(atr, log);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean initWithProgressDialog(final GzipRandomAccessIO cio) {
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
                } catch(ExceptionInInitializerError e) {
                    d.cancel();
                    JOptionPane.showMessageDialog(null, "Gzip support is broken: " + e.getCause().getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
        d.setVisible(true);
        if (d.isCanceled())
            return false;
        return true;
    }
}
