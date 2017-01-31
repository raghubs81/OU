#!/bin/bash

if [ ${#} -eq 0 ]
then
   echo "Usage: ${0} <package> [<relative path to source file> <target file>]"
   exit 0
fi
package=${1}
if [ ${#} -le 3 ]
then
   sourceFilePath=${2}
   destFileName=${3}
fi

adb exec-out run-as ${package} cat ${sourceFilePath} > ${destFileName}
