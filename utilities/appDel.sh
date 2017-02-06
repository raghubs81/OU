#!/bin/bash

if [ ${#} -eq 0 -o ${#} -ge 3 ]
then
   echo "Usage (A): ${0} <package> <relative path to source file>"
   echo "Usage (B): ${0} <relative path to source file>"
   exit 0
fi

if [ ${#} -eq 1 ]
then
  package=""
  sourceFilePath=${1}
elif [ ${#} -eq 2 ]
then
  package=${1}
  sourceFilePath=${2}
fi

if [ "${package}" == "" ]
then
   adb exec-out rm ${sourceFilePath}
else
   adb exec-out run-as ${package} rm ${sourceFilePath}
fi
