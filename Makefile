JAVAC = javac 
JFLAGS =
SRC_DIR = src/
HTTPSERVER = pt/ulisboa/tecnico/meic/cnv/httpserver/
MAZERUNNER = pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/

LOG4J = lib/log4j-1.2.17.jar
BIT =
RUN_DIR = bin/

all: 
	$(JAVAC) $(JFLAGS) $(SRC_DIR)$(MAZERUNNER)*.java $(SRC_DIR)$(MAZERUNNER)exceptions/*.java $(SRC_DIR)$(MAZERUNNER)render/*.java $(SRC_DIR)$(MAZERUNNER)strategies/datastructure/*.java $(SRC_DIR)$(MAZERUNNER)strategies/*.java -d $(RUN_DIR)
	$(JAVAC) $(JFLAGS) -cp $(RUN_DIR) $(SRC_DIR)$(HTTPSERVER)*.java -d $(RUN_DIR)
    

clean: 
	$(RM) -d $(RUN_DIR)$(HTTPSERVER)*.class
	$(RM) -d $(RUN_DIR)$(MAZERUNNER)*.class -d $(RUN_DIR)$(MAZERUNNER)exceptions/*.class $(RUN_DIR)$(MAZERUNNER)render/*.class $(RUN_DIR)$(MAZERUNNER)strategies/*.class $(SRC_DIR)$(MAZERUNNER)strategies/datastructure/*.class