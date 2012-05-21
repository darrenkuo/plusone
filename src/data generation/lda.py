import sys

import math
from math import e
from math import gamma

import operator

import random
from random import random as rand
from random import sample as rsample

import numpy as np
from numpy.random.mtrand import dirichlet

class Poisson(object):
    def __init__(self, L=15):
        self.lamb = L
        
    def sample(self):
        L = e ** (-self.lamb)
        k, p = 1, rand()
        while p > L:
            k += 1
            p *= rand()
        return k - 1

def sample(dist):
    """takes a list of probabilities and samples from it by index
       assumes a multinomial distribution
    Documentation TODO
    """
    p = rand()
    res = 0
    for i in range(len(dist)):
        res += dist[i]
        if res > p:
            return i
    #for debugging purposes only *SHOULD NOT REACH THIS LINE*
    print res, p

def count(words):
    word_count = {}
    num_words = 0
    for word in words:
        num_words += 1
        if word_count.has_key(word):
            word_count[word] += 1
        else:
            word_count[word] = 1
    word_count["total"] = num_words
    return word_count

def generate_docs(num_topics, num_docs, words_per_doc=50, vocab_size=30,
                  alpha=None, beta=None, noise=-1, plsi=False):
    """Generates documents according to plsi or lda
    
    Attributes:
        num_topics: 
            the number of underlying latent topics
        num_docs: 
            the number of documents to generate
        words_per_doc: 
            parameter to a Poisson distribution;
            determines the average words in a documents
        vocab_size: 
            the number of words in the vocabulary
        alpha: 
            parameters to dirichlet distribution for topics
        beta: 
            parameters to dirichlet distribution for words
        noise: 
            given as a probability; each word will be replaced with a random
            word with noise probability
        plsi:
            flag to determine which distribution to draw from,
            a random distribution or a sample from a dirichlet distribution
            
    Returns:
        docs:
            the list of documents, each a list of words (represented by their
            indices in range(vocab_size)
        word_dist:
            the distribution over words for each topic; 
            each row is the distribution for a different topic 
        topics_dist:
            the distribution over topics for each document;
            each row is the distribution for a different document
    """
    p = Poisson(words_per_doc)
    if alpha == None:
        alpha = [1]*num_topics
    if beta == None:
        beta = [1]*vocab_size
    if len(alpha) != num_topics or len(beta) != vocab_size:
        print "ERROR: dirichlet parameters unequal:"
        print "alpha supplied:", len(alpha), "(needed", num_topics, ")"
        print "beta supplied:", len(beta), "(needed", vocab_size, ")" 
        return
    if plsi:
        word_dist = [normalize(array([rand() for w in range(vocab_size)],
                                     'double')) for t in range(num_topics)]
    else:
        word_dist = [dirichlet(beta) for i in range(num_topics)]
    docs = []
    topic_dists = []
    for i in range(num_docs):
        words_per_doc = p.sample()
        doc = []
        if plsi:
            topic_dist = normalize(array([rand() for t in range(num_topics)],
                                         'double'))
        else:
            topic_dist = dirichlet(alpha)
        topic_dists.append(topic_dist)
        for word in range(words_per_doc):
            if rand() < noise:
                doc.append(rsample(range(vocab_size), 1))
            else:
                topic = sample(topic_dist)
                doc.append(sample(word_dist[topic]))
        docs.append(doc)
    return docs, word_dist, topic_dists

def normalize(dist):
    return dist / sum(dist)

def dirichlet_pdf(x, alpha):
    density = reduce(operator.mul, 
                   [x[i]**(alpha[i]-1.0) for i in range(len(alpha))])
    norm_top = gamma(np.sum(alpha))
    norm_bot = reduce(operator.mul, [gamma(a) for a in alpha])
    return (norm_top / norm_bot) * density

def write(data):
    docs, words, topics = data
    with open('lda-out', 'w') as f:
        for doc in docs:
            for word in doc:
                f.write(str(word) + " ")
            f.write('\n')
    with open('lda_model-out', 'w') as f:
        for topic in words:
            for word in topic:
                f.write(str(word) + " ")
            f.write('\n')
        f.write('V\n')
        for doc in topics:
            for topic in doc:
                f.write(str(topic) + " ")
            f.write('\n')

def main():
    args = sys.argv[1:]
    if len(args) == 2:
        num_topics, num_docs = [int(arg) for arg in args]
    else:
        num_topics, num_docs = 4, 20
        print "using default parameters for num_docs and num_topics"
    
    print "generating", num_docs, "documents with", num_topics, "topics"
    print ""
    data = generate_docs(num_topics, num_docs)
    if '-w' in args:
        print "writing data to file...",
        write(data)
        print "done"
    return data

if __name__ == '__main__':
    docs, words, topics = main()
