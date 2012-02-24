/* Take 100 documents from med.out and test everything on them. */

package plusone.experiments;

import java.io.IOException;
import java.util.Random;
import plusone.utils.Dataset;

public class EverythingTest {
    final static String datasetName = "med.out";
    final static int nPapers = 100;
    final static double trainPercent = 0.95;
    final static double testWordPercent = 0.3;
    final static int wordsToPredict = 1;  // Called "k" in some places in the code.
    final static long randomSeed = 56214;  // Guaranteed random.

    public static void main(String[] args) throws IOException {
        Random randGen = new Random(randomSeed);
        Dataset dataset = plusone.utils.Dataset.loadDatasetFromName(datasetName).take(nPapers);
        Dataset.TrainingAndTesting tt = dataset.splitByTrainPercent(trainPercent, randGen);
        // TODO: generate report, including: dataset hash; testing and training set size
    }
}
