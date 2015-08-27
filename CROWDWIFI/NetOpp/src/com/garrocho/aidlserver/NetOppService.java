package com.garrocho.aidlserver;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;

import com.garrocho.net.GerenciadorRede;
import com.garrocho.net.ProvedorConteudos;

public class NetOppService extends Service {

	public GerenciadorRede gerenciadorRede;
	public ProvedorConteudos provedorConteudos;
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	private final INetOpp.Stub mBinder = new INetOpp.Stub() {

		@SuppressLint("NewApi")
		@Override
		public boolean startNetwork() throws RemoteException {
			try {
				provedorConteudos = new ProvedorConteudos(5555, MainActivity.getAppContext());
				provedorConteudos.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				
				gerenciadorRede = new GerenciadorRede(MainActivity.getAppContext());
				gerenciadorRede.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				return true;
			} catch (Exception e) {
				return false; 
			}
		}

		@Override
		public boolean stopNetwork() throws RemoteException {
			try {
				provedorConteudos.cancel(true);
				gerenciadorRede.cancel(true);
				return true;
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public boolean addFile(String file) throws RemoteException {
			try {
				gerenciadorRede.fileList.add(file);				
				return true;
			} catch (Exception e) {
				return false; 
			}
		}

		@Override
		public boolean delFile(String file) throws RemoteException {
			try {
				return gerenciadorRede.fileList.remove(file);				
			} catch (Exception e) {
				return false; 
			}
		}

		@Override
		public List<String> clientList() throws RemoteException {
			try {
				return gerenciadorRede.ipList;
			} catch (Exception e) {
				return null; 
			}
		}

		@Override
		public List<String> fileList() throws RemoteException {
			try {
				return gerenciadorRede.fileList;				
			} catch (Exception e) {
				return null; 
			}
		}
	};
}