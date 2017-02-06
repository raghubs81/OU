#!/bin/bash

if [ ${#} -eq 0 -o ${#} -ge 3 ]
then
   echo "Usage (A): ${0} <package> [<dir>] "
   echo "Usage (B): ${0} <dir>"
   exit 0
fi

if [ ${#} -eq 1 ]
then
  package=""
  dir=${1}
elif [ ${#} -eq 2 ]
then
   package=${1}
   dir=${2}
fi

if [ "${package}" == "" ]
then
   adb exec-out ls ${dir}
else
   adb exec-out run-as ${package} ls ${dir}
fi
