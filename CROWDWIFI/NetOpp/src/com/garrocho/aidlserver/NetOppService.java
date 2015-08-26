package com.garrocho.aidlserver;

import java.io.File;
import java.util.List;
import java.util.concurrent.Semaphore;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.util.Log;

import com.garrocho.net.StateAP;
import com.garrocho.net.GerenciadorRede;
import com.garrocho.net.ProvedorConteudos;
import com.garrocho.net.StateSTA;

public class NetOppService extends Service {

	public GerenciadorRede gerenciadorRede;
	public ProvedorConteudos provedorConteudos;
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	private final INetOpp.Stub mBinder = new INetOpp.Stub() {

		@Override
		public boolean startNetwork() throws RemoteException {
			try {
				gerenciadorRede = new GerenciadorRede(MainActivity.getAppContext());
				gerenciadorRede.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			} catch (Exception e) {
				return false; 
			}
		}

		@Override
		public boolean stopNetwork() throws RemoteException {
			return false;
		}

		@Override
		public boolean addFile(String file) throws RemoteException {
			return false;
		}

		@Override
		public boolean delFile(String file) throws RemoteException {
			return false;
		}

		@Override
		public List<String> clientList() throws RemoteException {
			return null;
		}

		@Override
		public List<String> fileList() throws RemoteException {
			return null;
		}
	};
}