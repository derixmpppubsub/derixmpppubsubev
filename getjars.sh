#!/bin/sh
cd lib/
wget http://openjena.org/repo/com/hp/hpl/jena/arq/2.8.7/arq-2.8.7.jar
wget http://openjena.org/repo/com/hp/hpl/jena/jena/2.6.4/jena-2.6.4.jar
#wget http://www.apache.org/dyn/closer.cgi/logging/log4j/1.2.16/apache-log4j-1.2.16.tar.gz
#wget http://www.apache.org/dyn/closer.cgi/logging/log4j/1.2.16/apache-log4j-1.2.16.tar.gz.md5
#md5sum -c apache-log4j-1.2.16.tar.gz.md5 
wget http://archive.apache.org/dist/logging/log4j/1.2.16/apache-log4j-1.2.16.tar.gz
tar xvzf apache-log4j-1.2.16.tar.gz
cp apache-log4j-1.2.16/log4j-1.2.16.jar .
rm -rf apache-log4j-1.2.16*
wget http://www.igniterealtime.org/downloadServlet?filename=smack/smack_src_3_2_1.tar.gz -O smack_src_3_2_1.tar.gz
tar xvzf smack_src_3_2_1.tar.gz 
cp smack_src_3_2_1/smack.jar .
cp smack_src_3_2_1/smackx.jar .
cp smack_src_3_2_1/smackx-debug.jar .
rm -rf smack_src_3_2_1*
