#!/bin/bash
kill $(ps aux | grep 'oppnet.py' | awk '{print $2}')
kill $(ps aux | grep 'oppnet.py' | awk '{print $2}')