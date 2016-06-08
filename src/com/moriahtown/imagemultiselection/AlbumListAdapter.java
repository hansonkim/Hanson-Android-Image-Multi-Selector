package com.moriahtown.imagemultiselection;

import java.util.List;

import com.moriahtown.imagemultiselection.dataobj.Album;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AlbumListAdapter extends ArrayAdapter<Album>
{

	private Context mContext;

	public AlbumListAdapter(Context context)
	{
		super(context, 0);
		mContext = context;
	}

	public AlbumListAdapter(Context context, List<Album> list)
	{
		super(context, 0, 0, list);
		mContext = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view = convertView;
		final Holder holder;
		if (convertView == null)
		{
			view = LayoutInflater.from(mContext).inflate(
			        R.layout.row_album_list, parent, false);
			holder = new Holder();
			holder.ivThumb = (ImageView) view.findViewById(R.id.iv_thumb);
			holder.tvName = (TextView) view.findViewById(R.id.tv_name);
			holder.tvCount = (TextView) view.findViewById(R.id.tv_count);
			view.setTag(holder);
		}
		else
		{
			holder = (Holder) view.getTag();
		}
		Album album = getItem(position);
		holder.tvName.setText(album.getBucketDisplayName());
		holder.ivThumb.setImageURI(Uri.parse(album.getData()));
		holder.tvCount.setText(String.valueOf(album.getCount()));

		return view;
	}

	private class Holder
	{
		ImageView ivThumb;
		TextView tvName;
		TextView tvCount;
	}

}
