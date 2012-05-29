import json
import argparse
import src.datageneration.util as util

def parse(filename):
    with open(filename, 'r') as f:
        data = json.load(f)
    predicted_means = {}
    #this feels hacky--'tests's should not be hard-coded? we'll see
    for test in data['tests']:
        for key in test.keys():
            predicted_means[str(key)] = test[key]['Predicted_Mean']
    util.plot_dist(predicted_means.values(), labels=predicted_means.keys())
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