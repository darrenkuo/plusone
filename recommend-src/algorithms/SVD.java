package algorithms;
import WordIndex;

import java.util.*;

import Jama.*;
import com.aliasi.matrix.*;

public class SVD extends Algorithm {
	int K;
	
	double[][] U, UT, V, VT; // U=dxk, UT=kxd, VT=kxt, V=txk
	double[] S; // k
	
	public SVD( int K ) {
		super( "SVD-" + K );
		this.K = K;
	}
	
	public void train( List<HashMap<Integer,Double>> traindocs ) {
		double[][] A = new double[traindocs.size()][WordIndex.size()];
		
		for( int i = 0; i < traindocs.size(); i++ ) {
			HashMap<Integer,Double> hm = traindocs.get( i );
			
			for( int j : hm.keySet() ) {
				A[i][j] = hm.get( j );
			}
		}
		
		double featureInit = 0.01;
		double initialLearningRate = 0.005;
		int annealingRate = 1000;
		double regularization = 0.00;
		double minImprovement = 0.0000001;
		int minEpochs = 10;
		int maxEpochs = 50000;
		SvdMatrix svd = SvdMatrix.svd( A, K, featureInit, initialLearningRate, annealingRate, regularization, null, minImprovement, minEpochs, maxEpochs );
		
		U = svd.leftSingularVectors();
		V = svd.rightSingularVectors();
		S = svd.singularValues();
		
		VT = new double[K][WordIndex.size()];
		
		for( int i = 0; i < VT.length; i++ )
			for( int j = 0; j < V.length; j++ )
				VT[i][j] = V[j][i];
	}
	
	public double[] predict( HashMap<Integer,Double> givenwords ) {
    	double[][] c = new double[1][WordIndex.size()];
		
		for( int word : givenwords.keySet() )
			c[0][word] = givenwords.get( word );
		
		c = matmul( c, V );
		
		for( int i = 0; i < c.length; i++ )
			c[0][i] /= S[i];
		
		c = matmul( c, VT );
		return c[0];
    }
	
	private static final double[][] matmul( double[][] a, double[][] b ) {
		double[][] c = new double[a.length][b[0].length];
		
		for( int i = 0; i < c.length; i++ ) {
			for( int j = 0; j < c[0].length; j++ ) {
				for( int k = 0; k < b.length; k++ ) {
					c[i][j] += a[i][k] * b[k][j];
				}
			}
		}
		
		return c;
	}
	
	/*
	 * int K;
	 * 
	 * Matrix U, UT, V, VT; // U=dxk, UT=kxd, VT=kxt, V=txk
	 * Matrix S; // k
	 * 
	 * public SVD( int K ) {
	 * super( "SVD-"+K );
	 * this.K = K;
	 * }
	 * 
	 * public void train( List<HashMap<Integer,Double>> traindocs ) {
	 * Matrix A = new Matrix( traindocs.size(), WordIndex.size() );
	 * 
	 * for( int i = 0; i < traindocs.size(); i++ ) {
	 * HashMap<Integer,Double> hm = traindocs.get( i );
	 * 
	 * for( int j : hm.keySet() ) {
	 * A.set( i, j, hm.get( j ) );
	 * }
	 * }
	 * 
	 * SingularValueDecomposition svd = A.svd();
	 * U = svd.getU().getMatrix( 0, U.getRowDimension()-1, 0, K-1 );
	 * UT = U.transpose();
	 * V = svd.getV().getMatrix( 0, V.getRowDimension()-1, 0, K-1 );
	 * VT = V.transpose();
	 * S = new Matrix( new double[][]{ svd.getSingularValues() } ).getMatrix( 0, 0, 0, K-1 );
	 * }
	 * 
	 * public double[] predict( HashMap<Integer,Double> givenwords ) {
	 * Matrix C = new Matrix( 1, WordIndex.size() );
	 * 
	 * for( int j : givenwords.keySet() ) {
	 * C.set( 0, j, givenwords.get( j ) );
	 * }
	 * 
	 * C = C.times( V );
	 * C.arrayRightDivideEquals( S );
	 * C = C.times( VT );
	 * return C.getArray()[0];
	 * }
	 */
}
