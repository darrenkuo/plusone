import lda
from lda import Poisson
from lda import sample
from lda import normalize

import numpy as np
from numpy import array
from numpy.random.mtrand import dirichlet

import random
from random import random as rand
from random import randint as rint
from random import sample as rsample

def generate_ratings(num_types, num_users, ratings_per_user=20, num_items=100,
                     alpha=None, noise=0, plsi=False):
    p = Poisson(ratings_per_user)
    ratings = [[rint(1,5) for i in range(num_items)] for i in range(num_types)]
    if alpha == None:
        alpha = [1]*num_types
    user_ratings = []
    user_indices = []
    type_dists = []
    for i in range(num_users):
        ratings_per_user = p.sample()
        if plsi:
            type_dist = normalize(array([rand() for t in range(num_types)], 
                                        'double'))
        else:
            type_dist = dirichlet(alpha)
        type_dists.append(type_dist)
        rating = []
        indices = []
        items = rsample(range(num_items), ratings_per_user)
        items_left = range(num_items)
        for item in items:
            items_left.remove(item)
        for j in items:
            type = sample(type_dist)
            rating.append(ratings[type][j])
            indices.append(j)
        additions = int((noise / 100.0) * ratings_per_user)
        for k in rsample(items_left, additions):
            rating.append(rint(1,5)) 
        user_ratings.append(rating)
        user_indices.append(indices)
    user_ratings = user_indices, user_ratings
    
    return user_ratings, ratings, type_dists

def write(data):
    user_ratings, ratings, types = data
    user_indices, user_ratings = user_ratings
    with open('ratings-out', 'w') as f:
        for i in range(len(user_indices)):
            for index in user_indices[i]:
                f.write(str(index) + " ")
            f.write('\n')
            for rating in user_ratings[i]:
                f.write(str(rating) + " ")
            f.write('\n')
    with open('ratings_model-out', 'w') as f:
        for user in types:
            for type in user:
                f.write(str(type) + " ")
            f.write('\n')
        f.write("V\n")
        for user_type in ratings:
            for item in user_type:
                f.write(str(item) + " ")
            f.write('\n')

def main():
    data = generate_ratings(10, 200)
    print "writing data to file...",
    write(data)
    print "done."
    return data

if __name__ == '__main__':
    user_ratings, ratings, types = main()
    