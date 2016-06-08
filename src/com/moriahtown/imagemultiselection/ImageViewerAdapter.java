package com.moriahtown.imagemultiselection;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.moriahtown.imagemultiselection.dataobj.ImageItem;
import com.moriahtown.imagemultiselection.util.CursorPagerAdapter;
import com.moriahtown.imagemultiselection.util.ImageProvider;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class ImageViewerAdapter extends CursorPagerAdapter
{
	private Context mContext;
	private DisplayImageOptions options = new DisplayImageOptions.Builder()
	.imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(
			Bitmap.Config.ARGB_8888).showImageOnLoading(null).build();

	public ImageViewerAdapter(Context context, Cursor c)
	{
		super(context, c, true);
		this.mContext = context;
	}


	@Override
	public boolean isViewFromObject(View view, Object object)
	{
		return view == ((FrameLayout) object);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor)
	{
		ImageItem item = ImageProvider.cursorToItem(mContext
		        .getContentResolver(), cursor);
		ImageView imageView = (ImageView) view
		        .findViewById(R.id.iv_item_image_adapter);
		imageView.setAdjustViewBounds(true);
		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		ImageLoader.getInstance().displayImage(
		        "file://" + item.getOrgUri().toString(), imageView, options);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent)
	{
		return LayoutInflater.from(mContext).inflate(
		        R.layout.item_image_adapter, null);
	}

}
