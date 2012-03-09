package plusone.utils;

import java.io.File;
import plusone.utils.PlusoneFileWriter;
import cern.colt.matrix.*;
import java.util.*;
import org.apache.commons.lang.StringEscapeUtils;
import cern.colt.matrix.impl.*;

public class Plot{

    final static String[] remoteSources = {"jquery.js", "flot/jquery.flot.js"};
    final static String[] localSources = {"viz/GraphProperties.js"};
    private PlusoneFileWriter writer;
    public Plot(String outputFile){
	File output = new File(outputFile);
	writer = new PlusoneFileWriter(output);
    }

    void appendHtml(String s) {

        writer.write(s);
    }

    void appendJSSrc(String url) {
        appendHtml("<script src=\"" + url + "\" type=\"text/javascript\"></script>\n");
    }

    public void beginReport() {
        appendHtml("<!DOCTYPE HTML>\n<html>\n");
        appendHtml("<head>\n<meta charset=\"utf-8\">\n<title>Graph Properties</title>\n");
        for (String s : remoteSources)
            appendJSSrc("https://box309.bluehost.com/~falsifia/a/ko13/" + s);
        for (String s : localSources)
            appendJSSrc(s);
        appendHtml("</head>\n<body>\n");
    }

    public void endReport() {
        appendHtml("</body>\n</html>\n");
	writer.close();
    }

    public void report(String msg) {
        appendHtml("<p>" + StringEscapeUtils.escapeHtml(msg) + "</p>\n");
    }

    public void reportPlot(DoubleMatrix1D values, boolean logX, boolean logY) {
        appendHtml("<div class=\"plot_me\">");
        if (logX) appendHtml("s:logX ");
        if (logY) appendHtml("s:logY ");
        for (int i = 0; i < values.size(); ++i) {
            appendHtml((0 == i ? "" : " ") + (logX ? 1+i : i) + "," + (values.get(i)));
        }
        appendHtml("</div>\n");
    }
    public void reportPlot(List<Double> values, boolean logX, boolean logY){
	DoubleMatrix1D v = new DenseDoubleMatrix1D(values.size());
	for (int i = 0; i < values.size(); ++i) v.set(i, values.get(i));
	reportPlot(v, logX, logY);
    }


    public void reportHistogram(List<Double> values, int numBuckets) {
        if (0 == values.size()) {
            report("(Empty histogram.)");
            return;
        }
        double min = Collections.min(values);
        double step = (Collections.max(values) - min) * 1.01 / numBuckets;
        int buckets[] = new int[numBuckets];
        for (int i = 0; i < values.size(); ++i) {
            ++ buckets[(int)((values.get(i) - min) / step)];
        }
        appendHtml("<div class=\"plot_me\">");
        for (int i = 0; i < numBuckets; ++i) {
            appendHtml((0 == i ? "" : " ") + (min + step * i) + "," + buckets[i]);
            appendHtml(" " + (min + step * (1 + i)) + "," + buckets[i]);
        }
        appendHtml("</div>\n");
    }
}