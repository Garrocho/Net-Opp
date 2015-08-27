package com.garrocho.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.garrocho.aidlserver.NetOppService;
import com.garrocho.net.util.Cliente;

public class StateAP extends AsyncTask<Void, String, Void> {

	protected int          portaServidor    = 5555;
	protected ServerSocket soqueteServidor  = null;
	protected boolean      isStopped        = false;
	protected GerenciadorRede  main			= null;
	public List<Cliente> listaAP = new ArrayList<Cliente>();
	private ObjectOutputStream enviaDados;
	private ObjectInputStream recebeDados;

	public StateAP(int porta, GerenciadorRede main){
		this.portaServidor = porta;
		this.main = main;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@SuppressLint("NewApi")
	@Override
	protected synchronized Void doInBackground(Void... arg0) {
		
		try {

			this.soqueteServidor.close();
			this.soqueteServidor = null;
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (main.iServer) {

			if (this.soqueteServidor == null) {

				abrirSoqueteServidor();
			}
			else{

				Socket soqueteCliente = null;
				try {
					soqueteCliente = this.soqueteServidor.accept();
				} catch (IOException e) {
					e.printStackTrace();
				}

				//obtendo os fluxos de entrada e saida.
				try {
					enviaDados = new ObjectOutputStream(soqueteCliente.getOutputStream());
					recebeDados = new ObjectInputStream(soqueteCliente.getInputStream());

					final String req[] = (String[])recebeDados.readObject();

					if (req[0].equalsIgnoreCase("PUT")) {
						
						/*main.sem.acquire();
						if (!main.listaIpOk.containsKey(req[2]))
							main.listaIpOk.put(req[2], req[1]);
						main.sem.release();*/

						String nomeCli = req[1];

						/*List<ChildItem> listaPasta = (List<ChildItem>)recebeDados.readObject();
						if (main.listaPastas.containsKey(nomeCli)) {
							main.listaPastas.remove(nomeCli);
						}

						GroupItem gi = new GroupItem();
						gi.title = nomeCli;
						gi.items = listaPasta;

						main.listaPastas.put(nomeCli, gi);

						myHandler.post(new Runnable() {
							public void run() {
								main.configurarListTreeView();
							}
						});*/
					}
					else if (req[0].equalsIgnoreCase("LIST")) {
						enviaDados.writeObject(main.fileList);
						enviaDados.writeObject(main.ipList.toArray(new String[main.ipList.size()]));

						List<Cliente> aux = listaAP;
						Collections.sort(aux);

						int menor = 99999;
						int maior = 0;

						for (Cliente cli : listaAP) {
							for (int i=0; i < aux.size(); i++) {

							}
						}

						int posicao = 1;
						for (Cliente ipt: aux) {
							if (ipt.name.equalsIgnoreCase(req[1]))
								break;
							else
								posicao++;
						}
						enviaDados.writeObject(String.valueOf(posicao));
						//enviaDados.writeObject(main.downFiles.toArray(new String[main.listaIP.size()]));
					}
					else if (req[0].equalsIgnoreCase("AP")) {
						final String nome = req[1];
						final Integer bateria = Integer.parseInt(req[2]);
						boolean tem = false;
						int location = 0;
						for (Cliente cli : listaAP) {
							if (cli.name.equalsIgnoreCase(nome)) {
								tem = true;
								location = listaAP.indexOf(cli);
							}
						}
						if (!tem)
							listaAP.add(new Cliente(nome, bateria));
						else {
							listaAP.remove(location);
							listaAP.add(new Cliente(nome, bateria));
						}
					}
					else if (req[0].equalsIgnoreCase("DOWN")) {
						//if (!main.downFiles.contains(req[1])) {
						//	main.downFiles.add(req[1]);
						//	new TaskGetFile(req[1], main, false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						//}
					}

					enviaDados.close();
					recebeDados.close();
					soqueteCliente.close();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private boolean abrirSoqueteServidor() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		try {
			this.soqueteServidor = new ServerSocket(this.portaServidor);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

}