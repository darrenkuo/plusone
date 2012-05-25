import re
import os
import argparse

col_width = 20

def pad(width, s):
    return s+ " " * (width - len(s))

def read_pred(filename):
    for line in open(filename, "r"):
        return line[16:-1]

def main():
    parser = argparse.ArgumentParser(description="Rakes a directory to find \
    relevant output and prints it out.")
    parser.add_argument('p', action="store", metavar="dir_path",
                        help="directory to rake")
    parser.add_argument('-k', action="store", metavar="predictions", type=int,
                        default=3, help="which prediction type to query")
    args = parser.parse_args()
    
    print "walking through path: ", args.p
    print "looking for predictions =", args.k
    print ""
    
    top = args.p
    table = {}
    test_percentages = set()
    for (dirpath, dirnames, filenames) in os.walk(top):
        print dirnames, filenames
        m = re.match(".*/([^/]*)/([^/]*)$", dirpath)
        if None == m: 
            print "no regex match, moving on!\n"
            continue
        test_percentage = m.group(1)
        print "test_percentage:", test_percentage, "\n"
        test_percentages.add(test_percentage)
        #table[test_percentage] = {}
        predictions = m.group(2)
        if args.k != int(predictions):
            print "found", predictions, "(looking for", str(args.k) + ")" 
            #continue
        for filename in filenames:
            if ".out" != filename[-4:]: 
                print "found", filename, "(looking for .out files)"
                continue
            if filename not in table: table[filename] = {}
            table[filename][test_percentage] = read_pred(dirpath + "/" + filename)
    heldout_list = list(test_percentages)
    heldout_list.sort()
    print " " * col_width + "".join(pad(col_width, str(h)) for h in test_percentages)
    for filename in table:
        line = pad(col_width, filename)
        for test_percentage in test_percentages:
            line += pad(col_width, table[filename][test_percentage])
        print line

if "__main__" == __name__:
    main()
