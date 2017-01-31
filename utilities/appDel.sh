#!/bin/bash

if [ ${#} -eq 0 ]
then
   echo "Usage: ${0} <package> [<relative path to source file>]"
   exit 0
fi

package=${1}
if [ ${#} -le 2 ]
then
   sourceFilePath=${2}
fi

adb exec-out run-as ${package} rm ${sourceFilePath}
