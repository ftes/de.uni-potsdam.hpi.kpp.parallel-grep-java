JC=javac
JP=jar
SOURCES=de/uni_potsdam/hpi/kpp/parallel_grep_java/Grep.java de/uni_potsdam/hpi/kpp/parallel_grep_java/SearchThread.java
CLASSES=$(SOURCES:.java=.class)
EXECUTABLE=pargrepmon.jar
MAIN=de.uni_potsdam.hpi.kpp.parallel_grep_java.Grep

.SUFFIXES: .java .class

all: $(SOURCES) $(EXECUTABLE)

.java.class:
	$(JC) -g $<
	
$(EXECUTABLE): $(CLASSES) 
	$(JP) cfe $@ $(MAIN) $(CLASSES)
