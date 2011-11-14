package plusone.utils;

import java.util.ArrayList;
import java.util.List;

public class Term implements Comparable {

    /** 
     * Warning: doc_train might not do what you think (it can contain
     * testing documents).  See generateData in PaperAbstract.
     */
    private List<PaperAbstract> doc_train;
    private List<PaperAbstract> doc_test;

    public int id, totalCount = 0;
    public String word;
    
    public Term(int id) {
	this.id = id;
	doc_train = new ArrayList<PaperAbstract>();
	doc_test = new ArrayList<PaperAbstract>();
    }

    public int compareTo(Object obj) {
	return ((Term)obj).totalCount > totalCount ? 1 : -1;
    }
    
    public void addDoc(PaperAbstract doc, boolean test){
	if (test)
	    doc_test.add(doc);
	else
	    doc_train.add(doc);
    }
    
    public int idfRaw() { return doc_train.size(); }


    public List<PaperAbstract> getDocTrain() { return doc_train; }
}
