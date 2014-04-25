all: GraphColoring.class

JAVA = java
JAVAC = javac
DOT = dot
LIBS = open-localsearch-20140421-2.jar

GraphColoring.class: GraphColoring.java
	$(JAVAC) -cp .:$(LIBS) $< 

clean:
	rm -rf GraphColoring.class
