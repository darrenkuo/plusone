package plusone.clustering;

import plusone.utils.PaperAbstract;

import java.util.List;

public interface ClusteringMethod {
    public void analysis(int numPred, boolean usedWord);
    //private void train(List<PaperAbstract> abstracts);
    //private void test(List<PaperAbstract> abstracts);
}