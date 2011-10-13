FIND        := /usr/bin/find

JAVA_HOME   :=  /usr/lib/jvm/java-6-openjdk-i386
JAVA        := $(JAVA_HOME)/bin/java
JAVAC       := $(JAVA_HOME)/bin/javac
ANT         := /usr/bin/ant

SRCDIR      := src
LIBDIR      := lib
BUILDDIR    := build

# http://www.makelinux.net/make3/make3-CHP-9-SECT-2
space := $(empty) $(empty)
LIBS = $(subst ${space},:,$(strip $(wildcard ${LIBDIR}/*.jar)))
# linux
SRCS = $(shell find ${SRCDIR} -name \*.java)

JVMFLAGS    := -Dfile.encoding=UTF-8 -Xmx2048m
JAVACFLAGS  := -d ${BUILDDIR}
CLASSPATH   := ${LIBS}
MAINCLASS   := org.deri.xmpppubsub.PublishersTest

all: build

prep:
	[ -d ${BUILDDIR} ] || mkdir ${BUILDDIR}

build: prep
	# version ant
	${ANT} compile
	
	# version without ant
	#CLASSPATH=${CLASSPATH} ${JAVAC} ${JAVACFLAGS} ${SRCS}

jar: build
	${ANT} jar
	# 

clean:
	rm -rf ${BUILDDIR}

run: build
	CLASSPATH=${CLASSPATH}:${BUILDDIR} ${JAVA} ${JVMFLAGS} ${MAINCLASS}

.PHONY: all prep build jar clean run
