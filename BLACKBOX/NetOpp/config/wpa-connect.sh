#!/bin/bash
ifconfig wlan0 down
ifconfig wlan0 up
wpa_supplicant -d -i wlan0 -D nl80211 -c /BLACKBOX/NetOpp/config/wpa.conf >> /dev/null
