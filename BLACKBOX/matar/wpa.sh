#!/bin/bash
kill $(ps aux | grep 'wpa-connect.sh' | awk '{print $2}')
kill $(ps aux | grep 'wpa_supplicant' | awk '{print $2}')

