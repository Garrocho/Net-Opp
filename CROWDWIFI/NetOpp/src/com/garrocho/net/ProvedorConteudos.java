package com.garrocho.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.garrocho.aidlserver.MainActivity;

public class ProvedorConteudos extends AsyncTask<Void, String, Void> {

	protected int          portaServidor    = 4545;
	protected ServerSocket soqueteServidor  = null;
	protected boolean      isStopped        = false;
	protected MainActivity		   main 			= null;
	public long total = 0;

	public ProvedorConteudos(int porta, MainActivity main){
		this.portaServidor = porta;
		this.main = main;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@SuppressLint("NewApi")
	@Override
	protected Void doInBackground(Void... arg0) {
		abrirSoqueteServidor();
		while (true) {
			try {
				Thread t = new Thread(new ClientWorker(this.soqueteServidor.accept()));
				t.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class ClientWorker implements Runnable {
		private Socket soqueteCliente;
		private String nome;
		private ObjectOutputStream enviaDados;
		private ObjectInputStream recebeDados;

		ClientWorker(Socket soqueteCliente) {
			this.soqueteCliente = soqueteCliente;
		}
		public void run(){
			try {
				this.enviaDados = new ObjectOutputStream(soqueteCliente.getOutputStream());
				this.recebeDados = new ObjectInputStream(soqueteCliente.getInputStream());

				final String[] req = (String[])recebeDados.readObject();

				this.nome = req[0];

				boolean SAIR = false;
				File file = new File(nome);

				FileInputStream arquivoVideo = null;
				if (file.exists()) {
					arquivoVideo = new FileInputStream(file);

					enviaDados.writeObject("EXISTENTE");

					byte[] buf = new byte[5544545];
					while (SAIR != true) {  
						int tamanho = arquivoVideo.read(buf);  
						if (tamanho == -1) {
							SAIR = true;
							break;  
						}
						enviaDados.write(buf, 0, tamanho);  
					}
				}
				else {
					enviaDados.writeObject("INEXISTENTE");
				} 
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				enviaDados.close();
				recebeDados.close();
				soqueteCliente.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void abrirSoqueteServidor() {
		try {
			this.soqueteServidor = new ServerSocket(this.portaServidor);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}