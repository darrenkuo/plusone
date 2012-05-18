import lda
from lda import Poisson
from lda import sample
from lda import normalize

import numpy as np
from numpy import array

import random
from random import random as rand
from random import randint as rint
from random import sample as rsample

def generate_ratings(num_types, num_users, ratings_per_user=20, num_items=100,
                     noise=0):
    p = Poisson(ratings_per_user)
    ratings = [[rint(1,5) for i in range(num_items)] for i in range(num_types)]
    
    user_ratings = []
    user_indices = []
    type_dists = []
    for i in range(num_users):
        ratings_per_user = p.sample()
        type_dist = normalize(array([rand() for t in range(num_types)], 
                                    'double'))
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
        print "ratings", ratings_per_user
        print "additions", additions
        for k in rsample(items_left, additions):
            rating.append(rint(1,5)) 
        user_ratings.append(rating)
        user_indices.append(indices)
    user_ratings = user_indices, user_ratings
    
    return user_ratings, ratings, type_dists

def write(user_ratings):
    user_indices, user_ratings = user_ratings
    with open('ratings-out', 'w') as f:
        for i in range(len(user_indices)):
            for index in user_indices[i]:
                f.write(str(index) + " ")
            f.write('\n')
            for rating in user_ratings[i]:
                f.write(str(rating) + " ")
            f.write('\n')

def main():
    data = generate_ratings(4, 20)
    write(data[0])
    return data

if __name__ == '__main__':
    user_ratings, ratings, types = main()
    