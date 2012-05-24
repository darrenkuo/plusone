package plusone.clustering;

import java.io.File;

import plusone.utils.PredictionPaper;

public interface ClusteringMethod {
    /**
     * Predict k words to occur in each document in the testing set.
     * The testing documents (and training documents, if any) should
     * be provided to the ClusteringTest instance in some other way
     * (e.g. passed to a constructor).
     *
     * @param k The number of distinct words to predict for each document.
     * @param outputUsedWord If fals, this method should not list any words
     *        that it has already seen in the document.
     * @param outputDirectory A directory in which to create a file with the
     *        predicted words.  If it's null, then the predicted words are not
     *        printed anywhere.
     * @return p, where p[i][j] is the j-th predicted word for the i-th
     *         document.
     */

    public double[] predict(PredictionPaper paper);



}