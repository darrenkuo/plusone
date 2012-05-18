import numpy as np
from matplotlib.pyplot import *
import pylab

def plot_types(types):
    offset = 0
    for type in types:
        bar(offset, type, 0.01)
        offset += 0.01
    xticks(np.arange(.005, .1, .01), range(10))

#hist(words, range(vocab_size + 1)
