package plusone.utils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Term {

    /* Warning: doc_train might not do what you think (it can contain
     * testing documents).  See generateData in PaperAbstract. */
    List<PaperAbstract> doc_train;
    List<PaperAbstract> doc_test;
    public int id, totalCount;
    public String word;
    
    public Term(int id) {
	this.id = id;
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
    
    public int idfRaw() {
	return doc_train.size();
    }

    public double trainingIdf(int nDocs) {
        return Math.log(nDocs / (double)idfRaw());
    }

    public List<PaperAbstract> getDocTrain() { return doc_train; }
    public List<PaperAbstract> getDocTest() { return doc_train; }

    public SparseVec makeTrainingDocVec(boolean useFreqs) {
	SparseVec ret = new SparseVec();
	for (PaperAbstract doc : doc_train) {
	    if (!doc.isTest())
		ret.addSingle(doc.index, useFreqs ?  doc.getTrainingTf(id) 
			      : 1.0);
	}
	return ret;
    }
}
