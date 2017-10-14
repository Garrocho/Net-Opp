package com.garrocho.net;

import static com.garrocho.net.util.Arquivo.gravaImagem;
import android.os.AsyncTask;

import com.garrocho.aidlserver.NetOppService;
import com.garrocho.net.util.ConexaoCliente;

public class GetFile extends AsyncTask<Void, Void, Void> {

	public GerenciadorRede main;
	public String arquivo;
	public boolean open;

	public GetFile(String arquivo, GerenciadorRede main, boolean open) {
		this.main = main;
		this.arquivo = arquivo;
		this.open = open;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Void doInBackground(Void... arg0) {

		for (String ip : main.ipList) {

			if (ip.equalsIgnoreCase("0.0.0.0"))
				ip = main.getServerIP();
			if (!ip.equalsIgnoreCase(main.getMyIP())) {
				ConexaoCliente conexao = new ConexaoCliente(ip, 4545);
				if (conexao.conectaServidor()) {
					try {
						conexao.getEnviaDados().writeObject(new String[]{this.arquivo});
						conexao.getEnviaDados().flush();

						if (((String)(conexao.getRecebeDados().readObject())).equalsIgnoreCase("EXISTENTE")) {
							gravaImagem(main.contexto, conexao.getRecebeDados(), this.arquivo, main.pasta);
							break;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						conexao.desconectaServidor();
						Thread.sleep(2000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}
}