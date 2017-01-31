#!/bin/bash

if [ ${#} -eq 0 ]
then
   echo "Usage: ${0} <package> [<dir>]"
   exit 0
fi
package=${1}
if [ ${#} -le 2 ]
then
   dir=${2}
fi

adb exec-out run-as ${package} ls ${dir}
