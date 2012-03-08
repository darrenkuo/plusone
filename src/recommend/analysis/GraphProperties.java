package recommend.analysis;

import cern.colt.matrix.*;
import cern.colt.matrix.impl.*;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.SingularValueDecomposition;
import java.util.*;
import org.apache.commons.lang.StringEscapeUtils;
import recommend.util.Dataset;
import recommend.util.DocAlgebra;

public class GraphProperties {
    final static String[] remoteSources = {"jquery.js", "flot/jquery.flot.js"};
    final static String[] localSources = {"viz/GraphProperties.js"};
    Random rand = new Random();

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
            appendHtml((0 == i ? "" : " ") + (logX ? 1+i : i) + "," + (values.get(i) + 2.0));
        }
        appendHtml("</div>\n");
    }

    void reportHistogram(List<Double> values, int numBuckets) {
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

    static DoubleMatrix1D ones(int n) {
        return (new DenseDoubleMatrix1D(n)).assign(1.0);
    }

    static double normL2Sqr(DoubleMatrix1D v) {
        return v.zDotProduct(v);
    }

    List<Double> randomSimilarities(DoubleMatrix2D docTerm, int n) {
        int numDocs = docTerm.rows();
        List<Double> l2Sims = new ArrayList<Double>();
        for (int i = 0; i < n; ++i) {
            int idx0 = rand.nextInt(numDocs);
            int idx1 = rand.nextInt(numDocs - 1);
            if (idx0 <= idx1) ++idx1;
            DoubleMatrix1D d0 = docTerm.viewRow(idx0);
            DoubleMatrix1D d1 = docTerm.viewRow(idx1);
            l2Sims.add(d0.zDotProduct(d1) / Math.sqrt(normL2Sqr(d0) * normL2Sqr(d1)));
        }
        return l2Sims;
    }

    int sampleUnnorm(DoubleMatrix1D freqs, double l1) {
        double r = rand.nextDouble();
        double acc = 0.0;
        for (int i = 0;; ++i) {
            acc += freqs.get(i) / l1;
            if (acc >= r) return i;
        }
    }

    DoubleMatrix2D genIndependentDocs(DoubleMatrix1D termFreqs, int numDocs, int nwords) {
        DoubleMatrix2D docTerm = new SparseDoubleMatrix2D(numDocs, termFreqs.size());
        double termFreqL1 = termFreqs.zSum();
        for (int i = 0; i < numDocs; ++i) {
            for (int j = 0; j < nwords; ++j) {
                int term = sampleUnnorm(termFreqs, termFreqL1);
                docTerm.set(i, term, 1.0 + docTerm.get(i, term));
            }
        }
        return docTerm;
    }

    void dynMain(String[] argv) throws Throwable {
        final String datasetPath = System.getProperty("dataset", "data/med.json");
        final Algebra algebra = new Algebra();

        beginReport();

        /* Loading the dataset. */
        report("dataset path: " + datasetPath);
        Map<Integer, Double>[] docs = Dataset.loadDataset(datasetPath);
        DoubleMatrix2D docTerm = DocAlgebra.docsToMatrix(docs);
        DoubleMatrix2D termDoc = docTerm.viewDice();

        /* Basic information. */
        int numDocs = docTerm.rows();
        int numTerms = docTerm.columns();
        report("" + numDocs + " documents and " + numTerms + " terms");

        /* Term frequencies. */
        DoubleMatrix1D termFreqs = algebra.mult(termDoc, ones(numDocs));
        DoubleMatrix1D sortedTermFreqs = termFreqs.viewSorted().viewFlip();
        report("Sorted term frequencies:");
        reportPlot(sortedTermFreqs, true, true);

        /* Experiment: typical similarities. */
        report("Here are some L2-normalized similarities of random pairs of distinct documents.");
        reportHistogram(randomSimilarities(docTerm, 10000), 100);
        report("The same experiment, but with a set of " + numDocs + " random documents with 50 words each.");
        reportHistogram(randomSimilarities(genIndependentDocs(sortedTermFreqs, numDocs, 50), 10000), 100);

        endReport();
    }
}
