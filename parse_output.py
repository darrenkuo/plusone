import json
import argparse
import src.datageneration.util as util

TEST = 'tests'

def parse(filename):
    with open(filename, 'r') as f:
        data = json.load(f)
    names = []
    scores = []
    for test in data[TEST]:
        for key in test.keys():
            names.append(str(key))
            scores.append(test[key]['Predicted_Mean'])
    util.plot_dist(scores, labels=names)
    util.savefig(filename + '.pdf')
    #util.show()
    return data

def main():
    parser = argparse.ArgumentParser(description="reads a json file and plots\
    its constituent data")
    parser.add_argument('f', metavar='filename', action="store", 
                        help="json file to be read")
    
    args = parser.parse_args()
    print "reading file:", args.f
    return parse(args.f)

if __name__ == '__main__':
    data = main()