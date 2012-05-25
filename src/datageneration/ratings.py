import argparse

import util
from util import *

import numpy as np
from numpy.random.mtrand import dirichlet

import random
from random import random as rand
from random import randint as rint
from random import sample as rsample

def generate_ratings(num_types, num_users, ratings_per_user=20, num_items=100,
                     alpha=None, noise=-1, plsi=False):
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
            type_dist = normalize([rand() for t in range(num_types)])
        else:
            type_dist = dirichlet(alpha)
        type_dists.append(type_dist)
        rating = []
        indices = []
        for j in rsample(range(num_items), ratings_per_user):
            if rand() < noise:
                rating.append(rint(1,5))
            else:
                type = sample(type_dist)
                rating.append(ratings[type][j])
            indices.append(j)
        user_ratings.append(rating)
        user_indices.append(indices)
    user_ratings = user_indices, user_ratings
    
    return user_ratings, ratings, type_dists

def write(data):
    user_ratings, ratings, types = data
    user_indices, user_ratings = user_ratings
    with open('output/ratings-out', 'w') as f:
        for i in range(len(user_indices)):
            for index in user_indices[i]:
                f.write(str(index) + " ")
            f.write('\n')
            for rating in user_ratings[i]:
                f.write(str(rating) + " ")
            f.write('\n')
    with open('output/ratings_model-out', 'w') as f:
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
    parser = argparse.ArgumentParser(description="Ratings generator. Default\
    parameters are noted in parentheses.")
    parser.add_argument('-w', action="store_true", default=False,
                        help="write flag (false)")
    parser.add_argument('-k', action="store", metavar='num_types', type=int,
                        default=10, help="number of latent user types (10)")
    parser.add_argument('-n', action="store", metavar='num_users', type=int,
                        default=100, help="number of users to generate (100)")
    parser.add_argument('-l', action="store", type=int, default=20, 
                        help="average number of ratings per user (20)")
    parser.add_argument('-m', action="store", type=int, default=100,
                        help="number of items (100)")
    parser.add_argument('-s', action="store", metavar='noise', type=float, 
                        default=0, help="probability each rating is generated\
                        randomly (0)")
    parser.add_argument('-plsi', action="store_true", default=False,
                        help="flag to use plsi instead of lda (false)")
    
    args = parser.parse_args()
    
    print ""
    print "generating documents with parameters:"
    print "k    = ", args.k, "(number of user types)"
    print "n    = ", args.n, "(number of users)"
    print "l    = ", args.l, "(average number of ratings)"
    print "m    = ", args.m, "(number of items)"
    print "s    = ", args.s, "(noise probability)"
    print "plsi = ", args.plsi, "(whether to draw from plsi or lda model)"
    print ""
    
    if args.s == 0:
        noise = -1
    else:
        noise = args.s
    
    data = generate_ratings(args.k, args.n, args.l, args.m, 
                         noise=noise, plsi=args.plsi)
    if args.w:
        print "writing data to file...",
        write(data)
        print "done"
    return data

if __name__ == '__main__':
    user_ratings, ratings, types = main()
    