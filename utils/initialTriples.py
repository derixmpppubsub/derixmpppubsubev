postTemplate = "<http://ecp-alpha/semantic/post/%s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfs.org/sioc/ns#Post> . \n <http://ecp-alpha/semantic/post/%s> <http://purl.org/dc/elements/1.1/creator> <http://ecp-alpha/semantic/employee/%s> . \n"

triples = ""
numPubs = 100
numTriples = 10000
for i in range(1,numPubs+1):
    for j in range(1,numTriples+1):
        # post = "pub" + str(i) + "post" + str(j)
        post = "post" + str(j)
        employee = "pub" + str(i)
        triples += postTemplate  %  (post, post, employee)

f = open("../data/"+str(numPubs)+"pub"+str(numTriples)+"posts"+"initialtriples.nt", "w")
f.write(triples)
f.close()


