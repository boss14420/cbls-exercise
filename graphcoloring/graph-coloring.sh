#!/bin/bash - 
#===============================================================================
#
#          FILE: graph-coloring.sh
# 
#         USAGE: ./graph-coloring.sh 
# 
#   DESCRIPTION: 
# 
#       OPTIONS: ---
#  REQUIREMENTS: ---
#          BUGS: ---
#         NOTES: ---
#        AUTHOR: BOSS14420 (), 
#  ORGANIZATION: 
#       CREATED: 04/24/2014 22:32
#      REVISION:  ---
#===============================================================================

set -o nounset                              # Treat unset variables as an error

java -cp .:open-localsearch-20140421-2.jar GraphColoring "$@"
