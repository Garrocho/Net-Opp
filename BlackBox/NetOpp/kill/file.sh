#!/bin/bash
kill $(ps aux | grep 'StateSTA.py' | awk '{print $2}')
kill $(ps aux | grep 'StateAP.py' | awk '{print $2}')

