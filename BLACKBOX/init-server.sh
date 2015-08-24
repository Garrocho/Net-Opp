#!/bin/bash

teste=`ps axu | grep servidor.py | wc -l`;
if [ "$teste" != "1" ]; then
   echo "tem"
else
  echo "nao tem"
   /usr/bin/python /root/Net-Opp/servidor.py &
fi
