package recommend.analysis;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.SingularValueDecomposition;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import recommend.util.Dataset;
import recommend.util.DocAlgebra;

public class GraphProperties {
    public static void main(String[] argv) throws Throwable {
        (new GraphProperties()).dynMain(argv);
    }

    Logger log = Logger.getLogger("graph properties");

    void info(String msg) {
        log.log(Level.INFO, msg);
    }

    void dynMain(String[] argv) throws Throwable {
        String datasetPath = System.getProperty("dataset", "data/med.json");
        info("dataset path: " + datasetPath);
        Map<Integer, Double>[] docs = Dataset.loadDataset(datasetPath);
        DoubleMatrix2D matrix = DocAlgebra.docsToMatrix(docs);

        /* Basic information. */
        info("" + matrix.rows() + " documents and " + matrix.columns() + " terms");
        
        /* Singular values. */
        info("Computing singular values.");
        {
            SingularValueDecomposition svd = new SingularValueDecomposition
                (matrix.rows() < matrix.columns() ? (new Algebra()).transpose(matrix) : matrix);
            double[] singularValues = svd.getSingularValues();
            String sv = "Singular values:";
            for (int i = 0; i < 10; ++i) {
                sv += (i == 0 ? " " : ", ") + singularValues[i];
            }
            info(sv);
        }
    }
}
