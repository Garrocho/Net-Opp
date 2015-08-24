#!/usr/bin/env python
import CORBA, NetOpp
import os

ior = open(os.getcwd() + '/IOR.txt').read()
orb = CORBA.ORB_init()
servant = orb.string_to_object(ior)
servant._narrow(NetOpp.NetOppServer)
print servant.clientList()
print servant.fileList()
