import json
import argparse
import src.datageneration.util as util

TEST = 'tests'

def parse(filename, show):
    with open(filename, 'r') as f:
        data = json.load(f)
    names = []
    scores = []
    for test in data[TEST]:
        for key in test.keys():
            names.append(str(key))
            scores.append(test[key]['Predicted_Mean'])
    util.plot_dist(scores, labels=names)
    if show:
        util.show()
    else:
        util.savefig(filename + '.pdf')
    
    return data

def main():
    parser = argparse.ArgumentParser(description="reads a json file and plots\
    its constituent data")
    parser.add_argument('f', metavar='filename', action="store", 
                        help="json file to be read")
    parser.add_argument('-q', action="store_true", default=False, 
                        help="flag to suppress writing to file")
    
    args = parser.parse_args()
    print "reading file:", args.f
    if args.q:
        print "not writing to file and instead displaying plot with console"
    return parse(args.f, args.q)

if __name__ == '__main__':
    data = main()