package org.lf.ui.components.plugins.scrollablelog;

import org.lf.logs.Record;
import org.lf.plugins.tree.highlight.RecordColorer;
import org.lf.util.Removable;

import java.awt.*;
import java.util.List;

import static org.lf.util.CollectionFactory.newList;

public class AccumulativeColorer implements RecordColorer {
    List<RecordColorer> colorers = newList();

    @Override
    public Color getColor(Record r) {
        for (RecordColorer cur : colorers) {
            Color color = cur.getColor(r);
            if (color != null) return color;
        }
        return null;
    }

    public Removable add(final RecordColorer colorer) {
        colorers.add(colorer);
        return new Removable() {
            @Override
            public void remove() {
                colorers.remove(colorer);
            }
        };
    }

    public Removable addFirst(final RecordColorer colorer) {
        colorers.add(0, colorer);
        return new Removable() {
            @Override
            public void remove() {
                colorers.remove(colorer);
            }
        };
    }

}
