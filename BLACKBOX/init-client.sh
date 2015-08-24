#!/bin/bash

teste=`ps axu | grep cliente.py | wc -l`;
if [ "$teste" != "1" ]; then
   echo "tem"
else
  echo "nao tem"
   /usr/bin/python /root/Net-Opp/cliente.py &
fi
