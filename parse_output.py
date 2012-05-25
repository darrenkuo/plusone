import json
import argparse
#import src.datageneration.util as util

def parse(filename):
    with open(filename, 'r') as f:
        data = json.load(f)
    #do something with f

def main():
    parser = argparse.ArgumentParser(description="reads a json file and plots\
    its constituent data")
    parser.add_argument('f', metavar='filename', action="store", 
                        help="json file to be read")
    
    args = parser.parse_args()
    print "reading file:", args.f
    #parse(args.f)

if __name__ == '__main__':
    main()