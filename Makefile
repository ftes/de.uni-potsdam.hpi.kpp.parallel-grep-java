JC=javac
JP=jar
SOURCES=de/uni_potsdam/hpi/kpp/parallel_grep_java/Grep.java de/uni_potsdam/hpi/kpp/parallel_grep_java/SearchThread.java
OBJECTS=$(SOURCES:.java=.class)
EXECUTABLE=pargrepmon.jar
MAIN=de.uni_potsdam.hpi.kpp.parallel_grep_java.Grep

all: $(SOURCES) $(EXECUTABLE)
	
$(EXECUTABLE): $(OBJECTS) 
	$(JP) cfe $@ $(MAIN) $(OBJECTS)

.java.class:
	$(JC) $<
