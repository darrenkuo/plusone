"""Contains some useful classes and methods
"""
import numpy as np
import pylab
import operator

import math
from math import e
from math import gamma

import random
from random import random as rand

import matplotlib
from matplotlib.pyplot import *

class Poisson(object):
    """A class to represent a poisson distribution.
    
    Attributes:
        lamb:
            the parameter to the poisson distribution
    Methods:
        sample():
            returns a sample from the distribution
    """
    def __init__(self, L=15):
        self.lamb = L
        
    def sample(self):
        """Samples the poisson distribution.
        
        Args:
            none
            
        Returns:
            a number sampled from the poisson distribution with parameter
            self.lamb
        """
        L = e ** (-self.lamb)
        k, p = 1, rand()
        while p > L:
            k += 1
            p *= rand()
        return k - 1
    
def sample(dist):
    """Takes a distribution and samples from it. 
    
    Given a list of probabilities (that obey a distribution), samples from it 
    and returns an index of the list. This method assumes that dist obeys
    a multinomial distribution.
    
    Args:
        dist:
            A multinomial distribution represented by a list, 
            where each entry is the index's probability.
    
    Returns:
        a sample from the distribution, a random index in the list
    
    Sample usage:
        sample([.5, .5])    sample a distribution with two elements, 
                            each equally likely
        returns: 0
    """
    p = rand()
    res = 0
    for i in range(len(dist)):
        res += dist[i]
        if res > p:
            return i
    #for debugging purposes only *SHOULD NOT REACH THIS LINE*
    print res, p
    
def normalize(dist):
    """Normalizes an array so it obeys a multinomial distribution.
    
    Assumes dist is a numpy array. Divides each element in dist by the total
    so that all entries add up to 1.
    
    Args:
        dist: an array of numbers
        
    Returns:
        a normalized version of dist
    """
    return np.array(dist, 'double') / np.sum(dist)

def dirichlet_pdf(x, alpha):
    """Calculates the probability of the given sample from a dirichlet 
    distribution.
    
    Given a sample x and parameter alpha, calculates the probability that
    x was sampled from Dirichlet(alpha).
    
    Args:
        x:
            a list of numbers in the interval [0,1] that sum to 1
        alpha:
            the parameter to a dirichlet distribution; represented as a list
    
    Returns:
        the probability that x was sampled from Dirichlet(alpha)
    """
    density = reduce(operator.mul, 
                   [x[i]**(alpha[i]-1.0) for i in range(len(alpha))])
    norm_top = gamma(np.sum(alpha))
    norm_bot = reduce(operator.mul, [gamma(a) for a in alpha])
    return (norm_top / norm_bot) * density

def count(words):
    """Creates a histogram of occurrences in an array.
    
    Given a list, counts how many times each instance occurs.
    
    Args:
        words:
            a list of values
    Returns:
        a dictionary with keys as the values that appear in words and values
        as the number of times they occur 
    """
    word_count = {}
    num_words = 0
    unique_words = 0
    for word in words:
        num_words += 1
        if word_count.has_key(word):
            word_count[word] += 1
        else:
            word_count[word] = 1
            unique_words += 1
    word_count["total"] = num_words
    word_count["unique"] = unique_words
    return word_count

def plot_dist(types, color='b', labels=None):
    """Plots a distribution as a bar graph.
    
    Given a distribution, plots a bar graph. Each bar is an element in the
    distribution, and its value is the element's probability.
    
    Args:
        types:
            a distribution, represented as a list
    Returns:
        none, but plots the distribution
    """
    offset = 0
    width = 0.01
    if labels == None:
        labels = range(len(types))
    for type in types:
        bar(offset, type, width, color=color)
        offset += width
    xticks(np.arange(width / 2, width * len(types), .01), labels)

def plot_hist(words, color='b'):
    word_count = count(words)
    hist(words, range(word_count['unique'] + 1), color=color)