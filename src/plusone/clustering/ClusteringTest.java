package plusone.clustering;

public abstract class ClusteringTest implements ClusteringMethod {

    String testName;
    public ClusteringTest(String testName) {
	this.testName = testName;
    }

    /**
     * Predict k words to occur in each document in the testing set.  The
     * testing documents (and training documents, if any) should be provided to
     * the ClusteringTest instance in some other way (e.g. passed to a
     * constructor).
     *
     * @param k The number of distinct words to predict for each document.
     * @param outputUsedWord If fals, this method should not list any words
     *        that it has already seen in the document.
     * @return p, where p[i][j] is the j-th predicted word for the i-th
     *         document.
     */
    public Integer[][] predict(int k, boolean outputUsedWord) {
	return null;
    }
}