#!/bin/bash

teste=`ps axu | grep StateAP.py | wc -l`;
if [ "$teste" != "1" ]; then
   echo "tem"
else
  echo "nao tem"
   /usr/bin/python /BLACKBOX/NetOpp/StateAP.py &
fi
