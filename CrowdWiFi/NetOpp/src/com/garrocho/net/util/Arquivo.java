package com.garrocho.net.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class Arquivo {

	public static void gravaImagem(Context contexto, InputStream inPut, String nomeArquivo, String pasta) {
		Log.d("NETHOT", "NETHOT - Gravando " + nomeArquivo);
		try {
			byte[] buffer = new byte[5544545];
			File wallpaperDirectory = new File(pasta);
			wallpaperDirectory.mkdirs();
			File outputFile = new File(wallpaperDirectory, nomeArquivo);
			FileOutputStream fis = new FileOutputStream(outputFile);
			int count;
			while ((count = inPut.read(buffer)) > 0) {
				fis.write(buffer, 0, count);
				fis.flush();  
			}
			fis.close();
			Log.d("NETHOT", "NETHOT - Gravado com Sucesso! - " + nomeArquivo);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void gravaImagemAtual(Context contexto, FileInputStream imagemAtual, String nomeArquivo) {

		try {
			byte[] buffer = new byte[512];
			FileOutputStream fis = contexto.openFileOutput(nomeArquivo, Activity.MODE_PRIVATE);
			int count;
			while ((count = imagemAtual.read(buffer)) > 0) {
				fis.write(buffer, 0, count);
				fis.flush();  
			}
			fis.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public static Drawable carregaDrawable(Context contexto, String nomeArquivo) {
		if (!nomeArquivo.contains("/")) {
			try {
				File imageFile = new File(nomeArquivo);
				return new BitmapDrawable(contexto.getResources(), imageFile.getAbsolutePath());
			} catch (Exception e) {
				Bitmap bm = BitmapFactory.decodeFile(nomeArquivo);
				bm.recycle();
				return new BitmapDrawable(bm);
			}
		}
		else {
			try {
				File imageFile = new File(nomeArquivo);
				return new BitmapDrawable(contexto.getResources(), imageFile.getAbsolutePath());
			} catch (Exception e) {
				Bitmap bm = BitmapFactory.decodeFile(nomeArquivo);
				bm.recycle();
				return new BitmapDrawable(bm);
			}
		}
	}

	public static FileInputStream carregaInputStream(Context contexto, String nomeArquivo) {
		try {
			File file = new File(nomeArquivo);
			if (file.exists())
				return new FileInputStream(file);
			return null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}