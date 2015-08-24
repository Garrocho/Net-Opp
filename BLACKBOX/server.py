#!/usr/bin/env python

import sys, os
import CORBA, NetOpp, NetOpp__POA
from configobj import ConfigObj

config = ConfigObj('/etc/blackbox/carro/config.properties')
vd = config.get('videosDirectory')
pl = config.get('pathLog')

class NetOppServer(NetOpp__POA.NetOppServer):

	def startNetwork(self):
		try:
			os.system('/root/Net-Opp/init.sh')
			return True
		except:
			return False

	def stopNetwork(self):
		try:
			os.system('/root/Net-Opp/kill-init.sh')
			return True
		except:
			return False

	def addFile(self, file):
		try:
			lines = open(pl, "r").readlines()
			lines.append("{0}\n".format(file))
			videos = open("{0}".format(pl), "w")
			videos.writelines(lines)
			videos.close()
			return True
		except:
			return False

	def delFile(self, file):
		try:
			lines = open(pl, "r").readlines()
			lines.remove("{0}".format(file))
			videos = open("{0}".format(pl), "w")
			videos.writelines(lines)
			videos.close()
			return True
		except:
			return False

	def clientList(self):
		try:
			lines = open('/root/Net-Opp/clients.txt', "r").readlines()
			return lines
		except:
			return None

	def fileList(self):
		try:
			lines = open(pl, "r").readlines()
			return lines
		except:
			return None

orb = CORBA.ORB_init(sys.argv)
poa = orb.resolve_initial_references("RootPOA")

servant = NetOppServer()
poa.activate_object(servant)

a = open(os.getcwd() + '/IOR.txt', 'w')
a.write(orb.object_to_string(servant._this()))
a.close()

poa._get_the_POAManager().activate()
orb.run()