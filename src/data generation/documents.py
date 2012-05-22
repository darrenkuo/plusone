import argparse

import random
from random import random as rand
from random import sample as rsample

import numpy as np
from numpy.random.mtrand import dirichlet

import util
from util import *

def generate_docs(num_topics, num_docs, words_per_doc=50, vocab_size=30,
                  alpha=None, beta=None, noise=-1, plsi=False):
    """Generates documents according to plsi or lda
    
    Args:
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
        word_dist = [normalize([rand() for w in range(vocab_size)])
                     for t in range(num_topics)]
    else:
        word_dist = [dirichlet(beta) for i in range(num_topics)]
    docs = []
    topic_dists = []
    for i in range(num_docs):
        words_per_doc = p.sample()
        doc = []
        if plsi:
            topic_dist = normalize([rand() for t in range(num_topics)])
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

def write(data):
    docs, words, topics = data
    with open('output/documents-out', 'w') as f:
        for doc in docs:
            for word in doc:
                f.write(str(word) + " ")
            f.write('\n')
    with open('output/documents_model-out', 'w') as f:
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
    parser = argparse.ArgumentParser(description="Document generator. Default\
    parameters are noted in parentheses.")
    parser.add_argument('-w', action="store_true", default=False,
                        help="write flag (false)")
    parser.add_argument('-k', action="store", metavar='num_topics', type=int,
                        default=4, help="number of latent topics (4)")
    parser.add_argument('-n', action="store", metavar='num_docs', type=int,
                        default=20, help="number of documents to generate (20)")
    parser.add_argument('-l', action="store", type=int, default=50, 
                        help="average number of words per document (50)")
    parser.add_argument('-m', action="store", type=int, default=30,
                        help="size of the vocabulary (30)")
    parser.add_argument('-s', action="store", metavar='noise', type=float, 
                        default=-1, help="probability each word is generated\
                        randomly (0)")
    parser.add_argument('-plsi', action="store_true", default=False,
                        help="flag to use plsi instead of lda (false)")
    
    args = parser.parse_args()
    
    print ""
    print "generating documents with parameters:"
    print "k    = ", args.k, "(number of topics)"
    print "n    = ", args.n, "(number of documents)"
    print "l    = ", args.l, "(average number of words)"
    print "m    = ", args.m, "(size of vocabulary)"
    print "s    = ", args.s, "(noise probability)"
    print "plsi = ", args.plsi, "(whether to draw from plsi or lda model)"
    print ""
    
    if args.s == 0:
        noise = -1
    else:
        noise = args.s
    
    data = generate_docs(args.k, args.n, args.l, args.m, 
                         noise=noise, plsi=args.plsi)
    if args.w:
        print "writing data to file...",
        write(data)
        print "done"
    return data

if __name__ == '__main__':
    docs, words, topics = main()
