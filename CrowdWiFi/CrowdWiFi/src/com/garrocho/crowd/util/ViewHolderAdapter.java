package com.garrocho.crowd.util;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.garrocho.crowd.R;

public class ViewHolderAdapter extends ArrayAdapter<MyObj> {

	private Activity mContext;
	private LayoutInflater mInflater;

	public ViewHolderAdapter(Activity context, int textViewResourceId) {
		super(context, textViewResourceId);
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	static class ViewHolder {
		TextView name;
		TextView longtext;
		//ImageView imagem;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item, parent, false);
			holder = new ViewHolder();
			//holder.name = (TextView) convertView.findViewById(R.id.name);
			//holder.longtext = (TextView) convertView.findViewById(R.id.text);
			//holder.imagem = (ImageView) convertView.findViewById(R.id.imageView1);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		MyObj data = getItem(position);

		holder.name.setText(data.name);
		holder.longtext.setText(data.text);
		//holder.imagem.setImageResource(R.drawable.transparente);
		//Drawable d = carregaDrawable(mContext, data.imagem);
		//holder.imagem.setImageDrawable(d);
		return convertView;
	}
}
