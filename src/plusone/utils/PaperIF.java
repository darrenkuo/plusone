package plusone.utils;

import java.util.Set;

/**
 * Interface extended by training and prediction papers.
 */
public interface PaperIF {
    public Integer getTrainingTf(Integer word);
    public Set<Integer> getTrainingWords();
}
