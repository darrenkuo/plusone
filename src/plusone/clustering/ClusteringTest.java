package plusone.clustering;

public abstract class ClusteringTest implements ClusteringMethod {

    String testName;
    public ClusteringTest(String testName) {
	this.testName = testName;
    }
    
    public Integer[][] predict(int k, boolean outputUsedWord) {
	return null;
    }
}