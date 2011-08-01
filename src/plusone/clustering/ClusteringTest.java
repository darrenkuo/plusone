package plusone.clustering;

public abstract class ClusteringTest implements ClusteringMethod {

    String testName;
    public ClusteringTest(String testName) {
	this.testName = testName;
    }
    
    public void analysis(int numPred, boolean usedWord) {
	System.out.println("===============BEGINNING OF " + testName +
			   " Test=======================");
    }

}