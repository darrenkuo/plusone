package plusone.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

import plusone.clustering.Lda;

public class Test {

	public static void main(String[] args) {
		Terms terms;
		
		//DatasetJSON reader = DatasetJSON.loadDatasetFromPath("/Users/andrewgambardella/Research/plusone/data/reg_movielens5.json");
		DatasetJSON reader = DatasetJSON.loadDatasetFromPath("/Users/andrewgambardella/Desktop/documents-out.json");
		//DatasetJSON reader = DatasetJSON.loadDatasetFromPath("data/reg_simple.json");
		Indexer<String> wordIndexer = reader.getWordIndexer();
		Indexer<PaperAbstract> paperIndexer = reader.getPaperIndexer();
		List<PaperAbstract> documents = reader.getDocuments();
		Terms.Term[] termArray = new Terms.Term[wordIndexer.size()];
		for (int i = 0; i < wordIndexer.size(); i++) {
		    termArray[i] = new Terms.Term(i);
		}
		terms = new Terms(termArray);
		
		Lda alg = new Lda(documents, wordIndexer, terms);
		double[][] test = alg.predict(documents);
		for (int row=0; row<test.length; row++) {
			for (int col=0; col < test[row].length; col++) {
				System.out.print(test[row][col] + " ");
			}
			System.out.println();
		}
	}
}

