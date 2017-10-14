package com.garrocho.crowd;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

import com.coderplus.filepicker.FilePickerActivity;
import com.garrocho.aidlserver.INetOpp;
import com.garrocho.crowd.util.MyObj;
import com.garrocho.crowd.util.ViewHolderAdapter;
import com.idunnololz.widgets.AnimatedExpandableListView;
import com.idunnololz.widgets.AnimatedExpandableListView.AnimatedExpandableListAdapter;

import custom.fileobserver.FileListener;
import custom.fileobserver.FileWatcher;


public class Main extends Activity {

	private ViewHolderAdapter mAdapter;
	public List<String> listaIP = new ArrayList<String>();
	public Map<String, String> listaIpOk = new HashMap<String, String>();
	public List<String> downFiles = new ArrayList<String>();
	public String nome = null;
	public int posicao = 1;
	public Semaphore sem = new Semaphore(100, true);

	public Map<String, GroupItem> listaPastas = new HashMap<String, Main.GroupItem>();
	public List<ChildItem> minhaPasta  = new ArrayList<ChildItem>();

	protected INetOpp mService;
	ServiceConnection mServiceConnection;

	FileWatcher mWatcher;
	public String pastaSelecionada = null;
	private AnimatedExpandableListView listView;
	private ExampleAdapter adapter;

	public ProgressDialog progressDialog;

	void initConnection(){
		mServiceConnection = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
				mService = null;
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service)
			{
				// TODO Auto-generated method stub
				mService = INetOpp.Stub.asInterface((IBinder) service);
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = manager.getConnectionInfo();
		nome = info.getMacAddress();

		String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
		Intent intent = new Intent(this, FilePickerActivity.class);
		intent.putExtra(FilePickerActivity.EXTRA_SELECT_DIRECTORIES_ONLY, true);
		intent.putExtra(FilePickerActivity.EXTRA_FILE_PATH, sdcard);
		startActivityForResult(intent, 777);

		try {
			mService.startNetwork();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public float getBateria() {
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = registerReceiver(null, ifilter);

		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		return (level / (float)scale) * 100;
	}

	public File[] getArquivosSelecionada() {
		File file = new File(pastaSelecionada);
		return file.listFiles();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultcode, Intent intent)
	{
		super.onActivityResult(requestCode, resultcode, intent);

		switch (requestCode) {

		case 777:
			if(intent.hasExtra(FilePickerActivity.EXTRA_FILE_PATH)) {
				@SuppressWarnings("unchecked")
				List<File> files = (List<File>)intent.getSerializableExtra(FilePickerActivity.EXTRA_FILE_PATH);                   
				for(File file:files){
					if(file.isDirectory())
						Log.d("FILES", "FILES - " + file.getAbsolutePath());
					else
						Log.d("FILES", "FILES - " + file.getAbsolutePath());
					pastaSelecionada = file.getAbsolutePath();
					setTitle("Crowd WiFi - " + nome + " - " + pastaSelecionada);
					mWatcher = new FileWatcher(pastaSelecionada, true, FileWatcher.FILE_CHANGED);
					mWatcher.setFileListener(mFileListener);
					mWatcher.startWatching();

					file = new File( pastaSelecionada ) ;       
					File list[] = file.listFiles();

					List<ChildItem> minhaPastaCOPY  = new ArrayList<ChildItem>();

					for( int i=0; i< list.length; i++)
					{
						ChildItem item = new ChildItem();
						item.hint = "nao visualizado.";
						item.title = list[i].getName();
						try {
							mService.addFile(list[i].getName());
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						minhaPastaCOPY.add(item);
					}
					GroupItem gi = new GroupItem();
					gi.items = minhaPastaCOPY;
					gi.title = nome;
					minhaPasta = minhaPastaCOPY;
					listaPastas.put(nome, gi);

					configurarListTreeView();
				}                                     
			}
		}
	}

	public void configurarListTreeView() {

		List<GroupItem> items = new ArrayList<GroupItem>(listaPastas.values());

		if (adapter != null) {
			adapter.setData(items);
			adapter.notifyDataSetChanged();
		}
		else {
			adapter = new ExampleAdapter(this);
			adapter.setData(items);

			listView = (AnimatedExpandableListView) findViewById(R.id.listView);
			listView.setAdapter(adapter);
			listView.setOnGroupClickListener(new OnGroupClickListener() {

				@Override
				public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
					if (listView.isGroupExpanded(groupPosition)) {
						listView.collapseGroupWithAnimation(groupPosition);
					} else {
						listView.expandGroupWithAnimation(groupPosition);
					}
					return true;
				}
			});

			listView.setOnChildClickListener(new OnChildClickListener() {

				@Override
				public boolean onChildClick(ExpandableListView arg0, View clickedView, int groupPosition, int childPosition, long id) {
					Log.d("CROWD", "CROWD - " + groupPosition + " - " + childPosition + " - " + id);
					Log.d("CROWD", "CROWD - " + ((GroupItem)adapter.getGroup(groupPosition)).title);
					Log.d("CROWD", "CROWD - " + ((ChildItem)adapter.getChild(groupPosition, childPosition)).title);

					progressDialog = new ProgressDialog(Main.this);
					progressDialog.setTitle("Carregando Arquivo");
					progressDialog.setMessage("Aguarde...");
					progressDialog.setIndeterminate(true);
					progressDialog.show();

					if (!nome.equalsIgnoreCase(((GroupItem)adapter.getGroup(groupPosition)).title)) {
						String aux = ((ChildItem)adapter.getChild(groupPosition, childPosition)).title;
						boolean tem = false;
						for (File file : getArquivosSelecionada()) {
							if (file.getName().equalsIgnoreCase(aux)) {
								tem = true;
							}
						}

						if (!tem) {
							try {
								mService.addFile(aux);
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						}
						else {
							aux = pastaSelecionada + "/" + aux;
							abrirArquivo(aux);
						}
					}
					else {
						abrirArquivo(pastaSelecionada + "/" +  ((ChildItem)adapter.getChild(groupPosition, childPosition)).title);
					}
					return false;
				}
			});
		}
	}

	public void abrirArquivo(String arq) {
		progressDialog.dismiss();
		try
		{
			File file = new File(arq);
			Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW);
			file = new File(file.getAbsolutePath()); 
			String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
			String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
			myIntent.setDataAndType(Uri.fromFile(file), mimetype);
			startActivity(myIntent);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	public static ViewHolderAdapter buildViewHolderAdapter(Activity context, int textViewResourceId) {

		ArrayList<MyObj> list = new ArrayList<MyObj>();
		ViewHolderAdapter viewHolder = new ViewHolderAdapter(context, textViewResourceId);
		viewHolder.addAll(list);
		return viewHolder;
	}

	FileListener mFileListener = new FileListener(){

		@Override
		public void onFileCreated(String name) {
			Log.d("FILES", "FILES - " + "onFileCreated - " + name);
			File file = new File( pastaSelecionada ) ;       
			File list[] = file.listFiles();

			minhaPasta = new ArrayList<ChildItem>();

			for( int i=0; i< list.length; i++)
			{
				ChildItem item = new ChildItem();
				item.hint = "nao visualizado.";
				item.title = list[i].getName();
				minhaPasta.add(item);
			}
			GroupItem gi = new GroupItem();
			gi.items = minhaPasta;
			gi.title = nome;

			if (listaPastas.containsKey(nome)) {
				listaPastas.remove(nome);
			}
			listaPastas.put(nome, gi);
		}

		@Override
		public void onFileDeleted(String name) {
			Log.d("FILES", "FILES - " + "onFileDeleted - " + name);
			File file = new File( pastaSelecionada ) ;       
			File list[] = file.listFiles();

			minhaPasta  = new ArrayList<ChildItem>();

			for( int i=0; i< list.length; i++)
			{
				ChildItem item = new ChildItem();
				item.hint = "nao visualizado.";
				item.title = list[i].getName();
				minhaPasta.add(item);
			}
			GroupItem gi = new GroupItem();
			gi.items = minhaPasta;
			gi.title = nome;

			if (listaPastas.containsKey(nome)) {
				listaPastas.remove(nome);
			}
			listaPastas.put(nome, gi);
		}

		@Override
		public void onFileModified(String name) {
			Log.d("FILES", "FILES - " + "onFileModified - " + name);
			//configurarListTreeView();
		}

		@Override
		public void onFileRenamed(String oldName, String newName) {
			Log.d("FILES", "FILES - " + "onFileRenamed from: " + oldName + " to: " + newName);
			File file = new File( pastaSelecionada ) ;       
			File list[] = file.listFiles();

			List<ChildItem> minhaPastaCOPY  = new ArrayList<ChildItem>();

			for( int i=0; i< list.length; i++)
			{
				ChildItem item = new ChildItem();
				item.hint = "nao visualizado.";
				item.title = list[i].getName();
				minhaPastaCOPY.add(item);
			}
			GroupItem gi = new GroupItem();
			gi.items = minhaPastaCOPY;
			gi.title = nome;

			if (listaPastas.containsKey(nome)) {
				listaPastas.remove(nome);
			}
			minhaPasta = minhaPastaCOPY;
			listaPastas.put(nome, gi);
		}

	};
	@Override
	protected void onResume() {

		super.onResume();
	}

	@Override
	protected void onPause() {

		super.onPause();
	}

	@Override
	protected void onUserLeaveHint() {

		super.onUserLeaveHint();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		//mWatcher.stopWatching();
		super.onDestroy();
		unbindService(mServiceConnection);
	}

	public static class GroupItem implements Serializable {
		private static final long serialVersionUID = 1L;
		public String title;
		public List<ChildItem> items = new ArrayList<ChildItem>();
	}

	public static class ChildItem implements Serializable {
		private static final long serialVersionUID = 1L;
		public String title;
		public String hint;
	}

	public static class ChildHolder implements Serializable {
		private static final long serialVersionUID = 1L;
		TextView title;
	}

	public static class GroupHolder implements Serializable {
		private static final long serialVersionUID = 1L;
		TextView title;
	}

	/**
	 * Adapter for our list of {@link GroupItem}s.
	 */
	public class ExampleAdapter extends AnimatedExpandableListAdapter {
		private LayoutInflater inflater;

		private List<GroupItem> items;

		public ExampleAdapter(Context context) {
			inflater = LayoutInflater.from(context);
		}

		public void setData(List<GroupItem> items) {
			this.items = items;
		}

		@Override
		public ChildItem getChild(int groupPosition, int childPosition) {
			return items.get(groupPosition).items.get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getRealChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			ChildHolder holder;
			ChildItem item = getChild(groupPosition, childPosition);
			if (convertView == null) {
				holder = new ChildHolder();
				convertView = inflater.inflate(R.layout.list_item, parent, false);
				holder.title = (TextView) convertView.findViewById(R.id.textTitle);
				convertView.setTag(holder);
			} else {
				holder = (ChildHolder) convertView.getTag();
			}

			holder.title.setText(item.title);

			return convertView;
		}

		@Override
		public int getRealChildrenCount(int groupPosition) {
			return items.get(groupPosition).items.size();
		}

		@Override
		public GroupItem getGroup(int groupPosition) {
			return items.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return items.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			GroupHolder holder;
			GroupItem item = getGroup(groupPosition);
			if (convertView == null) {
				holder = new GroupHolder();
				convertView = inflater.inflate(R.layout.group_item, parent, false);
				holder.title = (TextView) convertView.findViewById(R.id.textTitle);
				convertView.setTag(holder);
			} else {
				holder = (GroupHolder) convertView.getTag();
			}

			holder.title.setText(item.title);

			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int arg0, int arg1) {
			return true;
		}

	}
}