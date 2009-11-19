package org.lf.services;

import org.lf.parser.FileBackedLog;
import org.lf.parser.LineParser;
import org.lf.plugins.FilterBySubstringPlugin;
import org.lf.plugins.FilterBySub;


public class Test {
    public static void main(String[] args) throws Exception {
        AnalysisPluginRepository pl = AnalysisPluginRepository.getInstance();
        pl.registerAnalysisPlugin(FilterBySubstringPlugin.class);
        pl.registerAnalysisPlugin(FilterBySub.class);

        System.out.println("Class-"+pl.getApplicableAnalysisPlugins(new Object[] {new FileBackedLog("test.log", new LineParser())}));
        System.out.println("Class-"+pl.getApplicableAnalysisPlugins(new Object[] {"hello", "goodbye"}));
    }
}
