#!/bin/sh
sudo aptitude install 4store
sudo aptitude install ant openjdk-6-jdk openjdk-6-jre

git clone https://github.com/derixmpppubsub/derixmpppubsub
cd derixmpppubsub
./getjars.sh
ant jar


git clone https://github.com/derixmpppubsub/derixmpppubsubev
cd ../derixmpppubsubev
./getjars.sh
ant compile
