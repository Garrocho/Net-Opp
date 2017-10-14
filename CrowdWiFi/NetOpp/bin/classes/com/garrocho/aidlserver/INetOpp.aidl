package com.garrocho.aidlserver;
	interface INetOpp
	{
	  boolean startNetwork();
      boolean stopNetwork();
      boolean addFile(String file);
      boolean delFile(String file);
      boolean addTradeOff(int tradeoff);
	  List<String> clientList();
      List<String> fileList();
	}