#!/bin/bash
ifconfig wlan0 down
ifconfig wlan0 up
wpa_supplicant -d -i wlan0 -D nl80211 -c /root/Net-Opp/wpa.conf >> /dev/null
