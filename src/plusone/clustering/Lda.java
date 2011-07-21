package plusone.clustering;

import plusone.utils.Indexer;
import plusone.utils.PaperAbstract;
import plusone.utils.PlusoneFileWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lda implements ClusteringMethod {

    private Indexer<String> wordIndexer = new Indexer<String>();

    public void train(List<PaperAbstract> abstracts) {
	try {
	    new File("lda").mkdir();
	} catch(Exception e) {
	    e.printStackTrace();
	}

	PlusoneFileWriter fileWriter = 
	    new PlusoneFileWriter("lda/data.ldain");
	//String ldaInput = "";
	for (PaperAbstract paper : abstracts) {
	    String[] words = paper.abstractText.split(" ");
	    Map<Integer, Integer> counter =
		new HashMap<Integer, Integer>();
	    for (String word : words) {
		int index = wordIndexer.addAndGetIndex(word);

		if (counter.containsKey(index))
		    counter.put(index, counter.get(index) + 1);
		else
		    counter.put(index, 0);
	    }

	    fileWriter.write("" + counter.size());
	    //ldaInput += ("" + counter.size());
	    for(Map.Entry<Integer, Integer> entry : counter.entrySet())
		//ldaInput += (entry.getKey() + ":" + entry.getValue());
		fileWriter.write(entry.getKey() + ":" + entry.getValue());

	    fileWriter.write("\n");
	    //ldaInput += "\n";
	}

	fileWriter.close();

	try {
	    Process p = Runtime.getRuntime().exec("lib/lda-c-dist/lda est 1 10 lib/lda-c-dist/settings.txt lda/data.ldain random lda");
	    BufferedReader stdInput = new BufferedReader(new 
							 InputStreamReader(p.getInputStream()));
	    
	    BufferedReader stdError = new BufferedReader(new 
							 InputStreamReader(p.getErrorStream()));
	    System.out.println("Here is the standard output of the command:\n");
	    String s;
	    while ((s = stdInput.readLine()) != null) {
		System.out.println(s);
	    }
            
	    // read any errors from the attempted command
	    System.out.println("Here is the standard error of the command (if any):\n");
	    while ((s = stdError.readLine()) != null) {
		System.out.println(s);
	    }
            
	    System.exit(0);
	}
	catch (IOException e) {
	    System.out.println("exception happened - here's what I know: ");
	    e.printStackTrace();
	    System.exit(-1);
	}
    }
    
    public void test(List<PaperAbstract> abstracts) {
    }
}