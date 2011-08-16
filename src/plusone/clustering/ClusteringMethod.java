package plusone.clustering;

import plusone.utils.PaperAbstract;

import java.io.File;
import java.util.List;

public interface ClusteringMethod {
    // outputDirectory being null means that we don't print predicted words
    public Integer[][] predict(int k, boolean outputUsedWord, File outputDirectory);
}