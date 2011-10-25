==================================
Evaluation tests installation
==================================

The tests are setup in 3 coputers:

* computer1, the XMPP hub: Ubuntu natty with Openfire
* computer2, the publishers : Debian sid with publishers + 4store
* computer3, the subscribers : Debian sid with Subscribers +4store

SW used for the tests:

* openfire 3.7.0
* 4store 1.1.3-1
* openjdk-6-jdk



Setup the XMPP hub computer
=============================

#. First, you need mysql installed:

  $ sudo aptitude install mysql-server

#. Create the database that Openfire will use:

  $ mysql -u root -p
  
  $ mysql> create database openfire;
  
  $ mysql> grant all privileges on openfire.* to 'openfire'@'localhost' identified by 'openfirepw';
  
  $ mysql> flush privileges;
  
  $ mysql> quit

#. Download openfire_3.7.0_all.deb through the web or by command line:

  $ wget http://www.igniterealtime.org/downloadServlet?filename=openfire/openfire_3.7.0_all.deb
  
  $ sudo dpkg -i openfire_3.7.0_all.deb

#. You can now finish the Openfire installation through the web at localhost:9090. In the DB setup step, choose MySQL, write "jdbc:mysql://localhost:3306/openfire" as Connection URL, and enter the username and password you used when creating the db.

#. Configure openfire to have memory enough

  $ sudo vim /etc/init.d/openfire
  
  Modify the line DAMEON_OPTS like:
  
  DAEMON_OPTS="$DAEMON_OPTS  -Xss256k -Xms768m -Xmx1024m -Djava.net.preferIPv4Stack=true  -server -DopenfireHome=${DAEMON_DIR} \
 -Dopenfire.lib.dir=${DAEMON_LIB} -classpath ${DAEMON_LIB}/startup.jar\
 -jar ${DAEMON_LIB}/startup.jar"


Setup subscribers computer
============================

#. Install 4store:

  $ sudo aptitude install 4store

After that, you should have also installed:

  libraptor1 1.4.21-6
  libraptor2-0  2.0.4-1
  librasqal3 0.9.27-1

#. Install java and ant:

  $ sudo aptitude install ant openjdk-6-jdk openjdk-6-jre

#. Get the source code:

  $  git clone https://github.com/derixmpppubsub/derixmpppubsub
  $  git clone https://github.com/derixmpppubsub/derixmpppubsubev

#. Compile the code:

  $ cd derixmpppubsub
  
  $ sh ./getjars.sh
  
  $ ant jar
  
  $ cd ../derixmpppubsubev
  
  $ sh ./getjars.sh
  
  $ ant compile

Alternative, you can just execute the setup.sh file in https://github.com/derixmpppubsub/derixmpppubsubev


Setup publishers computer
============================

The steps to initialize the publishers computer are the same as for the subscribers


Initialize/run the tests
===========================

Depending on the tests to run, you will run different initializations

Initialize the XMPP hub registered publishers and subscribers
--------------------------------------------------------------

You can do it in three different ways:

#. Download and compile the code as explained above and run :

  $ java -Xss256k -Xms768m -Xmx768m -Dfile.encoding=UTF-8 -classpath build/classes:lib/log4j-1.2.16.jar:lib/jena-2.6.4.jar:lib/arq-2.8.7.jar:lib/smack.jar:lib/smackx.jar:lib/smackx-debug.jar:../derixmpppubsub/build/classes  org.deri.xmpppubsubev.InitializePubSubs numberPublishers numberSubscribers numberTest

#. import one of the tests db availabe in data/, for instance:

  $ mysql -u openfire -p openfire < data/openfire100pub10sub.sql

With this method you have to previously shutdown the server:
  $ /etc/init.d/openfire stop 

And after importing the db, start it:
  $ /etc/init.d/openfire start

#. Some of the options for the PublishersTest include the XMPP server initialization, see below

If you need to trace Openfire or give it more memory, run it in this way:
  $ sudo su -s /bin/bash - openfire
  $ strace -f -e \!futex,gettimeofday -o openfire.strace /usr/lib/jvm/java-6-sun/bin/java  -Xss256k -Xms768m -Xmx1024m -server -DopenfireHome=/usr/share/openfire -Dopenfire.lib.dir=/usr/share/openfire/lib -classpath /usr/share/openfire/lib/startup.jar -jar /usr/share/openfire/lib/startup.jar

Initialize subscribers
--------------------------

#. Create the store:

  $ sudo 4s-backend-setup subscribers
  
#. Run the store:

  $ sudo 4s-backend subscribers
  # sudo 4s-httpd -s -1 -p 8000 subscribers

Run Subscribers
----------------

  $ java -Xss256k -Xms768m -Xmx768m -Dfile.encoding=UTF-8 -classpath build/classes:lib/log4j-1.2.16.jar:lib/jena-2.6.4.jar:lib/arq-2.8.7.jar:lib/smack.jar:lib/smackx.jar:lib/smackx-debug.jar org.deri.xmpppubsubev.SubscribersTest numberSubscribers numberTest xmppServerIP

Initialize publishers
----------------------

#. Create the store:

  $ sudo 4s-backend-setup publishers
  
#. Run the store:

  $ sudo 4s-backend publishers
  # sudo 4s-httpd -s -1 -p 8001 publishers

#. Import data into the publishers store

You can manually import the data, for instance: 

  $ sudo 4s-import -v -f ntriples publishers data/100pub10000postsinitialtriples.nt

Or it will be imported automatically depending on the test

Run Publishers
---------------

  $ java -Xss256k -Xms768m -Xmx1024m -Dfile.encoding=UTF-8 -classpath build/classes:lib/log4j-1.2.16.jar:lib/jena-2.6.4.jar:lib/arq-2.8.7.jar:lib/smack.jar:lib/smackx.jar:lib/smackx-debug.jar:../derixmpppubsub/build/classes org.deri.xmpppubsubev.PublishersTest xmppServerIP numberTest

Enjoy!
