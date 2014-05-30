#!/bin/bash - 
#===============================================================================
#
#          FILE: run.sh
# 
#         USAGE: ./run.sh 
# 
#   DESCRIPTION: run DefenseTimetablingOpenLocalSearch program
# 
#       OPTIONS: num of objective (0 - 3)
#  REQUIREMENTS: ---
#          BUGS: ---
#         NOTES: ---
#        AUTHOR: BOSS14420 (), 
#  ORGANIZATION: 
#       CREATED: 05/30/2014 20:21
#      REVISION:  ---
#===============================================================================

set -o nounset                              # Treat unset variables as an error

MAINCLASS=DefenseTimetablingOpenLocalSearch
JAVA=java
CLASSPATH=".:open-localsearch-lastest.jar"

$JAVA -cp $CLASSPATH $MAINCLASS "$@"
