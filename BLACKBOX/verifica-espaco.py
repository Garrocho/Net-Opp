import os
import psutil
from configobj import ConfigObj

config = ConfigObj('/etc/blackbox/carro/config.properties')
vd = config.get('videosDirectory')
pathLog = config.get('pathLog')
du = int(config.get('diskUsage'))

perAtual = int(psutil.disk_usage('/')[3])

if perAtual > du:
    lines = open(pathLog, "r").readlines()
    cont = -1
    while perAtual > du:
        cont = cont + 1
        try:
            os.remove(lines[cont][:-1])
        except:
            pass
        perAtual = int(psutil.disk_usage('/')[3])
    videos = open(pathLog, "w")
    videos.writelines(lines[cont+1:])
    videos.close()