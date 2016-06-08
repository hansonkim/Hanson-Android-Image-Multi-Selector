package com.moriahtown.imagemultiselection;

import java.util.ArrayList;

import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.moriahtown.imagemultiselection.ImageCursorAdapter.OnListClickListener;
import com.moriahtown.imagemultiselection.util.ImageProvider;

public class ImageListFragment extends Fragment
{

	public final static String TAG = "ImageListFragment";
	public final static String KEY_SELECTED_IMAGE = "key_selected_image";

	private GridView mGridView;
	// private ImageListAdapter mAdapter;
	private ImageCursorAdapter mAdapter;

	private ArrayList<String> mSelecteImage;
	private TextView tvTitle;
	private ProgressBar mProgressbar;

	private String mBucketId;
	private String mBucketName;

	private OnListClickListener mOnImageItemClickListener;

	public static ImageListFragment newInstance(String bucketId,
	        String bucketDisplayName, ArrayList<String> selecteImage,
	        OnListClickListener onImageItemClickListener)
	{
		ImageListFragment fragment = new ImageListFragment();

		Bundle bundle = new Bundle();
		bundle.putString(ImageColumns.BUCKET_ID, bucketId);
		bundle.putString(ImageColumns.BUCKET_DISPLAY_NAME, bucketDisplayName);
		bundle.putStringArrayList(KEY_SELECTED_IMAGE, selecteImage);
		fragment.setArguments(bundle);
		fragment.setOnImageItemClickListener(onImageItemClickListener);

		return fragment;
	}

	public void setOnImageItemClickListener(
	        OnListClickListener onImageItemClickListener)
	{
		this.mOnImageItemClickListener = onImageItemClickListener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState)
	{

		mBucketId = getArguments().getString(
		        MediaStore.Images.ImageColumns.BUCKET_ID);
		mBucketName = getArguments().getString(
		        MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);
		mSelecteImage = getArguments().getStringArrayList(KEY_SELECTED_IMAGE);

		View view = initView(inflater);
		updateList();

		return view;
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}

	private View initView(LayoutInflater inflater)
	{
		View view = inflater.inflate(R.layout.fragment_image_list, null);
		mGridView = (GridView) view.findViewById(R.id.gv_images);
		tvTitle = (TextView) view.findViewById(R.id.tv_title);
		mProgressbar = (ProgressBar) view.findViewById(R.id.progressbar);

		return view;
	}

	public void updateList()
	{
		tvTitle.setText(mBucketName);
		
		mAdapter = new ImageCursorAdapter(getActivity(), ImageProvider
		        .getImagesCursorInBucket(getActivity().getContentResolver(),
		                mBucketId), mSelecteImage, mBucketId, mBucketName, mOnImageItemClickListener);
		mGridView.setAdapter(mAdapter);

		mAdapter.notifyDataSetChanged();
	}

	public void reload(String bucketId, String bucketName,
	        ArrayList<String> list)
	{
		if (!mBucketId.equals(bucketId) || !mBucketName.equals(bucketName)
		        || !mSelecteImage.equals(list))
		{
			mGridView.setAdapter(null);
			mBucketId = bucketId;
			mBucketName = bucketName;
			mSelecteImage = list;
			updateList();
		}
	}

	public void showProgress()
	{
		mProgressbar.setVisibility(View.VISIBLE);
	}

	public void hideProgress()
	{
		mProgressbar.setVisibility(View.GONE);
	}

	public void uncheckImage(String orgId)
	{
		// checkSelection();
		mAdapter.notifyDataSetChanged();
	}
}
