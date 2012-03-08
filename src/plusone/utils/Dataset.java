package plusone.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;

public class Dataset {
    final static String index_pattern_string = "#INDEX ([\\d]+)";
    final static Pattern index_pattern = Pattern.compile(index_pattern_string);

    final static String inref_pattern_string = "#IN-REF ([\\d\\s]+)";
    final static Pattern inref_pattern = Pattern.compile(inref_pattern_string);

    final static String outref_pattern_string = "#OUT-REF ([\\d\\s]+)";
    final static Pattern outref_pattern = Pattern.compile(outref_pattern_string);

    final static String abstract_pattern_string = "#ABSTRACT ([\\s\\S]+)";
    final static Pattern abstract_pattern = Pattern.compile(abstract_pattern_string);

    class Paper {
	Integer[] inReferences;
	Integer[] outReferences;
	Integer[] abstractWords;
	Integer index;
	Integer group;

	public Paper(Integer index, Integer[] inReferences, 
		     Integer[] outReferences, Integer[] abstractWords) {
	    this.inReferences = inReferences;
	    this.outReferences = outReferences;
	    this.abstractWords = abstractWords;
	    this.index = index;
	}
    }

    /* Member fields. */

    List<PaperAbstract> documents = new ArrayList<PaperAbstract>();
    public List<PaperAbstract> getDocuments() { return documents; }

    Indexer<String> wordIndexer = new Indexer<String>();
    public Indexer<String> getWordIndexer() { return wordIndexer; }

    private Indexer<PaperAbstract> paperIndexer = 
	new Indexer<PaperAbstract>();
    public Indexer<PaperAbstract> getPaperIndexer() { return paperIndexer; }

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

    public static Dataset loadDatasetFromPath(String filename) {
        Dataset dataset = new Dataset();
        dataset.loadInPlaceFromPath(filename);
        return dataset;
    }
}
