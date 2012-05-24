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
    public String getName(){
	return testName;
    }

<<<<<<< HEAD
    public double[] predict(PredictionPaper testPaper) { 
=======
    public double[] predict(int k, PredictionPaper testPaper) { 
>>>>>>> 51347e16ab32dde454feef1e42de9d758b272f16
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