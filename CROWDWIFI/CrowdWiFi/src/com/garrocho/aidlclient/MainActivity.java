package com.garrocho.aidlclient;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aidlclient.R;
import com.garrocho.aidlserver.INetOpp;

public class MainActivity extends Activity  {

	EditText texto;
	Button mAdd, mDell, mStart, mStop;
	protected INetOpp mService;
	ServiceConnection mServiceConnection;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		texto = (EditText) findViewById(R.id.firstValue);
		mAdd = (Button) findViewById(R.id.add);
		mDell = (Button) findViewById(R.id.dell);
		mStart = (Button) findViewById(R.id.start);
		mStop = (Button) findViewById(R.id.stop);

		mAdd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					if (mService.addFile(texto.getText().toString())) {
						Toast.makeText(getApplicationContext(), "ADD FILE - YES", Toast.LENGTH_SHORT).show();
					}
					else {
						Toast.makeText(getApplicationContext(), "ADD FILE - NO", Toast.LENGTH_SHORT).show();
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}

			}
		});

		mDell.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					if (mService.delFile(texto.getText().toString())) {
						Toast.makeText(getApplicationContext(), "DELL FILE - YES", Toast.LENGTH_SHORT).show();
					}
					else {
						Toast.makeText(getApplicationContext(), "DELL FILE - NO", Toast.LENGTH_SHORT).show();
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}

			}
		});
		
		mStart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					if (mService.startNetwork()) {
						Toast.makeText(getApplicationContext(), "START NET - YES", Toast.LENGTH_SHORT).show();
					}
					else {
						Toast.makeText(getApplicationContext(), "START NET - NO", Toast.LENGTH_SHORT).show();
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}

			}
		});
		
		mStop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					if (mService.stopNetwork()) {
						Toast.makeText(getApplicationContext(), "STOP NET - YES", Toast.LENGTH_SHORT).show();
					}
					else {
						Toast.makeText(getApplicationContext(), "STOP NET - NO", Toast.LENGTH_SHORT).show();
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}

			}
		});

		initConnection();

	}
	void initConnection(){
		mServiceConnection = new ServiceConnection() {



			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
				mService = null;
				Toast.makeText(getApplicationContext(), "no", Toast.LENGTH_SHORT).show();
				Log.d("IRemote", "Binding - Service disconnected");
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service)
			{
				// TODO Auto-generated method stub
				mService = INetOpp.Stub.asInterface((IBinder) service);
				Toast.makeText(getApplicationContext(), "yes", Toast.LENGTH_SHORT).show();
				Log.d("IRemote", "Binding is done - Service connected");
			}
		};
		if(mService == null)
		{
			Intent it = new Intent();
			it.setAction("com.remote.service.NETOPP");
			//binding to remote service
			bindService(it, mServiceConnection, Service.BIND_AUTO_CREATE);
		}
	}
	protected void onDestroy() {

		super.onDestroy();
		unbindService(mServiceConnection);
	};
}
