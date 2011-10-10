package plusone.clustering;

import java.io.File;
import plusone.utils.PlusoneFileWriter;

public abstract class ClusteringTest implements ClusteringMethod {

    public String testName;
    public ClusteringTest(String testName) {
	this.testName = testName;
    }

    public Integer[][] predict(int k, boolean outputUsedWord) {
	return null;
    }

    protected PlusoneFileWriter makePredictionWriter(int k, boolean outputUsedWord, File outputDirectory, String extra) {
	if (outputDirectory == null) {
            return new PlusoneFileWriter();
        } else {
	    return new PlusoneFileWriter(new File(outputDirectory, 
                                                  this.testName + "-" + k + "-" + outputUsedWord +
                                                  (extra == null ? "" : "-" + extra) +
                                                  ".predict"));
	}
    }
}