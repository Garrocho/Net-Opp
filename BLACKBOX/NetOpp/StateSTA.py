import os
import json
import time
import socket
import logging
from threading import Thread

def conectar():
    soquete = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    soquete.connect(('10.10.0.1', 5555))
    return soquete

def put_file(arq):
    print arq
    soquete = conectar()
    soquete.send('PUT')
    print soquete.recv(1024)
    soquete.send(arq)
    env = soquete.recv(1024)
    print env
    if env == 'TRUE':
        fp = open('/BLACKBOX/NetOpp/files/{0}'.format(arq), 'r')
        strng = fp.read(1024)
        while strng:
            soquete.send(strng)
            strng = fp.read(1024)
    soquete.close()
    global logger
    logger.error('[ENVIADO - {0}]'.format(arq))

def get_file(arq):
    print arq
    soquete = conectar()
    soquete.send('GET')
    soquete.send(arq)
    arquivo = open('/BLACKBOX/NetOpp/files/{0}'.format(arq), 'w')
    while 1:
        dados = soquete.recv(1024)
        if not dados:
            break
        arquivo.write(dados)
    arquivo.close()
    soquete.close()
    global logger
    logger.error('[RECEBIDO - {0}]'.format(arq))

logger = logging.getLogger('myapp')
hdlr = logging.FileHandler('/BLACKBOX/Net-Opp/log/cliente.log')
formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
hdlr.setFormatter(formatter)
logger.addHandler(hdlr) 
logger.setLevel(logging.WARNING)
logger.error('Cliente de Arquivos Iniciou...')
nada = 15
while True:
    soquete = conectar()
    soquete.send('LIST')
    arqs = json.loads(soquete.recv(1014))
    soquete.close()
    ass = os.listdir('/BLACKBOX/Net-Opp/files/')
    for i in arqs:
        if i not in ass:
            Thread(target=get_file, args=(i, )).start()
        else:
            nada = nada - 1

    for i in ass:
        if i not in arqs:
            Thread(target=put_file, args=(i, )).start()
        else:
            nada = nada - 1
    if nada <= 5:
        fil = open('/BLACKBOX/Net-Opp/config/nada.txt', 'w')
        fil.write('1')
        fil.close()
    time.sleep(5)