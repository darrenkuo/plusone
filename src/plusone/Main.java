package plusone;

import plusone.utils.Indexer;
import plusone.utils.KNNGraphDistanceCache;
import plusone.utils.KNNSimilarityCache;
import plusone.utils.MetadataLogger;
import plusone.utils.PaperAbstract;
import plusone.utils.PlusoneFileWriter;
import plusone.utils.PredictionPaper;
import plusone.utils.Terms;
import plusone.utils.TrainingPaper;
import java.util.Arrays;

import plusone.clustering.Baseline;
import plusone.clustering.ClusteringTest;
import plusone.clustering.CommonNeighbors;
import plusone.clustering.DTRandomWalkPredictor;
import plusone.clustering.KNN;
import plusone.clustering.KNNWithCitation;
import plusone.clustering.LSI;
//import plusone.clustering.SVDAndKNN;

//import plusone.clustering.Lda;
//import plusone.clustering.KNNRandomWalkPredictor;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private Indexer<String> wordIndexer = new Indexer<String>();
    private Indexer<PaperAbstract> paperIndexer = 
	new Indexer<PaperAbstract>();

    private Terms terms;
    private KNNSimilarityCache knnSimilarityCache;
    private KNNGraphDistanceCache knnGraphDistanceCache;
    private static MetadataLogger metadataLogger;
    private static Random randGen;

    // Document sets
    public List<TrainingPaper> trainingSet;
    public List<PredictionPaper> testingSet;

    // Clustering Methods
    private Baseline baseline;
    private CommonNeighbors cn;
    private DTRandomWalkPredictor dtRWPredictor;
    private KNN knn;
    private KNNWithCitation knnc;
    private LSI lsi;
    //private SVDAndKNN svdKnn;
    //private KNNRandomWalkPredictor knnRWPredictor;

    class Paper {
	Integer[] inReferences;
	Integer[] outReferences;
	Integer[] abstractWords;
	Integer index;

	public Paper(Integer index, Integer[] inReferences, 
		     Integer[] outReferences, Integer[] abstractWords) {
	    this.inReferences = inReferences;
	    this.outReferences = outReferences;
	    this.abstractWords = abstractWords;
	    this.index = index;
	}
    }

    public Main(long randomSeed) {
	randGen = new Random(randomSeed);
	metadataLogger = new MetadataLogger();
    }

    public void load_data(String filename, double trainPercent) {
	List<Paper> papers = new ArrayList<Paper>();

	String index_pattern_string = "#INDEX ([\\d]+)";
	Pattern index_pattern = Pattern.compile(index_pattern_string);

	String inref_pattern_string = "#IN-REF ([\\d\\s]+)";
	Pattern inref_pattern = Pattern.compile(inref_pattern_string);

	String outref_pattern_string = "#OUT-REF ([\\d\\s]+)";
	Pattern outref_pattern = Pattern.compile(outref_pattern_string);

	String abstract_pattern_string = "#ABSTRACT ([\\s\\S]+)";
	Pattern abstract_pattern = Pattern.compile(abstract_pattern_string);

	Indexer<Paper> tempPaperIndexer = new Indexer<Paper>();
	Map<Integer, Integer> paperIndexMap = new HashMap<Integer, Integer>();

	try {
	    FileInputStream fstream = new FileInputStream(filename);
	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = 
		new BufferedReader(new InputStreamReader(in));
	    String strLine;
	    
	    while ((strLine = br.readLine()) != null) {
		if (!strLine.equals( "<!--")) {
		    System.out.println("Bad start...error soon!");
		    System.out.println("found " + strLine + "instead.");
		}

		int index = -1;
		Integer[] inRef = null;
		Integer[] outRef = null;
		String abstractText = null;
		
		strLine = br.readLine();
		Matcher matcher = index_pattern.matcher(strLine);
		if (matcher.matches()) {
		    index = new Integer(matcher.group(1));
		}

		strLine = br.readLine();
		matcher = inref_pattern.matcher(strLine);
		if (matcher.matches()) {
		    String matched_string = matcher.group(1);
		    String[] array = matched_string.split(" ");
		    inRef = new Integer[array.length];
		    for (int i = 0; i < array.length; i ++) {
			inRef[i] = new Integer(array[i]);
		    }
		}

		strLine = br.readLine();
		matcher = outref_pattern.matcher(strLine);
		if (matcher.matches()) {
		    String matched_string = matcher.group(1);
		    String[] array = matched_string.split(" ");
		    outRef = new Integer[array.length];
		    for (int i = 0; i < array.length; i ++) {
			outRef[i] = new Integer(array[i]);
		    }
		}

		strLine = br.readLine();
		matcher = abstract_pattern.matcher(strLine);
		if (matcher.matches()) {
		    abstractText = matcher.group(1);
		}

		String[] words = abstractText.trim().split(" ");
		Integer[] abstractWords = new Integer[words.length];

		for (int i = 0; i < words.length; i ++) {
		    abstractWords[i] = 
			wordIndexer.fastAddAndGetIndex(words[i]);
		}

		strLine = br.readLine();

		inRef = inRef == null ? new Integer[0] : inRef;
		outRef = outRef == null ? new Integer[0] : outRef;

		Paper p = new Paper(index, inRef, outRef, abstractWords);

		papers.add(p);
		paperIndexMap.put(index, tempPaperIndexer.addAndGetIndex(p));
	    }
	    br.close();
	} catch(Exception e) {
	    e.printStackTrace();
	}

	List<PaperAbstract> documents = new ArrayList<PaperAbstract>();
	int inref_zero = 0;
	for (Paper a : papers) {
	    ArrayList<Integer> references = new ArrayList<Integer>();
	    for (int i = 0; i < a.inReferences.length; i ++) {
		Integer j = paperIndexMap.get(a.inReferences[i]);
		if (j != null)
		    references.add(j);
	    }
	    Integer[] inReferences =
		references.toArray(new Integer[references.size()]);

	    references = new ArrayList<Integer>();
	    for (int i = 0; i < a.outReferences.length; i ++) {
		Integer j = paperIndexMap.get(a.outReferences[i]);
		if (j != null)
		    references.add(j);	       
	    }
	    Integer[] outReferences =
		references.toArray(new Integer[references.size()]);

	    PaperAbstract p = new PaperAbstract(paperIndexMap.get(a.index),
						inReferences,
						outReferences,
						a.abstractWords);
		
	    documents.add(p);
	    paperIndexer.add(p);
	    inref_zero += inReferences.length == 0 ? 1 : 0;
	 //   inref_zero += outReferences.length == 0 ? 1 : 0;	    
	}
	System.out.println("inref zero: " + inref_zero);
	System.out.println("total number of papers: " + documents.size());

	splitByTrainPercent(trainPercent, documents);
    }

    /**
     * Splits all the documents into training and testing papers.
     * This function must be called before we can do execute any
     * clustering methods.
     */
    private void splitByTrainPercent(double trainPercent, 
				    List<PaperAbstract> documents) {
    Random randGen = Main.getRandomGenerator();
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

	if (testIsEnabled("knn") || testIsEnabled("knnc"))
	    knnSimilarityCache = 
		new KNNSimilarityCache(trainingSet, testingSet);

	if (testIsEnabled("knnc"))
	    knnGraphDistanceCache = 
		new KNNGraphDistanceCache(trainingSet, testingSet, 
					  paperIndexer);
    }
    
    public static Random getRandomGenerator() { return randGen; }

    public static MetadataLogger getMetadataLogger() { return metadataLogger; }

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

	/* FIXME: We probably should divide by k here, rather than the total
	 * number of predictions made; otherwise we reward methods that make
	 * less predictions.  -James */
	
	return new double[]{(double)predicted, idfScore, 
			    tfidfScore, (double)prediction.length}; 
   }

    public static void printResults(double[] results) {
	System.out.println("Predicted: " + results[0]);
	System.out.println("idf score: " + results[1]);
	System.out.println("tfidf score: " + results[2]);
    }

    public static void printResults(File output, double[] results) {
	PlusoneFileWriter writer = new PlusoneFileWriter(output);
	writer.write("Predicted: " + results[0] + "\n");
	writer.write("idf score: " + results[1] + "\n");
	writer.write("tfidf score: " + results[2] + "\n");
	writer.close();
    }

    public void runClusteringMethods(File outputDir, int k) {

	int size = trainingSet.size() + testingSet.size();
	if (testIsEnabled("baseline")) {
	    baseline = new Baseline(trainingSet, terms);
	    runClusteringMethod(testingSet, baseline, outputDir, k, size);
	    
	}
		
	if (testIsEnabled("dtrw")) {
	    int rwLength =
		Integer.getInteger("plusone.dtrw.walkLength", 4);
	    boolean stoch = Boolean.getBoolean("plusone.dtrw.stochastic");
	    int nSampleWalks = Integer.getInteger("plusone.dtrw.nSampleWalks");
	    System.out.println("Random walk length: " + rwLength);
	    if (stoch)
		System.out.println("Stochastic random walk: " + nSampleWalks + " samples.");
	    dtRWPredictor =
		new DTRandomWalkPredictor(trainingSet, terms, rwLength, stoch, nSampleWalks);
	    runClusteringMethod(testingSet, dtRWPredictor, 
		    outputDir, k, size);
	}


	int[] closest_k = 
	    parseIntList(System.getProperty("plusone.closestKValues", 
					    "1,3,5,10,25,50,100,250,500,1000,10000,100000"));

	for (int ck = 0; ck < closest_k.length; ck ++) {
	    if (testIsEnabled("knn")) {
		knn = new KNN(closest_k[ck], trainingSet, paperIndexer, 
			      terms, knnSimilarityCache);
		runClusteringMethod(testingSet, knn, outputDir, k, size);
	    }
	    if (testIsEnabled("knnc")) {
		knnc = new KNNWithCitation(closest_k[ck], trainingSet,
					   paperIndexer, knnSimilarityCache,
					   knnGraphDistanceCache, terms);
		runClusteringMethod(testingSet, knnc, outputDir, k, size);
	    }

	    if (testIsEnabled("cn")) {
		cn = new CommonNeighbors(closest_k[ck], trainingSet, paperIndexer,
					 knnSimilarityCache, 
					 knnGraphDistanceCache, terms);
		runClusteringMethod(testingSet, cn, outputDir, k, size);
	    }
	    

	    /*
	    if (testIsEnabled("svdknn")) {
		svdKnn = new SVDAndKNN(closest_k[ck], trainingSet);
		runClusteringMethod(testingSet, knnc, outputDir, k, size);
	    }
	    */		    
	    /*
	    if (testIsEnabled("knnrw")) {
		knnRWPredictor =
		    new KNNRandomWalkPredictor(closest_k[ck], trainingSet,
					       wordIndexer, paperIndexer,
					       1, 0.5, 1);
		runClusteringMethod(trainingSet, testingSet,
				    knnRWPredictor, outputDir, k, usedWord);
	    }
	    */
	}

	int[] dimensions = {10, 20, 25, 50, 100, 150};
	for (int dk = 0; dk < dimensions.length; dk ++) {
	    if (testIsEnabled("lsi")) {
		lsi = new LSI(dimensions[dk], trainingSet, terms);

		runClusteringMethod(testingSet, lsi, outputDir, k, size);
	    }
	}
    }

    public void runClusteringMethod(List<PredictionPaper> testingSet,
				    ClusteringTest test, File outputDir,
				    int k, int size) {
	long t1 = System.currentTimeMillis();
	System.out.println("[" + test.testName + "] starting test" );
	double[] results = {0.0, 0.0, 0.0, 0.0};
	MetadataLogger.TestMetadata meta = getMetadataLogger().getTestMetadata(test.testName);
	test.addMetadata(meta);
	List<Double> predictionScores = new ArrayList<Double>();
	for (PredictionPaper testingPaper : testingSet) {
	    Integer[] predict = test.predict(k, testingPaper);
	    double[] result = evaluate(testingPaper, predict, size, k);
	    results[0] += result[0];
	    results[1] += result[1];
	    results[2] += result[2];
	    results[3] += result[3];
	    predictionScores.add(result[0]);
	}
	meta.createListValueEntry("predictionScore", predictionScores.toArray());
	
	File out = new File(outputDir, test.testName + ".out");
	Main.printResults(out, new double[]{results[0]/results[3], 
					    results[1], results[2]});
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

    /*
     * data - args[0]
     * train percent - args[1]
     * test word percent - args[2] (currently ignored)
     */
    public static void main(String[] args) {

	String data_file = System.getProperty("plusone.dataFile", "");

	if (!new File(data_file).exists()) {
	    System.out.println("Data file does not exist.");
	    System.exit(0);
	}

	long randSeed = 
	    new Long(System.getProperty("plusone.randomSeed", "0"));
	Main main = new Main(randSeed);

	double trainPercent = new Double
	    (System.getProperty("plusone.trainPercent", "0.95"));
	String experimentPath = System.getProperty("plusone.outPath", 
						   "experiment");

	main.load_data(data_file, trainPercent);
	System.out.println("data file " + data_file);
	System.out.println("train percent " + trainPercent);
	//System.out.println("test word percent " + testWordPercent);


	/* These values can be set on the command line.  For example, to set
	 * testWordPercents to {0.4,0.5}, pass the command-line argument
	 * -Dplusone.testWordPercents=0.4,0.5 to java (before the class name)
	 */
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

	    for (int ki = 0; ki < ks.length; ki ++) {
		int k = ks[ki];

		File kDir = null;
		try {
		    kDir = new File(twpDir, k + "");
		    kDir.mkdir();
		} catch(Exception e) {
		    e.printStackTrace();
		}

		System.out.println("processing testwordpercent: " + 
				   testWordPercent + " k: " + k);

		main.runClusteringMethods(kDir, k);
	    }
	}

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