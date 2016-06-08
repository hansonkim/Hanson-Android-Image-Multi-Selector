package com.moriahtown.imagemultiselection;

import java.util.ArrayList;

import android.content.Context;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.moriahtown.imagemultiselection.ImageCursorAdapter.OnListClickListener;
import com.moriahtown.imagemultiselection.dataobj.ImageItem;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ImageListAdapter extends ArrayAdapter<ImageItem>
{
	private Context mContext;
	private ArrayList<String> mSelecteImage;
	private String mBucketId;
	private String mBucketName;
	private OnListClickListener mOnImageItemClickListener;

	public ImageListAdapter(Context context, int resource)
	{
		super(context, resource);
		this.mContext = context;
	}

	public ImageListAdapter(Context context, ArrayList<ImageItem> list,
	        ArrayList<String> seletedImages, String bucketId,
	        String bucketName, OnListClickListener onImageItemClickListener)
	{
		super(context, 0, 0, list);
		this.mContext = context;
		this.mSelecteImage = seletedImages;
		this.mBucketId = bucketId;
		this.mBucketName = bucketName;
		this.mOnImageItemClickListener = onImageItemClickListener;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		View view = convertView;
		final Holder holder;
		if (convertView == null)
		{
			view = LayoutInflater.from(mContext).inflate(
			        R.layout.item_image_grid, parent, false);
			holder = new Holder();
			holder.iv = (ImageView) view.findViewById(R.id.iv_image);
			holder.cb = (CheckBox) view.findViewById(R.id.cb_check);
			view.setTag(holder);
		}
		else
		{
			holder = (Holder) view.getTag();
		}

		final ImageItem item = getItem(position);

		if (item.getThumbUri() != null)
		{
			ImageLoader.getInstance().displayImage(
			        "file://" + item.getThumbUri().toString(), holder.iv);
		}
		else
		{
			holder.iv.setImageBitmap(MediaStore.Images.Thumbnails.getThumbnail(
			        mContext.getContentResolver(), item.getOrgId(),
			        MediaStore.Images.Thumbnails.MINI_KIND, null));
		}

		checkSelection(item);
		holder.cb.setChecked(item.isChecked());

		holder.cb.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				boolean isInserted = mOnImageItemClickListener
				        .onListCheckClick(item);
				CheckBox cb = (CheckBox) v.findViewById(R.id.cb_check);
				cb.setChecked(isInserted);
				item.setChecked(isInserted);
			}
		});

		view.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				if (mOnImageItemClickListener != null)
				{
					mOnImageItemClickListener.onListImageClick(mBucketId,
					        mBucketName, item);
				}
			}
		});

		return view;
	}

	private void checkSelection(ImageItem item)
	{
		if (mSelecteImage != null && mSelecteImage.size() > 0)
		{
			item.setChecked(mSelecteImage.contains(String.valueOf(item
			        .getOrgId())));
		}
		else
		{
			item.setChecked(false);
		}
	}

	private class Holder
	{
		ImageView iv;
		CheckBox cb;
	}
}
