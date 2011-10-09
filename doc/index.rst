=================
Installation
=================

* computer1: Openfire
* computer2: Publishers + 4store
* computer3: Subscribers +4store

SW
* openfire 3.7.0
* 4store 1.1.3-1
libraptor1 1.4.21-6
libraptor2-0  2.0.4-1
librasqal3 0.9.27-1


Setup hub
=================

Install openfire
----------------

Download openfire_3.7.0_all.deb
sudo dpkg -i openfire_3.7.0_all.deb

localhost:9090

Setup subscribers
==================

#. Install 4store

sudo aptitude install 4store

#. Create store

sudo 4s-backend-setup subscribers

#. Run store

sudo 4s-backend subscribers
sudo 4s-httpd -p 8000 subscribers

#. Compile Subscribers

#. Create accounts
java -Xmx2048m -Dfile.encoding=UTF-8 -classpath bin:lib/log4j-1.2.16.jar:lib/jena-2.6.4.jar:lib/arq-2.8.7.jar:lib/smack.jar:lib/smackx.jar:lib/smackx-debug.jar:lib/slf4j-log4j12-1.5.8.jar:lib/icu4j-3.4.4.jar:lib/iri-0.8.jar:lib/junit-4.5.jar:lib/log4j-1.2.13.jar:lib/lucene-core-2.3.1.jar:lib/slf4j-api-1.5.8.jar:lib/stax-api-1.0.1.jar:lib/wstx-asl-3.2.9.jar:lib/xercesImpl-2.7.1.jar:lib/any23-core-0.6.1.jar:lib/derixmpppubsub.jar org.deri.xmpppubsubev.CreateAccounts


#. Run Subscribers
java -Xmx2048m -Dfile.encoding=UTF-8 -classpath bin:lib/log4j-1.2.16.jar:lib/jena-2.6.4.jar:lib/arq-2.8.7.jar:lib/smack.jar:lib/smackx.jar:lib/smackx-debug.jar:lib/slf4j-log4j12-1.5.8.jar:lib/icu4j-3.4.4.jar:lib/iri-0.8.jar:lib/junit-4.5.jar:lib/log4j-1.2.13.jar:lib/lucene-core-2.3.1.jar:lib/slf4j-api-1.5.8.jar:lib/stax-api-1.0.1.jar:lib/wstx-asl-3.2.9.jar:lib/xercesImpl-2.7.1.jar:lib/any23-core-0.6.1.jar:lib/derixmpppubsub.jar org.deri.xmpppubsubev.SubscribersTest 1 1 10

Setup publishers
==================

#. Install 4store

sudo aptitude install 4store

#. Create store

sudo 4s-backend-setup subscribers

#. Run Publishers store
sudo 4s-backend publishertest 
sudo 4s-httpd -p 8001 publishertest

#. Compile Publishers


#. Run Publishers
java -Xmx2048m -Dfile.encoding=UTF-8 -classpath bin:lib/log4j-1.2.16.jar:lib/jena-2.6.4.jar:lib/arq-2.8.7.jar:lib/smack.jar:lib/smackx.jar:lib/smackx-debug.jar:lib/slf4j-log4j12-1.5.8.jar:lib/icu4j-3.4.4.jar:lib/iri-0.8.jar:lib/junit-4.5.jar:lib/log4j-1.2.13.jar:lib/lucene-core-2.3.1.jar:lib/slf4j-api-1.5.8.jar:lib/stax-api-1.0.1.jar:lib/wstx-asl-3.2.9.jar:lib/xercesImpl-2.7.1.jar:lib/any23-core-0.6.1.jar:lib/derixmpppubsub.jar org.deri.xmpppubsubev.PublishersTest 1 1 10

