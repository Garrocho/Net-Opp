from threading import Thread
import numpy as np
import cv2
import time
from configobj import ConfigObj
import CORBA, NetOpp
import os

config = ConfigObj('/BLACKBOX/NetOpp/config/config.properties')
vd = config.get('videosDirectory')
vt = config.get('videosTime')
pl = config.get('pathLog')
vt = 20

def addFinal(pathLog, nameVideo):
    ior = open(os.getcwd() + '/BLACKBOX/NetOpp/config/IOR.txt').read()
    orb = CORBA.ORB_init()
    servant = orb.string_to_object(ior)
    servant._narrow(NetOpp.NetOppServer)
    servant.addFile("{0}/{1}".format(pathLog, nameVideo))

cap = cv2.VideoCapture(0)

altura = 480
largura = 640

cap.set(cv2.cv.CV_CAP_PROP_FRAME_WIDTH, largura);
cap.set(cv2.cv.CV_CAP_PROP_FRAME_HEIGHT, altura);

out = cv2.VideoWriter('output.avi', cv2.cv.CV_FOURCC('D','I','V','3'), 30, (largura, altura),True)
aux=0
fps = 1
counter = 0
sec = 0

while counter < 100:
	ret, frame = cap.read()
	counter = counter + 1

counter = 0

while fps < 3 or fps > 30:
        start = time.time()
        counter = 0
        while counter < 200:
            ret, frame = cap.read()
            #frame = cv2.flip(frame,0)
            out.write(frame)
            counter=counter+1

        end=time.time()
        sec = end - start
        fps = counter / sec

num_frames = 0
print vt
print fps

while True:
    videoName = time.strftime("{0}%Y-%m-%d-%H-%M-%S.avi".format(vd), time.gmtime())
    out2 = cv2.VideoWriter(videoName, cv2.cv.CV_FOURCC('D','I','V','3'), int(fps), (largura,altura),True)
    num_frames = 0
    start = time.time()
    while num_frames < vt*int(fps):
        num_frames=num_frames+1
        ret, frame = cap.read()
        #frame = cv2.flip(frame,0)
        out2.write(frame)
    end = time.time()
    sec = end - start
    fps = int(num_frames/int(sec))
    thread = Thread(target = addFinal, args = (pl, videoName, ))
    thread.start()
cap.release()
