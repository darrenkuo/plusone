package plusone.clustering;

import plusone.utils.PaperAbstract;

import java.util.List;

public interface ClusteringMethod {
    public void train(List<PaperAbstract> abstracts);
    public void test(List<PaperAbstract> abstracts);
}