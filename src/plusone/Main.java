package plusone;

import plusone.utils.DatasetJSON;
import plusone.utils.Indexer;
import plusone.utils.ItemAndScore;
import plusone.utils.KNNGraphDistanceCache;
import plusone.utils.KNNSimilarityCache;
import plusone.utils.KNNSimilarityCacheLocalSVDish;
import plusone.utils.MetadataLogger;
import plusone.utils.PaperAbstract;
import plusone.utils.PlusoneFileWriter;
import plusone.utils.PredictionPaper;
import plusone.utils.Terms;
import plusone.utils.TrainingPaper;
import plusone.utils.LocalSVDish;
import plusone.utils.Results;
import plusone.utils.LocalCOSample;

import plusone.clustering.Baseline;
import plusone.clustering.ClusteringTest;
import plusone.clustering.CommonNeighbors;
import plusone.clustering.DTRandomWalkPredictor;
import plusone.clustering.KNN;
import plusone.clustering.KNNLocalSVDish;
import plusone.clustering.KNNWithCitation;
import plusone.clustering.LSI;
import plusone.clustering.CO;
import plusone.clustering.PLSI;
//import plusone.clustering.SVDAndKNN;

import plusone.clustering.Lda;
//import plusone.clustering.KNNRandomWalkPredictor;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import org.json.*;


public class Main {

	//   private final static int nTrialsPerPaper = 12;
	private static int nTrials;
	private static Indexer<String> wordIndexer;
	private static Indexer<PaperAbstract> paperIndexer;

	private static Terms terms;

	private static MetadataLogger metadataLogger;
	private static Random randGen;

	private Map<PaperAbstract, Integer> indices;
	private Map<String,Results>[] allResults;
	// Document sets
	public List<TrainingPaper> trainingSet;
	public List<PredictionPaper> testingSet;
	
	private double ldaPerplexity;

	private static int FOLD; // cross validation parameter
	private static DatasetJSON dataset;

	public static void load_data(String filename) {
		dataset = DatasetJSON.loadDatasetFromPath(filename);
		wordIndexer = dataset.getWordIndexer();
		paperIndexer = dataset.getPaperIndexer();
	}

	private void setupData(int testGroup, double testWordPercent){
		System.out.println("Preparing data...");
		List<PaperAbstract> documents = dataset.getDocuments();
		// split into training documents and testing documents
		trainingSet=new ArrayList<TrainingPaper>();
		testingSet = new ArrayList<PredictionPaper>();
		for (int i = 0; i < documents.size(); i ++) {
			indices.put(documents.get(i), i);
			if (documents.get(i).getGroup()==testGroup)
				testingSet.add((PredictionPaper)documents.get(i));
			else
				trainingSet.add((TrainingPaper)documents.get(i));		
		}
		
		System.out.println("Training size:" + trainingSet.size());
		System.out.println("Testing size:" + testingSet.size());
		
		// Held out words
		Terms.Term[] terms = new Terms.Term[wordIndexer.size()];
		for (int i = 0; i < wordIndexer.size(); i++) {
			terms[i] = new Terms.Term(i);
		}

		for (TrainingPaper a : trainingSet){
			((PaperAbstract)a).generateTf(testWordPercent, terms, false);
		}

		for (PredictionPaper a : testingSet){
			((PaperAbstract)a).generateTf(testWordPercent, null, true);
		}
		this.terms = new Terms(terms);
		System.out.println("Data ready for experiment");
	}

	private void runExperiments(String path) {
		Boolean crossValid = Boolean.getBoolean("plusone.crossValidation.run");

		if (crossValid)
			System.out.println("We will do "+FOLD+"-fold cross validation");
		double[] testWordPercents = 
				parseDoubleList(System.getProperty("plusone.testWordPercents", 
						"0.3,0.5"));
		int[] ks = 
				parseIntList(System.getProperty("plusone.kValues", 
						"1,3,5,10"));
		Arrays.sort(ks);
		allResults = new Map[ks.length];
		for (int twp = 0; twp < testWordPercents.length; twp++) {
			double testWordPercent = testWordPercents[twp];
			System.out.println("processing testwordpercent: " + testWordPercent);
			for (int i=0;i<ks.length;i++)
				allResults[i]=new HashMap<String,Results>();

			for (int testGroup=(crossValid ?0:FOLD-1);testGroup<FOLD;testGroup++){	    
				setupData(testGroup,testWordPercent);

				runClusteringMethods(ks);

			}
		}
		outputResults(ks, testWordPercents);
	}

	/** Outputs the results of the tests into the data folder.
	 * 
	 * @param ks an array containing how many words each test should predict
	 * @param twpNames an array containing the percentage of held out words for each test
	 * @throws JSONException
	 */
	private void outputResults(int[] ks, double[] twpNames) {
		try {
			JSONObject json = new JSONObject();
			JSONArray tests = new JSONArray();
			for (int i = 0; i < twpNames.length; i++) {
				for(int ki=0;ki<ks.length;ki++){
					JSONObject allTests = new JSONObject();
					int k=ks[ki];
					Map<String,Results> resultK=allResults[ki];
					for (Map.Entry<String,Results> entry : resultK.entrySet()){
						JSONObject thisTest = new JSONObject();
						thisTest.put("numPredictions", k);
						thisTest.put("trainPercent", twpNames[i]);
						double[] mean = entry.getValue().getResultsMean();
						double[] variance = entry.getValue().getResultsVariance();
						thisTest.put("Predicted_Mean" , mean[0]);
						thisTest.put("idf score_Mean" , mean[1]);
						thisTest.put("tfidf score_Mean" , mean[2]);
						thisTest.put("Predicted_Var" , variance[0]);
						thisTest.put("idf score_Var" , variance[1]);
						thisTest.put("tfidf score_Var" , variance[2]);
						if (entry.getKey().equals("Lda")) {
							thisTest.put("Perplexity", ldaPerplexity);
						}
						allTests.put(entry.getKey(), thisTest);
					}
					tests.put(allTests);
				}
			}
			json.put("tests", tests);
			Date date = new Date();
			String outName = "experiment" + date.getTime() +".json";
			File out = new File("data", outName);
			System.out.println("Wrote to " + outName);
	
			PlusoneFileWriter writer = new PlusoneFileWriter(out);
			writer.write(json.toString());
			writer.close();
		} catch (JSONException e) {
			System.out.println("Error writing to output");
		}
	}


	public void runClusteringMethods(int[] ks) {
		int size = trainingSet.size() + testingSet.size();
		// Baseline
		if (testIsEnabled("baseline")) {
			Baseline baseline = new Baseline(trainingSet, terms);
			runClusteringMethod(baseline, ks, size, false);
		}

		// KNN
		KNNSimilarityCache knnSimilarityCache = null;
		if (testIsEnabled("knn") || testIsEnabled("knnc"))
			knnSimilarityCache = 
			new KNNSimilarityCache(trainingSet, testingSet);
		KNN knn;
		int[] closest_k =   parseIntList(System.getProperty("plusone.closestKValues", 
				"1,3,5,10,25,50,100,250,500,1000"));
		for (int ck = 0; ck < closest_k.length; ck ++) {
			if (testIsEnabled("knn")) {
				knn = new KNN(closest_k[ck], trainingSet, paperIndexer, 
						terms, knnSimilarityCache);
				runClusteringMethod(knn, ks, size, false);
			}
		}

		// Local Co-Occurance
		CO co;
		if (testIsEnabled("localCO")){
			co=new CO(Integer.getInteger("plusone.localCO.docEnzs"),
					Integer.getInteger("plusone.localCO.termEnzs"),
					Integer.getInteger("plusone.localCO.dtNs"),
					Integer.getInteger("plusone.localCO.tdNs"),
					trainingSet, terms);
			runClusteringMethod(co,ks,size,false);
		}
		// LSI
		LSI lsi;
		if (testIsEnabled("lsi")){
			int[] dimensions = parseIntList(System.getProperty("plusone.svdDimensions", 
					"1,5,10,20"));
			for (int dk = 0; dk < dimensions.length; dk ++) {

				lsi = new LSI(dimensions[dk], trainingSet, terms);

				runClusteringMethod(lsi, ks, size,false);

			}
		}
		//PLSI
		PLSI plsi;
		if (testIsEnabled("plsi")){
			int[] dimensions = parseIntList(System.getProperty("plusone.plsi.dimensions", 
					"1,5,10,20"));
			plsi = new PLSI(trainingSet, terms.size());
			for (int dk = 0; dk < dimensions.length; dk ++) {
				plsi.train(dimensions[dk]);
				runClusteringMethod(plsi, ks, size, false);

			}
		}
		//lda
		Lda lda = null;
		if (testIsEnabled("lda")){
			int[] dimensions = parseIntList(System.getProperty("plusone.lda.dimensions", 
					"20"));
			for (int dk = 0; dk < dimensions.length; dk ++) {
				lda = new Lda(trainingSet, wordIndexer, terms, dimensions[dk],
						indices);
				runClusteringMethod(lda, ks, size, true);

			}
			ldaPerplexity = lda.getPerplexity();
		}
		// KNNSVDish
		int[] closest_k_svdish = parseIntList(System.getProperty("plusone.closestKSVDishValues", 
				"1,3,5,10,25,50,100,250,500,1000"));
		KNNSimilarityCacheLocalSVDish KNNSVDcache = null;
		LocalSVDish localSVD;
		KNNLocalSVDish knnSVD;
		if (testIsEnabled("svdishknn")){
			localSVD=new LocalSVDish(Integer.getInteger("plusone.svdishknn.nLevels"),
					parseIntList(System.getProperty("plusone.svdishknn.docEnzs")),
					parseIntList(System.getProperty("plusone.svdishknn.termEnzs")),
					parseIntList(System.getProperty("plusone.svdishknn.dtNs")),
					parseIntList(System.getProperty("plusone.svdishknn.tdNs")),
					parseIntList(System.getProperty("plusone.svdishknn.numLVecs")),
					trainingSet,terms.size(),
					Integer.getInteger("plusone.svdishknn.walkLength"));
			KNNSVDcache = new KNNSimilarityCacheLocalSVDish(trainingSet,testingSet,localSVD);
		}
		for (int ck = 0; ck < closest_k_svdish.length; ck ++) {
			if (testIsEnabled("svdishknn")){
				knnSVD= new KNNLocalSVDish(closest_k_svdish[ck],trainingSet, paperIndexer,
						terms, KNNSVDcache);
				runClusteringMethod(knnSVD, ks,size,false);
			}
		}


		/*CommonNeighbors cn;
	  DTRandomWalkPredictor dtRWPredictor;
	  KNN knn;
	  KNNWithCitation knnc;
	  LSI lsi;



	  KNNGraphDistanceCache knnGraphDistanceCache;


	  if (testIsEnabled("knnc"))
	  knnGraphDistanceCache = 
	  new KNNGraphDistanceCache(trainingSet, testingSet, 
	  paperIndexer);



	  if (testIsEnabled("dtrw")) {
	  int rwLength =
	  Integer.getInteger("plusone.dtrw.walkLength", 4);
	  boolean stoch = Boolean.getBoolean("plusone.dtrw.stochastic");
	  int nSampleWalks = Integer.getInteger("plusone.dtrw.nSampleWalks");
	  System.out.println("Random walk length: " + rwLength);
	  if (stoch)
	  System.out.println("Stochastic random walk: " + nSampleWalks + " samples.");
	  boolean finalIdf = Boolean.getBoolean("plusone.dtrw.finalIdf");
	  boolean ndiw = Boolean.getBoolean("plusone.dtrw.normalizeDocsInWord");
	  Boolean nwid = Boolean.getBoolean("plusone.dtrw.normalizeWordsInDoc");
	  dtRWPredictor =
	  new DTRandomWalkPredictor(trainingSet, terms, rwLength, stoch, nSampleWalks, finalIdf, nwid, ndiw);
	  runClusteringMethod(testingSet, dtRWPredictor, 
	  outputDir, ks, size);
	  }









	  for (int ck = 0; ck < closest_k.length; ck ++) {
	  if (testIsEnabled("knn")) {
	  knn = new KNN(closest_k[ck], trainingSet, paperIndexer, 
	  terms, knnSimilarityCache);
	  runClusteringMethod(testingSet, knn, outputDir, ks, size);
	  }
	  if (testIsEnabled("knnc")) {
	  knnc = new KNNWithCitation(closest_k[ck], trainingSet,
	  paperIndexer, knnSimilarityCache,
	  knnGraphDistanceCache, terms);
	  runClusteringMethod(testingSet, knnc, outputDir, ks, size);
	  }*/

		/*	    if (testIsEnabled("cn")) {
		    cn = new CommonNeighbors(closest_k[ck], trainingSet, paperIndexer,
		    knnSimilarityCache, 
		    knnGraphDistanceCache, terms);
		    runClusteringMethod(testingSet, cn, outputDir, ks, size);
		    }



		    if (testIsEnabled("svdknn")) {
		    svdKnn = new SVDAndKNN(closest_k[ck], trainingSet);
		    runClusteringMethod(testingSet, knnc, outputDir, ks, size);
		    }


		    if (testIsEnabled("knnrw")) {
		    knnRWPredictor =
		    new KNNRandomWalkPredictor(closest_k[ck], trainingSet,
		    wordIndexer, paperIndexer,
		    1, 0.5, 1);
		    runClusteringMethod(trainingSet, testingSet,
		    knnRWPredictor, outputDir, ks, usedWord);
		    }*/

		/*	}




		 */


	}
	private void logResult(int ki,String expName, double[] result){
		if (!allResults[ki].containsKey(expName))
			allResults[ki].put(expName,new Results(expName));
		allResults[ki].get(expName).addResult(result[0],result[1],result[2]);
	}

	public void runClusteringMethod(ClusteringTest test, int[] ks, int size, boolean bulk) {
		long t1 = System.currentTimeMillis();
		System.out.println("[" + test.testName + "] starting test" );
		double[][] allScores=new double[testingSet.size()][terms.size()];
		if (bulk){
			allScores = test.predict(testingSet);
		}

		double[][] results = new double[ks.length][4];

		for (int id=0;id<testingSet.size();id++){
			PredictionPaper testingPaper=testingSet.get(id);
			double[] itemScores;
			if (!bulk)
				itemScores= test.predict(testingPaper);
			else
				itemScores=allScores[id];


			int largestK=ks[ks.length-1];
			Queue<ItemAndScore> queue = new PriorityQueue<ItemAndScore>(largestK + 1);
			for (int i=0;i<itemScores.length;i++) {
				if (testingPaper.getTrainingTf(i) > 0.0)
					continue;

				if (queue.size() < largestK || 
						(double)itemScores[i] > queue.peek().score) {
					if (queue.size() >= largestK)
						queue.poll();
					queue.add(new ItemAndScore(i, itemScores[i], true));
				}
			}

			List<Integer> topPrdcts = new ArrayList<Integer>();
			while (!queue.isEmpty()) {
				topPrdcts.add(0,(Integer)queue.poll().item);
			}

			for (int ki = 0; ki < ks.length; ki ++) {
				int k = ks[ki];

//				MetadataLogger.TestMetadata meta = getMetadataLogger().getTestMetadata("k=" + k + test.testName);
//				test.addMetadata(meta);
//				List<Double> predictionScores = new ArrayList<Double>();

				Integer[] predict = topPrdcts.subList(0, k).toArray(new Integer[k]);

				double[] result = evaluate(testingPaper, predict, size, k);
				for (int j = 0; j < 4; ++j) results[ki][j] += result[j];
			}
		}
				//				predictionScores.add(result[0]);

//				meta.createListValueEntry("predictionScore", predictionScores.toArray());
//				meta.createSingleValueEntry("numPredict", k);
		for (int ki=0;ki<ks.length;ki++){
				//File out = new File(kDir, test.testName + ".out");
				int k=ks[ki];
				this.logResult(ki, test.getName(), new double[]{results[ki][0]/k/testingSet.size(), 
					results[ki][1]/k/testingSet.size(), results[ki][2]/k/testingSet.size()});
				//Main.printResults(out, new double[]{results[0]/results[3], 
				//		results[1], results[2]});
		}
		System.out.println("[" + test.testName + "] took " +
				(System.currentTimeMillis() - t1) / 1000.0 
				+ " seconds.");
	}




	static double[] parseDoubleList(String s) {
		String[] tokens = s.split(",");
		double[] ret = new double[tokens.length];
		for (int i = 0; i < tokens.length; ++ i) {
			ret[i] = Double.valueOf(tokens[i]);
		}
		return ret;
	}

	static int[] parseIntList(String s) {
		String[] tokens = s.split(",");
		int[] ret = new int[tokens.length];
		for (int i = 0; i < tokens.length; ++ i) {
			ret[i] = Integer.valueOf(tokens[i]);
		}
		return ret;
	}

	static Boolean testIsEnabled(String testName) {
		return Boolean.getBoolean("plusone.enableTest." + testName);
	}

	public static void printResults(Results results) {
		double[] mean = results.getResultsMean();
		double[] variance = results.getResultsVariance();
		System.out.println("Predicted_Mean: " + mean[0]);
		System.out.println("idf score_Mean: " + mean[1]);
		System.out.println("tfidf score_Mean: " + mean[2]);
		System.out.println("Predicted_Var: " + variance[0]);
		System.out.println("idf score_Var: " + variance[1]);
		System.out.println("tfidf score_Var: " + variance[2]);

	}

	public static void printResults(File output, Results results) {
		PlusoneFileWriter writer = new PlusoneFileWriter(output);
		double[] mean = results.getResultsMean();
		double[] variance = results.getResultsVariance();

		writer.write("Predicted_Mean: " + mean[0] + "\n");
		writer.write("idf score_Mean: " + mean[1] + "\n");
		writer.write("tfidf score_Mean: " + mean[2] + "\n");
		writer.write("Predicted_Var: " + variance[0] + "\n");
		writer.write("idf score_Var: " + variance[1] + "\n");
		writer.write("tfidf score_Var: " + variance[2] + "\n");
		writer.close();
	}

	/* FIXME: We probably should divide by k here, rather than the total
	 * number of predictions made; otherwise we reward methods that make
	 * less predictions.  -James */
	public double[] evaluate(PredictionPaper testingPaper,
			Integer[] prediction, int size, int k) {
		int predicted = 0, total = 0;
		double tfidfScore = 0.0, idfScore = 0.0, idf_top =  Math.log(size);

		Set<Integer> givenWords = testingPaper.getTrainingWords();
		Set<Integer> predictionWords = ((PaperAbstract)testingPaper).
				getTestingWords();
		for (int j = 0; j < prediction.length && j < k; j ++) {
			Integer word = prediction[j];
			if (predictionWords.contains(word)) {
				predicted ++;
				double logVal = Math.log(terms.get(word).idfRaw() + 1.0);

				tfidfScore += ((PaperAbstract)testingPaper).
						getTestingTf(word) * 
						(idf_top - logVal);
				idfScore += (idf_top - logVal);
			}
		}

		return new double[]{(double)predicted, idfScore, 
				tfidfScore, (double)prediction.length}; 
	}


	public static Random getRandomGenerator() { return randGen; }

	public static MetadataLogger getMetadataLogger() { return metadataLogger; }

	// -------------------------------------------------------------------------------------------------------------
	/**
	 * Splits all the documents into training and testing papers.
	 * This function must be called before we can do execute any
	 * clustering methods.
	 */
	private void splitByTrainPercent(double trainPercent, 
			List<PaperAbstract> documents) {
		//    Random randGen = Main.getRandomGenerator();
		trainingSet = new ArrayList<TrainingPaper>();
		testingSet = new ArrayList<PredictionPaper>();
		for (int i = 0; i < documents.size(); i ++) {
			if (randGen.nextDouble() < trainPercent)
				trainingSet.add((TrainingPaper)documents.get(i));
			else
				testingSet.add((PredictionPaper)documents.get(i));
		}
		System.out.println("trainingSet size: " + trainingSet.size());
		System.out.println("testingSet size: " + testingSet.size());
	}

	public void splitHeldoutWords(double testWordPercent) {
		Terms.Term[] terms = new Terms.Term[wordIndexer.size()];
		for (int i = 0; i < wordIndexer.size(); i++) {
			terms[i] = new Terms.Term(i);
		}

		for (TrainingPaper a : trainingSet){
			((PaperAbstract)a).generateTf(testWordPercent, terms, false);
		}

		for (PredictionPaper a : testingSet){
			((PaperAbstract)a).generateTf(testWordPercent, null, true);
		}

		/*
	  terms_sorted = new Term[terms.length];

	  for (int c = 0; c < terms.length; c ++) {
	  terms_sorted[c] = terms[c];
	  }
	  Arrays.sort(terms_sorted);
		 */
		this.terms = new Terms(terms);



	}

	/*
	 * data - args[0]
	 * train percent - args[1]
	 * test word percent - args[2] (currently ignored)
	 */
	public static void main(String[] args) {

		String data_file = System.getProperty("plusone.dataFile", "med.out");

		if (!new File(data_file).exists()) {
			System.out.println("Data file does not exist.");
			System.exit(0);
		}

		long randSeed = 
				new Long(System.getProperty("plusone.randomSeed", "0"));

		randGen = new Random(randSeed);
		metadataLogger = new MetadataLogger();

		Main main = new Main();

		FOLD =  new Integer(System.getProperty("plusone.crossValidation.FOLD","10"));
		//	double trainPercent = new Double
		//    (System.getProperty("plusone.trainPercent", "0.95"));
		String experimentPath = System.getProperty("plusone.outPath", 
				"experiment");

		load_data(data_file);
		//main.splitByTrainPercent(trainPercent, dataset.getDocuments());
		System.out.println("data file " + data_file);
		//System.out.println("train percent " + trainPercent);
		//System.out.println("test word percent " + testWordPercent);
		System.out.println("Number of Documents: "+ dataset.getDocuments().size());
		System.out.println("Wordindexer size: " + wordIndexer.size());

		for (PaperAbstract paper:dataset.getDocuments())
			paper.setGroup(randGen.nextInt(FOLD));

		main.indices = new HashMap<PaperAbstract, Integer>();
		main.runExperiments(experimentPath);
		/* These values can be set on the command line.  For example, to set
		 * testWordPercents to {0.4,0.5}, pass the command-line argument
		 * -Dplusone.testWordPercents=0.4,0.5 to java (before the class name)
		 */
		/*
	  double[] testWordPercents = 
	  parseDoubleList(System.getProperty("plusone.testWordPercents", 
	  "0.1,0.3,0.5,0.7,0.9"));
	  int[] ks = 
	  parseIntList(System.getProperty("plusone.kValues", 
	  "1,5,10,15,20"));

	  for (int twp = 0; twp < testWordPercents.length; twp++) {
	  double testWordPercent = testWordPercents[twp];	    
	  File twpDir = null;
	  try {
	  twpDir = new File(new File(experimentPath), 
	  testWordPercent + "");
	  twpDir.mkdir();
	  } catch(Exception e) {
	  e.printStackTrace();
	  }

	  main.splitHeldoutWords(testWordPercent);


	  System.out.println("processing testwordpercent: " + 
	  testWordPercent);

	  main.runClusteringMethods(twpDir, ks);

	  }*/

		if (Boolean.getBoolean("plusone.dumpMeta")) {
			PlusoneFileWriter writer = 
					new PlusoneFileWriter(new File(new File(experimentPath),
							"metadata"));
			writer.write("var v = ");
			writer.write(Main.getMetadataLogger().getJson());
			writer.close();
		}
	}


}