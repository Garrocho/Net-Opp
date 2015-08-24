#!/bin/bash
kill $(ps aux | grep 'dhclient wlan0' | awk '{print $2}')

