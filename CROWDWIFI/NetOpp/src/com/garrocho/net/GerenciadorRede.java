package com.garrocho.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;

import com.garrocho.aidlserver.INetOpp.Stub;
import com.garrocho.aidlserver.MainActivity;
import com.garrocho.net.hotspot.ClientScanResult;
import com.garrocho.net.hotspot.WifiApManager;

public class GerenciadorRede extends AsyncTask<Void, String, Void> {

	public StateAP ap;
	public StateSTA sta;
	private int netID = -1;
	public Context contexto;
	private int cont_wifi = 0;
	private Handler myHandler;
	private int cont_server = 0;
	public String pasta = Environment.getExternalStorageDirectory() + "/NetOpp";
	public boolean iClient, iServer;
	public boolean fuiServer = false;
	private WifiManager wifiManager = null;
	static WifiApManager wifiApManager = null;
	public List<String> ipList = new ArrayList<String>();
	public List<String> fileList = new ArrayList<String>();
	public List<String> downList = new ArrayList<String>();
	private Map<String, Integer> IPinativo = new HashMap<String, Integer>();

	public GerenciadorRede(Context contexto){
		this.contexto = contexto;
		int bateria = (int)getBateria();
		this.cont_server = (int)(55 - (int)(bateria / 2))/2;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		myHandler = new Handler();
	}

	@SuppressLint("NewApi")
	@Override
	protected Void doInBackground(Void... arg0) {
		while (true) {
			if (iServer) {
				trataServidor();
			}
			else {
				trataCliente();
			}
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static int randInt(int min, int max) {
		Random foo = new Random();
		int randomNumber = foo.nextInt(max - min) + min;
		if(randomNumber == min) {
			return min + 1;
		}
		else {
			return randomNumber;
		}

	}

	public synchronized void trataServidor() {
		if (wifiApManager == null) {
			wifiApManager = new WifiApManager(MainActivity.getAppContext());
			wifiApManager.setWifiApEnabled(null, true);
			iServer = true;
			cont_server = randInt(25, 30);
			myHandler.post(new Runnable() {
				public void run() {
					startServer();
				}
			});
		}
		else if (!wifiApManager.isWifiApEnabled()) {
			wifiApManager.setWifiApEnabled(null, true);
			iServer = true;
			cont_server = randInt(25, 30);
			myHandler.post(new Runnable() {
				public void run() {
					startServer();
				}
			});
		}
		else {
			BufferedReader br = null;
			final ArrayList<ClientScanResult> result = new ArrayList<ClientScanResult>();

			try {
				br = new BufferedReader(new FileReader("/proc/net/arp"));
				String line;
				while ((line = br.readLine()) != null) {
					String[] splitted = line.split(" +");

					if ((splitted != null) && (splitted.length >= 4)) {
						String mac = splitted[3];
						if (mac.matches("..:..:..:..:..:..")) {
							boolean isReachable = InetAddress.getByName(splitted[0]).isReachable(300);

							if (isReachable) {
								result.add(new ClientScanResult(splitted[0], splitted[3], splitted[5], isReachable));
							}
						}
					}
				}
			} catch (Exception e) {
				Log.e(this.getClass().toString(), e.toString());
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					Log.e(this.getClass().toString(), e.getMessage());
				}
			}

			List<String> auxIps = new ArrayList<String>();

			for (ClientScanResult clientScanResult : result) {
				auxIps.add(clientScanResult.getIpAddr());
			}

			if (auxIps.size() == 0) {
				cont_server--;
			}
			else {
				/*try {
					contexto.sem.acquire();
					final Iterator<String> ips = contexto.listaIpOk.keySet().iterator();
					while (ips.hasNext()) {
						final String ip = ips.next();
						String mac = contexto.listaIpOk.get(ip);
						if (!auxIps.contains(ip)) {
							if (contexto.listaPastas.containsKey(mac)) {

								if (!IPinativo.containsKey(mac)) {
									IPinativo.put(mac, 0);
								}
								else if (IPinativo.get(mac) < 3) {
									int va = IPinativo.remove(mac);
									va++;
									IPinativo.put(mac, va);
								}
								else if (IPinativo.get(mac) > 0 && IPinativo.get(mac) < 3) {
									IPinativo.remove(mac);
									IPinativo.put(mac, 0);
								}
								else {
									IPinativo.remove(mac);
									Log.d("NETHOT", "NETHOT - REMOVE");
									//contexto.listaPastas.remove(mac);
									ips.remove();
								}
							}
						}
					}
					contexto.sem.release();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}*/

			}

			if (cont_server < 0) {
				cont_server = randInt(2, 5);
				iServer = false;
			}
			else {
				myHandler.post(new Runnable() {
					public void run() {
						startServer();
					}
				});
			}

			final List<String> auxIps2 = auxIps;

			myHandler.post(new Runnable() {
				public void run() {
					ipList = auxIps2;
					ipList.add("0.0.0.0");
				}
			});

		}
	}

	public boolean tudoOK() {
		try {
			if (wifiApManager != null) {
				if (wifiApManager.isWifiApEnabled()) {
					wifiApManager.setWifiApEnabled(null, false);
				}
				try {
					Thread.sleep(1500);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (wifiManager == null) {
				wifiManager = (WifiManager)contexto.getSystemService(Context.WIFI_SERVICE);

				if (!wifiManager.isWifiEnabled()) {
					wifiManager.setWifiEnabled(true);
					if (wifiManager.isWifiEnabled())
						return false;
				}
			}
			if (!wifiManager.isWifiEnabled()) {
				wifiManager.setWifiEnabled(true);
				if (wifiManager.isWifiEnabled())
					return false;
			}

			if (!ipOK()) {
				return false;
			}
			if (wifiManager.getConnectionInfo().getSSID() == null) {
				return false;
			}
		} catch (Exception e) {
			wifiManager = (WifiManager)contexto.getSystemService(Context.WIFI_SERVICE);

			if (!wifiManager.isWifiEnabled()) {
				wifiManager.setWifiEnabled(true);
				if (wifiManager.isWifiEnabled())
					return false;
			}
		}
		return true;
	}

	public void trataCliente() {

		boolean scan = false, conectado = false;

		if (!tudoOK()) {

			WifiConfiguration wifi;
			wifiManager.startScan();

			try {
				Thread.sleep(2000);
			} catch (Exception e) {
				Log.d("NETHOT", "NETHOT - " + e.getMessage());
			}

			List<ScanResult> apList;
			WifiConfiguration tmpConfig = null;

			apList = wifiManager.getScanResults();

			while (apList == null) {
				apList = wifiManager.getScanResults();
			}

			for (ScanResult result : apList) {
				if (result.SSID.contains("Crowd")) {
					tmpConfig = new WifiConfiguration();
					tmpConfig.BSSID = result.BSSID;
					tmpConfig.SSID = "\"" + result.SSID + "\"";
					tmpConfig.priority = 1;
					tmpConfig.preSharedKey = "\"" + "123456789" + "\"";
					tmpConfig.status = WifiConfiguration.Status.ENABLED;
					tmpConfig.hiddenSSID = true;
					tmpConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
					tmpConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
					tmpConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
				}
			}
			wifi = tmpConfig;

			if (wifi != null) {
				scan = true;
			}
			else {
				scan = false;
			}

			if (scan) {

				netID = wifiManager.addNetwork(wifi);

				if (wifiManager.enableNetwork(netID, true)) {
					conectado = true;
				}

			}

			if ((scan == false || conectado == false) ||  tudoOK() == false) {

				if (cont_wifi == 4) {
					wifiManager = (WifiManager)contexto.getSystemService(Context.WIFI_SERVICE);

					if (!wifiManager.isWifiEnabled()) {
						wifiManager.setWifiEnabled(true);
					}
					else {
						wifiManager.removeNetwork(netID);

						if (wifiApManager == null)
							wifiApManager = new WifiApManager(contexto);
						wifiApManager.setWifiApEnabled(null, true);
						try {
							Thread.sleep(200);
						} catch (Exception e) {
							Log.d("NETHOT", "NETHOT - " + e.getMessage());
						}
						wifiApManager.setWifiApEnabled(null, false);
						try {
							Thread.sleep(200);
						} catch (Exception e) {
							Log.d("NETHOT", "NETHOT - " + e.getMessage());
						}
						wifiManager.setWifiEnabled(true);
					}
					cont_wifi = 0;
				}
				else
					cont_wifi++;

				if (cont_server > 0)
					cont_server--;
				else {
					wifiManager.removeNetwork(netID);
					iServer = true;
				}
			}
			else {
				iServer = false;
				myHandler.post(new Runnable() {
					public void run() {
						startCliente();
					}
				});
			}
		}
		else {
			myHandler.post(new Runnable() {
				public void run() {
					startCliente();
				}
			});
		}
	}

	public boolean ipOK() {
		int ips = wifiManager.getConnectionInfo().getIpAddress();
		@SuppressWarnings("deprecation")
		String ipAddress = Formatter.formatIpAddress(ips);
		String ip = ipAddress.substring(0, ipAddress.lastIndexOf(".")+1) + "1";
		if (!ip.equalsIgnoreCase("192.168.43.1")) {
			return false;
		}
		return true;
	}
	
	public void verificarPasta() {
		File folder = new File(pasta);
		if (!folder.exists()) {
		    folder.mkdir();
		}
	}
	
	public float getBateria() {
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = contexto.registerReceiver(null, ifilter);
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		return (level / (float)scale) * 100;
	}

	@SuppressLint("NewApi")
	public void startCliente() {
		iClient = true;

		if (ap != null) {
			while (!ap.isCancelled()) {
				ap.cancel(true);
				Log.d("NETHOT", "NETHOT - Cancelando Servidor");
			}
		}

		ap = null;
		iServer = false;

		if (sta == null) {
			Log.d("NETHOT", "NETHOT - " + "Startando Client");
			sta = new StateSTA(getServerIP(), 5555, this);
			sta.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

			ipList.clear();
		}
	}

	@SuppressLint("NewApi")
	public void startServer() {
		iServer = true;

		if (sta != null) {
			while (!sta.isCancelled()) {
				sta.cancel(true);
				Log.d("NETHOT", "NETHOT - Cancelando Get Mensagens");
			}
		}

		sta = null;
		iClient = false;

		if (ap == null) {
			Log.d("NETHOT", "NETHOT - Startando Server");
			ap = new StateAP(5555, this);
			ap.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			ipList.clear();
		}
	}

	public String getMyIP() {
		WifiManager wifiManager = (WifiManager)contexto.getSystemService(Context.WIFI_SERVICE);
		int ips = wifiManager.getConnectionInfo().getIpAddress();
		String ipAddress = Formatter.formatIpAddress(ips);
		return ipAddress;
	}

	public String getServerIP() {
		WifiManager wifiManager = (WifiManager)contexto.getSystemService(Context.WIFI_SERVICE);
		int ips = wifiManager.getConnectionInfo().getIpAddress();
		@SuppressWarnings("deprecation")
		String ipAddress = Formatter.formatIpAddress(ips);
		return ipAddress.substring(0, ipAddress.lastIndexOf(".")+1) + "1";
	}
	
	public String getMyName() {
		WifiManager manager = (WifiManager)contexto.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = manager.getConnectionInfo();
		return info.getMacAddress();
	}

}