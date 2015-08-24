#!/bin/bash
kill $(ps aux | grep 'cliente.py' | awk '{print $2}')
kill $(ps aux | grep 'servidor.py' | awk '{print $2}')

