package org.lf.ui.persistence;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.lf.plugins.Plugin;
import org.lf.plugins.ProgramContext;
import org.lf.ui.components.tree.NodeData;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

/**
 * Created on: 27.05.2010 15:02:15
 */
public class TreePersistencePlugin implements Plugin {
    @Override
    public void init(ProgramContext context) {
        XStream xstream = context.getXstream();

        xstream.registerConverter(new Converter() {
            @Override
            public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext context) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
                writer.startNode("value");
                if (node.getUserObject() != null) {
                    context.convertAnother((NodeData)node.getUserObject());
                }
                writer.endNode();
                for (int i = 0; i < node.getChildCount(); ++i) {
                    writer.startNode("child");
                    context.convertAnother(node.getChildAt(i));
                    writer.endNode();
                }
            }

            @Override
            public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
                DefaultMutableTreeNode res = new DefaultMutableTreeNode();
                reader.moveDown();
                if (reader.hasMoreChildren()) {
                    res.setUserObject(context.convertAnother(res, NodeData.class));
                }
                reader.moveUp();
                while (reader.hasMoreChildren()) {
                    reader.moveDown();
                    res.add((MutableTreeNode) context.convertAnother(res, DefaultMutableTreeNode.class));
                    reader.moveUp();
                }
                return res;
            }

            @Override
            public boolean canConvert(Class clazz) {
                return DefaultMutableTreeNode.class.isAssignableFrom(clazz);
            }
        });
    }
}
