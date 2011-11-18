package plusone.clustering;

import java.io.File;
import plusone.utils.PlusoneFileWriter;
import plusone.utils.PredictionPaper;
import plusone.utils.MetadataLogger.TestMetadata;

public abstract class ClusteringTest implements ClusteringMethod {

    public String testName;
    public ClusteringTest(String testName) {
	this.testName = testName;
    }

    public Integer[] predict(int k, PredictionPaper testPaper) { 
	return null;
    }

    protected PlusoneFileWriter makePredictionWriter(int k, 
						     File outputDirectory, 
						     String extra) {
	if (outputDirectory == null) {
            return new PlusoneFileWriter();
        } else {
	    return new PlusoneFileWriter(new File(outputDirectory, 
                                                  this.testName + "-" + 
						  k + "-" +
                                                  (extra == null ? "" : 
						   "-" + extra) +
                                                  ".predict"));
	}
    }

    public void addMetadata(TestMetadata meta) {
	meta.createSingleValueEntry("expName", testName);
    }
}