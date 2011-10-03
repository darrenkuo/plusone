package plusone.utils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Term {
    /* Warning: doc_train might not do what you think (it can contain testing
     * documents).  See generateData in PaperAbstract. */
    List<PaperAbstract> doc_train;
    List<PaperAbstract> doc_test;
    int Id;
    public String word;
    public int totalCount;
    
    public Term(int Id, String word) {
	this.Id=Id;
	this.word=word;
	doc_train = new ArrayList<PaperAbstract>();
	doc_test= new ArrayList<PaperAbstract>();
	totalCount = 0;
    }
    
    public void addDoc(PaperAbstract doc, boolean test){
	if (test)
	    doc_test.add(doc);
	else
	    doc_train.add(doc);
    }
    
    public int idfRaw(){
	return doc_train.size();
    }

    public List<PaperAbstract> getDocTrain() { return doc_train; }
    public List<PaperAbstract> getDocTest() { return doc_train; }

    public SparseIntIntVec makeTrainingDocVec() {
	SparseIntIntVec ret = new SparseIntIntVec();
	for (PaperAbstract doc : doc_train) {
	    if (!doc.test)
		ret.addSingle(doc.indexInGlobalList, 1);
	}
	return ret;
    }
}
