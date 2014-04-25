#!/bin/bash - 
#===============================================================================
#
#          FILE: test.sh
# 
#         USAGE: ./test.sh 
# 
#   DESCRIPTION: 
# 
#       OPTIONS: ---
#  REQUIREMENTS: ---
#          BUGS: ---
#         NOTES: ---
#        AUTHOR: BOSS14420 (), 
#  ORGANIZATION: 
#       CREATED: 04/25/2014 13:54
#      REVISION:  ---
#===============================================================================

set -o nounset                              # Treat unset variables as an error

testdata='DSJC125.1 DSJC125.9 DSJC1000.1 fpsol2.i.1 fpsol2.i.3 le450_15a le450_15d le450_25a le450_5d mulsol.i.1 mulsol.i.4 zeroin.i.1 anna huck jean games120 miles1000 miles1500 queen5_5 myciel3 4-Insertions_3'
testnum=10

for data in $testdata
do
    echo "Data set: "$data
    time ./graph-coloring.sh graph_color/${data}.col $testnum > /dev/null
    echo ""
done
