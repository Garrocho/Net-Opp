#!/bin/bash
kill $(ps aux | grep 'GerenciadorRede.py' | awk '{print $2}')
kill $(ps aux | grep 'GerenciadorRede.py' | awk '{print $2}')