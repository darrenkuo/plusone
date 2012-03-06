class recommend.util;

import cern.colt.matrix.*;
import java.util.*;
import recommend.util.WordIndex;

public class DocAlgebra {
    /* Returns a matrix with docs.size() rows and WordIndex.size() columns. */
    public static DoubleMatrix2D docsToMatrix(List<Map<Integer, Double>> docs) {
        matrix = new SparseDoubleMatrix2D(docs.size(), WordIndex.size());
        for (int i = 0; i < docs.size(); ++i)
            for (Map.Entry<Integer, Double> e : docs.get(i))
                matrix.set(i, e.key(), e.value());
        return matrix;
    }
}
