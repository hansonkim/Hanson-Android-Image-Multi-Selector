package com.moriahtown.imagemultiselection;

import java.util.ArrayList;

import com.moriahtown.imagemultiselection.util.ImageProvider;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class SelectedImageListAdapter extends ArrayAdapter<String>
{
	private Context mContext;
	private OnDeleteClickListener mOnDeleteClickListener;

	public SelectedImageListAdapter(Context context, ArrayList<String> list,
	        OnDeleteClickListener onDeleteClickListener)
	{
		super(context, 0, list);
		mContext = context;
		mOnDeleteClickListener = onDeleteClickListener;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		View view = convertView;
		final Holder holder;
		if (convertView == null)
		{
			view = LayoutInflater.from(mContext).inflate(
			        R.layout.item_selected_image, null);
			holder = new Holder();
			holder.ivImage = (ImageView) view.findViewById(R.id.iv_image);
			holder.ivDelete = (ImageView) view.findViewById(R.id.iv_delete);
			view.setTag(holder);
		}
		else
		{
			holder = (Holder) view.getTag();
		}

		final String item = getItem(position);
		final long orgId = Long.valueOf(item);
		String thumbUriString = ImageProvider.getThumbUriString(mContext
		        .getContentResolver(), orgId);
		if(!TextUtils.isEmpty(thumbUriString))
		{
			holder.ivImage.setImageURI(Uri.parse(thumbUriString));
		}
		else
		{
			holder.ivImage.setImageBitmap(MediaStore.Images.Thumbnails.getThumbnail(
				        mContext.getContentResolver(), orgId, MediaStore.Images.Thumbnails.MINI_KIND, null));
		}
		
		view.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				mOnDeleteClickListener.onDeleteClick(item, position);
			}
		});

		return view;

	}

	private class Holder
	{
		ImageView ivImage;
		ImageView ivDelete;
	}

	public interface OnDeleteClickListener
	{
		void onDeleteClick(String orgId, int position);
	}
}
