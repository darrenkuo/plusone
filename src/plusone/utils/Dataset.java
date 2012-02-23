package plusone.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Dataset {
    final static String index_pattern_string = "#INDEX ([\\d]+)";
    final static Pattern index_pattern = Pattern.compile(index_pattern_string);

    final static String inref_pattern_string = "#IN-REF ([\\d\\s]+)";
    final static Pattern inref_pattern = Pattern.compile(inref_pattern_string);

    final static String outref_pattern_string = "#OUT-REF ([\\d\\s]+)";
    final static Pattern outref_pattern = Pattern.compile(outref_pattern_string);

    final static String abstract_pattern_string = "#ABSTRACT ([\\s\\S]+)";
    final static Pattern abstract_pattern = Pattern.compile(abstract_pattern_string);

    final static String baseDatasetUrl = "http://www.falsifian.org/a/9xwk/";

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

    public class TrainingAndTesting {
	List<TrainingPaper> trainingSet;
	public List<TrainingPaper> getTrainingSet() { return trainingSet; }
        
	List<PredictionPaper> testingSet;
	public List<PredictionPaper> getTestingSet() { return testingSet; }
        
        public TrainingAndTesting(List<TrainingPaper> trainingSet,
                                  List<PredictionPaper> testingSet) {
            this.trainingSet = trainingSet;
            this.testingSet = testingSet;
        }
    }

    /* Member fields. */

    List<PaperAbstract> documents;
    public List<PaperAbstract> getDocuments() { return documents; }

    Indexer<String> wordIndexer;
    public Indexer<String> getWordIndexer() { return wordIndexer; }

    private Indexer<PaperAbstract> paperIndexer;
    public Indexer<PaperAbstract> getPaperIndexer() { return paperIndexer; }

    /* Constructors. */

    public Dataset(List<PaperAbstract> documents, Indexer<String> wordIndexer,
                   Indexer<PaperAbstract> paperIndexer) {
        this.documents = documents;
        this.wordIndexer = wordIndexer;
        this.paperIndexer = paperIndexer;
    }

    public Dataset() {
        this(new ArrayList<PaperAbstract>(), new Indexer<String>(),
             new Indexer<PaperAbstract>());
    }

    /* More public methods. */

    /** Returns a dataset with the first nPapers papers.  The new dataset is
     * backed by this one, so changes to this dataset may affect the returned
     * dataset.
     */
    public Dataset take(int nPapers) {
        return new Dataset(documents.subList(0, Math.min(documents.size(), nPapers)),
                           wordIndexer, paperIndexer);
    }

    /**
     * Splits all the documents into training and testing papers.
     */
    public TrainingAndTesting splitByTrainPercent(double trainPercent, Random randGen) {
	List<TrainingPaper> trainingSet = new ArrayList<TrainingPaper>();
	List<PredictionPaper> testingSet = new ArrayList<PredictionPaper>();
	for (int i = 0; i < documents.size(); i ++) {
	    if (randGen.nextDouble() < trainPercent)
		trainingSet.add((TrainingPaper)documents.get(i));
	    else
		testingSet.add((PredictionPaper)documents.get(i));
	}
        return new TrainingAndTesting(trainingSet, testingSet);
    }

    public static Dataset loadDatasetFromPath(String filename) {
        Dataset dataset = new Dataset();
        dataset.loadInPlaceFromPath(filename);
        return dataset;
    }

    public static Dataset loadDatasetFromName(String datasetName) {
        String datasetPath = "/tmp/" + datasetName + "." + System.getProperty("user.name");
        if (!new File(datasetPath).exists())
            Fetch.fetchUrl(baseDatasetUrl + datasetName, datasetPath);
        return loadDatasetFromPath(datasetPath);
    }

    /* Private helper functions. */

    /* Private method used by loadDataset. */
    void loadInPlaceFromPath(String filename) {
	List<Paper> papers = new ArrayList<Paper>();

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
    }
}
