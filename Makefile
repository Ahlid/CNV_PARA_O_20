JVM = java 
JAVAC = javac
JFLAGS = 
SHELL = /bin/bash
SRC_DIR = src/
PACKAGE = pt/ulisboa/tecnico/meic/cnv/
HTTPSERVER = $(PACKAGE)httpserver/
MAZERUNNER = $(PACKAGE)mazerunner/maze/
INST = $(PACKAGE)instrumentation/
STORAGE = $(PACKAGE)storage/
LOADBALANCER = $(PACKAGE)loadbalancer/

LOG4J = lib/log4j-1.2.17.jar
AWS_VERSION=1.11.308
AWS=/media/sf_CNV_PARA_O_20/aws-java-sdk-$(AWS_VERSION)/
BIT = BIT/
RUN_DIR = bin/
TOOL_OUTPUT = $(RUN_DIR)$(MAZERUNNER)

export _JAVA_OPTIONS=-XX:-UseSplitVerifier

## Refresh environment 
refresh: | clean all

## Make all : (warning: changing this order WILL break things)
all: | create_bin maze bit loadbalancer storage httpserver instrumentation bit_samples

############### Sub sections #####################
## Create bin directory
create_bin: 
	[[ -d $(RUN_DIR) ]] || mkdir $(RUN_DIR)
## Compile mazerunner 
maze: 
	$(JAVAC) $(JFLAGS) $(SRC_DIR)$(MAZERUNNER)*.java $(SRC_DIR)$(MAZERUNNER)exceptions/*.java $(SRC_DIR)$(MAZERUNNER)render/*.java $(SRC_DIR)$(MAZERUNNER)strategies/datastructure/*.java $(SRC_DIR)$(MAZERUNNER)strategies/*.java -d $(RUN_DIR)
## Compile lowBIT and highBIT
bit:
	$(JAVAC) $(JFLAGS) -sourcepath $(SRC_DIR) $(SRC_DIR)$(BIT)lowBIT/*.java -d $(RUN_DIR)
	$(JAVAC) $(JFLAGS) -sourcepath $(SRC_DIR) $(SRC_DIR)$(BIT)highBIT/*.java -d $(RUN_DIR)
## Compile loadbalancer
loadbalancer: 
	$(JAVAC) $(JFLAGS) -cp $(RUN_DIR):$(LOG4J):$(AWS)/lib/aws-java-sdk-$(AWS_VERSION).jar:$(AWS)/third-party/lib/* $(SRC_DIR)$(LOADBALANCER)*.java -d $(RUN_DIR)
## Compile storage
storage:
	$(JAVAC) $(JFLAGS) -cp $(RUN_DIR):$(LOG4J):$(AWS)/lib/aws-java-sdk-$(AWS_VERSION).jar:$(AWS)/third-party/lib/* $(SRC_DIR)$(STORAGE)*.java -d $(RUN_DIR)
## Compile hhtpserver
httpserver:
	$(JAVAC) $(JFLAGS) -cp $(RUN_DIR) $(SRC_DIR)$(HTTPSERVER)*.java -d $(RUN_DIR)
## Compile instumentation
instrumentation:
	$(JAVAC) $(JFLAGS) -cp $(RUN_DIR):$(LOG4J) $(SRC_DIR)$(INST)*.java -d $(RUN_DIR)
## Compile bit_samples
## TO REMOVE!
bit_samples:
	$(JAVAC) $(JFLAGS) -cp $(RUN_DIR) $(SRC_DIR)samples/*.java -d $(RUN_DIR)



run_inst:
	$(JVM) $(JFLAGS) -cp $(RUN_DIR):$(LOG4J):$(AWS)/lib/aws-java-sdk-$(AWS_VERSION).jar:$(AWS)/third-party/lib/* $(INST)InstrumentationTool $(inputClass)

run_httpserver:
	$(JVM) $(JFLAGS) -cp $(RUN_DIR):$(LOG4J):$(AWS)/lib/aws-java-sdk-$(AWS_VERSION).jar:$(AWS)/third-party/lib/* $(HTTPSERVER)WebServer

inst_test:
	#$(JVM) $(JFLAGS) -cp $(RUN_DIR):$(LOG4J) StatisticsTool $(inputClass)
	mkdir -p $(TOOL_OUTPUT) $(TOOL_OUTPUT)exceptions/ $(TOOL_OUTPUT)render/ $(TOOL_OUTPUT)strategies/datastructure/ $(TOOL_OUTPUT)strategies/
	$(JVM) $(JFLAGS) -cp $(RUN_DIR) StatisticsTool -dynamic $(RUN_DIR)$(MAZERUNNER) $(TOOL_OUTPUT)
	#$(JVM) $(JFLAGS) -cp $(RUN_DIR) StatisticsTool -dynamic $(RUN_DIR)$(MAZERUNNER)exceptions/ $(TOOL_OUTPUT)exceptions/
	#$(JVM) $(JFLAGS) -cp $(RUN_DIR) StatisticsTool -dynamic $(RUN_DIR)$(MAZERUNNER)render/ $(TOOL_OUTPUT)render/
	#$(JVM) $(JFLAGS) -cp $(RUN_DIR) StatisticsTool -dynamic $(RUN_DIR)$(MAZERUNNER)strategies/datastructure/ $(TOOL_OUTPUT)strategies/datastructure/
	#$(JVM) $(JFLAGS) -cp $(RUN_DIR) StatisticsTool -dynamic $(RUN_DIR)$(MAZERUNNER)strategies/ $(TOOL_OUTPUT)strategies/
	
clean: 
	$(RM) -d $(RUN_DIR)$(MAZERUNNER)*.class -d $(RUN_DIR)$(MAZERUNNER)exceptions/*.class $(RUN_DIR)$(MAZERUNNER)render/*.class $(RUN_DIR)$(MAZERUNNER)strategies/*.class $(RUN_DIR)$(MAZERUNNER)strategies/datastructure/*.class
	$(RM) $(RUN_DIR)$(BIT)highBIT/*.class
	$(RM) $(RUN_DIR)$(BIT)lowBIT/*.class
	$(RM) $(RUN_DIR)$(STORAGE)*.class
	$(RM) $(RUN_DIR)$(HTTPSERVER)*.class
	$(RM) $(RUN_DIR)$(INST)*.class
