package org.lf.ui.components.plugins.scrollablelog;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.lf.logs.Log;
import org.lf.plugins.Attributes;
import org.lf.plugins.Plugin;
import org.lf.plugins.ProgramContext;
import org.lf.plugins.display.View;
import org.lf.plugins.extension.ExtensionPointID;
import org.lf.plugins.extension.ListExtensionPoint;
import org.lf.ui.components.plugins.scrollablelog.extension.SLInitExtension;

import java.util.List;

/**
 * Created on: 26.05.2010 15:49:11
 */
public class ScrollableLogPlugin implements Plugin {
    public static final ExtensionPointID<SLInitExtension> SL_INIT_EXTENSION_POINT_ID = ExtensionPointID.create();

    private static ListExtensionPoint<SLInitExtension> extensionPoint = new ListExtensionPoint<SLInitExtension>();

    @Override
    public void init(ProgramContext context) {
        context.getExtensionPointsManager().registerExtensionPoint(SL_INIT_EXTENSION_POINT_ID, extensionPoint);

        context.getXstream().registerConverter(new Converter() {
            @Override
            public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
                ScrollableLogView view = (ScrollableLogView) source;

                writer.startNode("log");
                context.convertAnother(view.getLog());
                writer.endNode();

                writer.startNode("attributes");
                context.convertAnother(view.getAttributes());
                writer.endNode();

                writer.startNode("viewState");
                context.convertAnother(view.getState());
                writer.endNode();
            }

            @Override
            public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
                reader.moveDown();
                Log log = (Log)context.convertAnother(null, Log.class);
                reader.moveUp();

                reader.moveDown();
                Attributes attributes = (Attributes)context.convertAnother(null, Attributes.class);
                reader.moveUp();

                reader.moveDown();
                ScrollableLogState state = (ScrollableLogState)context.convertAnother(null, ScrollableLogState.class);
                reader.moveUp();

                ScrollableLogView res = new ScrollableLogView(log, attributes);
                res.restoreState(state);
                return res;
            }

            @Override
            public boolean canConvert(Class type) {
                return View.class.isAssignableFrom(type);
            }
        });
    }

    static List<SLInitExtension> getInitExtensions() {
        return extensionPoint.getItems();
    }
}
