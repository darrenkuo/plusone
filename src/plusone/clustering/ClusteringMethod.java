package plusone.clustering;

import plusone.utils.PaperAbstract;

import java.util.List;

public interface ClusteringMethod {
    public Integer[][] predict(int k, boolean outputUsedWord);
}