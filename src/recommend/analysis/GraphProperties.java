package recommend.analysis;

import cern.colt.matrix.*;
import cern.colt.matrix.impl.*;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.SingularValueDecomposition;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringEscapeUtils;
import recommend.util.Dataset;
import recommend.util.DocAlgebra;

public class GraphProperties {
    final static String[] remoteSources = {"jquery.js", "flot/jquery.flot.js"};
    final static String[] localSources = {"viz/GraphProperties.js"};

    public static void main(String[] argv) throws Throwable {
        (new GraphProperties()).dynMain(argv);
    }

    void appendHtml(String s) {
        System.out.print(s);
    }

    void appendJSSrc(String url) {
        appendHtml("<script src=\"" + url + "\" type=\"text/javascript\"></script>\n");
    }

    void beginReport() {
        appendHtml("<!DOCTYPE HTML>\n<html>\n");
        appendHtml("<head>\n<meta charset=\"utf-8\">\n<title>Graph Properties</title>\n");
        for (String s : remoteSources)
            appendJSSrc("https://box309.bluehost.com/~falsifia/a/ko13/" + s);
        for (String s : localSources)
            appendJSSrc(s);
        appendHtml("</head>\n<body>\n");
    }

    void endReport() {
        appendHtml("</body>\n</html>\n");
    }

    void report(String msg) {
        appendHtml("<p>" + StringEscapeUtils.escapeHtml(msg) + "</p>\n");
    }

    void reportPlot(DoubleMatrix1D values, boolean logX, boolean logY) {
        appendHtml("<div class=\"plot_me\">");
        if (logX) appendHtml("s:logX ");
        if (logY) appendHtml("s:logY ");
        for (int i = 0; i < values.size(); ++i) {
            appendHtml((0 == i ? "" : " ") + i + "," + (values.get(i) + 2.0));
        }
        appendHtml("</div>\n");
    }

    DoubleMatrix1D ones(int n) {
        return (new DenseDoubleMatrix1D(n)).assign(1.0);
    }

    void dynMain(String[] argv) throws Throwable {
        final String datasetPath = System.getProperty("dataset", "data/med.json");
        final Algebra algebra = new Algebra();
        report("dataset path: " + datasetPath);
        Map<Integer, Double>[] docs = Dataset.loadDataset(datasetPath);
        DoubleMatrix2D docTerm = DocAlgebra.docsToMatrix(docs);
        DoubleMatrix2D termDoc = docTerm.viewDice();

        beginReport();

        /* Basic information. */
        int numDocs = docTerm.rows();
        int numTerms = docTerm.columns();
        report("" + numDocs + " documents and " + numTerms + " terms");

        DoubleMatrix1D termFreqs = algebra.mult(termDoc, ones(numDocs));
        report("Sorted term frequencies:");
        reportPlot(termFreqs.viewSorted(), false, true);

        endReport();
    }
}
