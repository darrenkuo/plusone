import lda
from lda import Poisson
from lda import sample

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
        for j in rsample(range(num_items), ratings_per_user):
            type = sample(type_dist)
            rating.append(ratings[type][j])
            indices.append(j)
        erasures = int((noise / 100.0) * ratings_per_user)
        for k in rsample(range(len(rating)), erasures):
            rating[k] = 0 
        user_ratings.append(rating)
        user_indices.append(indices)
    user_ratings = user_indices, user_ratings
    
    return user_ratings, ratings, type_dists

def normalize(dist):
    return dist / sum(dist)

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
    