import os
import json
import time
import socket
import logging
from threading import Thread

IPS = []

def trata_cliente(conexao, endereco):
    requisicao = conexao.recv(1024)
    print requisicao
    if requisicao == 'LIST':
        arqs = os.listdir('/etc/black/arquivos/')
        conexao.send(json.dumps(arqs))
    elif requisicao == 'GET':
        arqs = os.listdir('/etc/black/arquivos/')
        arquivo = conexao.recv(1024)
        if arquivo in arqs:
            fp = open('/etc/black/arquivos/{0}'.format(arquivo), 'r')
            strng = fp.read(1024)
            while strng:
                conexao.send(strng)
                strng = fp.read(1024)
            global logger
            logger.error('[ENVIADO - {0}]'.format(arquivo))
    elif requisicao == 'PUT':
        conexao.send('OK')
        arqs = os.listdir('/etc/black/arquivos/')
        arquivo = conexao.recv(1024)
        print arquivo
        print arqs
        if arquivo not in arqs:
            conexao.send('TRUE')
            arq = open('/etc/black/arquivos/{0}'.format(arquivo), 'w')
            while 1:
                dados = conexao.recv(1024)
                if not dados:
                    break
                arq.write(dados)
            arq.close()
            global logger
            logger.error('[RECEBIDO - {0}]'.format(arquivo))
        else:
            conexao.send('FALSE')
    conexao.close()


def loop_servidor():
    
    soquete = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    soquete.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    soquete.bind(('10.10.0.1', 5555))
    soquete.listen(10)
    global IPS
    # Fica aqui aguardando novas conexoes.
    while True:

        # Para cada nova conexao e criado um novo processo para tratar as requisicoes.
        conexao = soquete.accept()
        novaConexao = []
        novaConexao.append(conexao[0])
        novaConexao.append(conexao[1])
        if conexao[1] not in IPS:
            IPS.append(conexao[1])
        Thread(target=trata_cliente, args=(novaConexao)).start()


if __name__ == '__main__':
    logger = logging.getLogger('myapp')
    hdlr = logging.FileHandler('/root/Net-Opp/servidor.log')
    formatter = logging.Formatter('%(asctime)s %(levelname)s %(message)s')
    hdlr.setFormatter(formatter)
    logger.addHandler(hdlr)
    logger.setLevel(logging.WARNING)
    logger.error('Servidor de Arquivos Iniciou na Porta 5555...')
    Thread(target=loop_servidor).start()
