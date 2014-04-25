all: nqueen

CXX = g++
CFLAGS = -std=c++11 -Wall -Ofast -march=native -pipe

nqueen.o: main.cc
	$(CXX) $(CFLAGS) -c -o $@ $<

nqueen: nqueen.o
	$(CXX) $(CFLAGS) -o $@ $<

clean:
	rm -rf nqueen.o
