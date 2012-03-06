package recommend;
import Jama.*;

public class Test3 {
	public static void main( String[] args ) {
	    double[][] arr = new double[][]{ { 1,2,3}, {4,5,6} };
	    Matrix A = new Matrix( arr );
	    SingularValueDecomposition svd = A.svd();
	    Matrix U, V, S;
	    
	    U = svd.getU();
	    V = svd.getV();
	    S = new Matrix( new double[][]{ svd.getSingularValues() } );
	    
	    System.out.println( U.getRowDimension() + " " + U.getColumnDimension() );
	    System.out.println( V.getRowDimension() + " " + V.getColumnDimension() );
	    System.out.println( S.getRowDimension() + " " + S.getColumnDimension() );
    }
}
