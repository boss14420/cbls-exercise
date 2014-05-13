all: GraphColoring.class

JAVA = java
JAVAC = javac
DOT = dot
LIBS = -cp .:open-localsearch-lastest.jar

GraphColoring.class: GraphColoring.java
	$(JAVAC) $(LIBS) $< 

clean:
	rm -rf GraphColoring.class
