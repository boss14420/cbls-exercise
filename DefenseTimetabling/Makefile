

CLASSPATH = -cp .:open-localsearch-lastest.jar
JFLAGS = -g $(CLASSPATH)
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
        AllDifferentVarIntLSInt.java \
        DefenseTimetablingOpenLocalSearch.java \
        FConsecutive.java \
        FMatch.java \
        IFuncFactory.java \
        Jury.java \
        SubjectMatch.java \
        TabuSearch2.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class

run: DefenseTimetablingOpenLocalSearch.class
	$(JAVA) $(CLASSPATH) DefenseTimetablingOpenLocalSearch
