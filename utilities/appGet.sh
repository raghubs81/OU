#!/bin/bash

if [ ${#} -eq 0 -o ${#} -eq 1 -o ${#} -ge 4 ]
then
   echo "Usage (A): ${0} <package> <relative path to source file> <target file> "
   echo "Usage (B): ${0} <relative path to source file> <target file>"
   exit 0
fi

if [ ${#} -eq 2 ]
then
  package=""
  sourceFilePath=${1}
  destFileName=${2}
elif [ ${#} -eq 3 ]
then
  package=${1}
  sourceFilePath=${2}
  destFileName=${3}
fi

if [ "${package}" == "" ]
then
   adb exec-out cat ${sourceFilePath} > ${destFileName}
else
   adb exec-out run-as ${package} cat ${sourceFilePath} > ${destFileName}
fi
flip -bc ${destFileName} > temp.txt
mv temp.txt ${destFileName}

