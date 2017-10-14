package com.garrocho.net;

import static com.garrocho.net.util.Arquivo.gravaImagem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;

import com.garrocho.aidlserver.NetOppService;
import com.garrocho.net.util.ConexaoCliente;

public class StateSTA extends AsyncTask<Void, Void, Void> {

	private int porta = 5555;
	public GerenciadorRede main;
	public ConexaoCliente conexao = null;
	public List<String> listaIP = new ArrayList<String>();
	public boolean continuar = true;
	public int contPUT = 0;

	public StateSTA(String ip, int porta, GerenciadorRede main) {
		this.porta = porta;
		this.main = main;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Void doInBackground(Void... arg0) {

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		int contBateria = 0;

		while (main.iClient && !isCancelled()){

			if (main.fileList != null) {

				conexao = new ConexaoCliente(main.getServerIP(), porta);
				if (conexao.conectaServidor()) {
					try {
						conexao.getEnviaDados().writeObject(new String[]{"PUT", main.getMyName(), main.getMyIP()});
						conexao.getEnviaDados().writeObject(main.fileList);
						conexao.getEnviaDados().flush();
						conexao.desconectaServidor();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			if (contBateria <= 0) {
				conexao = new ConexaoCliente(main.getServerIP(), porta);
				if (conexao.conectaServidor()) {
					try {
						conexao.getEnviaDados().writeObject(new String[]{"AP", main.getMyName(), String.valueOf((int) main.getBateria())});
						conexao.getEnviaDados().flush();
						conexao.desconectaServidor();

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				contBateria = 5;
			}

			else if (continuar && main.downList != null) {
				for (String arquivo : main.downList) {
					boolean baixou = false;
					if (arquivo != null) {
						for (String ip : listaIP) {
							if (ip.equalsIgnoreCase("0.0.0.0"))
								ip = main.getServerIP();
							if (!ip.equalsIgnoreCase(main.getMyIP())) {
								ConexaoCliente conexao = new ConexaoCliente(ip, 4545);
								if (conexao.conectaServidor()) {
									try {
										conexao.getEnviaDados().writeObject(new String[]{arquivo});
										conexao.getEnviaDados().flush();

										if (((String)(conexao.getRecebeDados().readObject())).equalsIgnoreCase("EXISTENTE")) {
											gravaImagem(main.contexto, conexao.getRecebeDados(), arquivo, main.pasta);
											baixou = true;
											break;
										}
									} catch (Exception e) {
										baixou = false;
									}
									try {
										conexao.desconectaServidor();
										Thread.sleep(2000);
									} catch (Exception e) {
									}
								}
							}
						}
					}
					if (baixou)
						break;
				}

				continuar = false;
			}
		}
		return null;
	}

	@Override
	protected void onCancelled() {
		if (conexao != null) {
			try {
				conexao.desconectaServidor();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		super.onCancelled();
	}
}