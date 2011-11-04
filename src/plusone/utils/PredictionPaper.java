package plusone.utils;

import java.util.Set;

public interface PredictionPaper {
    public Double getTrainingTf(Integer word);
    public Set<Integer> getTrainingWords();
    public Integer getIndex();
    public Integer[] getInReferences();
    public Integer[] getOutReferences();
}