package plusone.utils;

import java.util.Set;

public interface TrainingPaper {
    public Integer getTrainingTf(Integer word);
    public Set<Integer> getTrainingWords();
}