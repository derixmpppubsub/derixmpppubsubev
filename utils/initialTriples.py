#!/usr/bin/python
# vim: set expandtab tabstop=4 shiftwidth=4:
# -*- coding: utf-8 -*-
import sys
import getopt

postTemplate = "<http://ecp-alpha/semantic/post/%s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/sioc/ns#Post> . \n <http://ecp-alpha/semantic/post/%s> <http://purl.org/dc/elements/1.1/creator> <http://ecp-alpha/semantic/employee/%s> . \n"

def main(argv):
    npubs = 100
    ntriples = 10000
    
    short_opts = "p:t:"
    long_opts = ["npubs=", "ntriples="]
    try:                                
        opts, args = getopt.getopt(argv, short_opts, long_opts)
    except getopt.GetoptError:
        print "bad arguments"
        sys.exit(0)
    for opt, arg in opts:
        if opt in ("-p", "--npubs"):     
            npubs = arg
        elif opt in ("-t","--ntriples"):
            ntriples = arg
    print ntriples
    print npubs
    triples = ""
    for i in range(1,int(npubs)+1):
        for j in range(1,int(ntriples)+1):
            # post = "pub" + str(i) + "post" + str(j)
            post = "post" + str(j)
            employee = "pub" + str(i)
            triples += postTemplate  %  (post, post, employee)

    f = open("../data/"+str(npubs)+"pub"+str(ntriples)+"posts.nt", "w")
    f.write(triples)
    f.close()
    
if __name__ == "__main__":
    main(sys.argv[1:])

