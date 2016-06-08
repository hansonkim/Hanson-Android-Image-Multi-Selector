package com.moriahtown.imagemultiselection;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.moriahtown.imagemultiselection.dataobj.ImageItem;
import com.moriahtown.imagemultiselection.util.ImageProvider;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class ImageCursorAdapter extends CursorAdapter
{
	private final static String TAG = "ImageCursorAdapter";

	private String mBucketId;
	private String mBucketName;
	private OnListClickListener mOnImageItemClickListener;

	private ArrayList<String> mSelecteImage;

	private static String[] allProjection =
	{ "*" };

	private DisplayImageOptions options = new DisplayImageOptions.Builder()
	        .imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(
	                Bitmap.Config.ARGB_8888).showImageOnLoading(R.drawable.image_backgrund).build();

	public ImageCursorAdapter(Context context, Cursor c,
	        ArrayList<String> selectedImage, String bucketId,
	        String bucketName, OnListClickListener onImageItemClickListener)
	{
		super(context, c, true);
		this.mSelecteImage = selectedImage;
		this.mBucketId = bucketId;
		this.mBucketName = bucketName;
		this.mOnImageItemClickListener = onImageItemClickListener;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor)
	{
		ImageView iv = (ImageView) view.findViewById(R.id.iv_image);
		CheckBox cb = (CheckBox) view.findViewById(R.id.cb_check);

		final ImageItem item = ImageProvider.cursorToItem(context
		        .getContentResolver(), cursor);

		if (item.getThumbUri() != null)
		{
			Log.d(TAG, "load thumb");
			item.setThumbUri(item.getThumbUri());
			ImageLoader.getInstance().displayImage(
			        "file://" + item.getThumbUri().toString(), iv, options);
		}
		else
		{
			Log.d(TAG, "load bitmap");
			ImageLoader.getInstance().displayImage(
			        "file://" + item.getOrgUri().toString(), iv, options);
		}

		item.setChecked(checkSelection(item.getOrgId()));
		cb.setChecked(item.isChecked());
		cb.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				boolean isInserted = mOnImageItemClickListener
				        .onListCheckClick(item);
				CheckBox cb = (CheckBox) v;
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
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent)
	{

		return LayoutInflater.from(mContext).inflate(R.layout.item_image_grid,
		        parent, false);
	}

	private boolean checkSelection(long orgId)
	{
		if (mSelecteImage != null && mSelecteImage.size() > 0)
		{
			return mSelecteImage.contains(String.valueOf(orgId));
		}
		else
		{
			return false;
		}
	}

	public interface OnListClickListener
	{
		void
		        onListImageClick(String bucketId, String bucketName,
		                ImageItem item);

		boolean onListCheckClick(ImageItem imageItem);
	}
}
