import re
import os
import sys

col_width = 20

def pad(width, s):
    return s+ " " * (width - len(s))

def read_pred(filename):
    for line in open(filename, "r"):
        return line[16:-1]

if "__main__" == __name__:
    top = sys.argv[1]
    table = {}
    heldouts = set()
    for (dirpath, dirnames, filenames) in os.walk(top):
        m = re.match(".*/([^/]*)/([^/]*)$", dirpath)
        if None == m: continue
        heldout = m.group(1)
        heldouts.add(heldout)
        k = m.group(2)
        if "3" != k: continue
        for filename in filenames:
            if ".out" != filename[-4:]: continue
            if filename not in table: table[filename] = {}
            table[filename][heldout] = read_pred(dirpath + "/" + filename)
    heldout_list = list(heldouts)
    heldout_list.sort()
    print " " * col_width + "".join(pad(col_width, str(h)) for h in heldouts)
    for filename in table:
        line = pad(col_width, filename)
        for heldout in heldouts:
            line += pad(col_width, table[filename][heldout])
        print line
