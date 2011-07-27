package plusone.clustering;

public abstract class ClusteringTest implements ClusteringMethod {

    String testName;
    public ClusteringTest(String testName) {
	this.testName = testName;
    }
    
    public void analysis(double trainPercent, double testWordPercent) {
	System.out.println("===============BEGINNING OF " + testName +
			   " Test=======================");
    }

}