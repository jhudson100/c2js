#!/usr/bin/env python3

import sys

ctr=1

with open("src/parser/myterminals.tokens","w") as ofp:
    with open("terminals.txt") as fp:
        for line in fp:
            line=line.strip()
            idx = line.find(':')
            if idx != -1:
                lhs = line[:idx].strip()
                ofp.write( "{}={}\n".format(lhs,ctr) )
                ctr+=1
                
