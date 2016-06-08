package com.moriahtown.imagemultiselection;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.moriahtown.imagemultiselection.ImageCursorAdapter.OnListClickListener;
import com.moriahtown.imagemultiselection.dataobj.ImageItem;
import com.moriahtown.imagemultiselection.util.ImageProvider;
import com.moriahtown.imagemultiselection.util.WeakReferenceHandler;

public class ImageViewerFragment extends Fragment implements
        OnPageChangeListener, OnClickListener
{
	public final static String TAG = "ImageViewerFragment";

	public final static String KEY_SELECTED_IMAGE = "key_selected_image";
	public final static String KEY_CLICKED_IMAGE_ID = "key_clicked_image_id";

	private ImageViewerAdapter mAdapter;

	private ViewPager vp;

	private ArrayList<String> mSelecteImage;

	private TextView tvTitle;
	private CheckBox cb;
	private Button btnEdit;
	private ProgressBar mProgressbar;

	private String mBucketId;
	private String mBucketName;
	private long mClickedOrgId;
	private int mCurrentPosition; // 클릭한 이미지의 위치

	private onViewerClickListener mOnViewerClickListener;

	public static ImageViewerFragment newInstance(String bucketId,
	        String bucketDisplayName, ArrayList<String> selecteImage,
	        long orgId, OnListClickListener onImageItemClickListener)
	{
		ImageViewerFragment fragment = new ImageViewerFragment();

		Bundle bundle = new Bundle();
		bundle.putString(ImageColumns.BUCKET_ID, bucketId);
		bundle.putString(ImageColumns.BUCKET_DISPLAY_NAME, bucketDisplayName);
		bundle.putStringArrayList(KEY_SELECTED_IMAGE, selecteImage);
		bundle.putLong(KEY_CLICKED_IMAGE_ID, orgId);
		fragment.setArguments(bundle);

		return fragment;
	}

	public static ImageViewerFragment newInstance()
	{
		ImageViewerFragment fragment = new ImageViewerFragment();
		return fragment;

	}

	public void
	        setOnEditClickListener(onViewerClickListener onEditClickListener)
	{
		this.mOnViewerClickListener = onEditClickListener;
	}

	public void setClickedId(long orgId)
	{
		mClickedOrgId = orgId;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState)
	{
		Log.d(TAG, "onCreateView");
		View view = initView(inflater);

		mBucketId = getArguments().getString(
		        MediaStore.Images.ImageColumns.BUCKET_ID);
		mBucketName = getArguments().getString(
		        MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME);
		mSelecteImage = getArguments().getStringArrayList(KEY_SELECTED_IMAGE);
		mClickedOrgId = getArguments().getLong(KEY_CLICKED_IMAGE_ID);

		updateList();
		return view;
	}

	@Override
	public void onResume()
	{
		Log.d(TAG, "onResume");
		super.onResume();
	}

	private View initView(LayoutInflater inflater)
	{
		View view = inflater.inflate(R.layout.fragment_image_viewer, null);
		vp = (ViewPager) view.findViewById(R.id.vp);
		vp.setOnPageChangeListener(this);
		tvTitle = (TextView) view.findViewById(R.id.tv_title);
		cb = (CheckBox) view.findViewById(R.id.cb_check);
		cb.setOnClickListener(this);
		btnEdit = (Button) view.findViewById(R.id.btn_edit);
		btnEdit.setOnClickListener(this);
		mProgressbar = (ProgressBar) view.findViewById(R.id.progressbar);

		return view;
	}

	public void setCurrentItem()
	{
		vp.setCurrentItem(mCurrentPosition, false);
		updateViewState(mCurrentPosition);
	}

	public void updateViewState(int position)
	{
		mCurrentPosition = position;
		Cursor cursor = mAdapter.getCursor();
		cursor.moveToPosition(position);
		ImageItem item = ImageProvider.cursorToItem(getActivity()
		        .getContentResolver(), cursor);
		item.setChecked(checkSelected(item));
		cb.setChecked(item.isChecked());
		tvTitle.setText((mCurrentPosition + 1) + "/" + cursor.getCount());
	}

	private boolean checkSelected(final ImageItem item)
	{
		if (mSelecteImage != null && mSelecteImage.size() > 0)
		{
			return mSelecteImage.contains(String.valueOf(item.getOrgId()));
		}
		else
		{
			return false;
		}
	}

	public void updateList()
	{
		new AsyncTask<String, Long, Cursor>()
		{

			@Override
			protected void onPreExecute()
			{
				mHandler.sendEmptyMessage(MSG_START_LOAD_IMAGE);
				super.onPreExecute();
			}

			@Override
			protected Cursor doInBackground(String... params)
			{
				Log.d(TAG, "loadImages");
				ContentResolver cr = getActivity().getContentResolver();
				return ImageProvider.getImagesCursorInBucket(cr, mBucketId);
			}

			@Override
			protected void onPostExecute(Cursor cursor)
			{
				mAdapter = new ImageViewerAdapter(getActivity(), cursor);
				vp.setAdapter(mAdapter);
				updateViewState(0);
				getCurrentPosition(getActivity().getContentResolver());
				mHandler.sendEmptyMessage(MSG_END_LOAD_IMAGE);
				super.onPostExecute(cursor);
			}
		}.execute();
	}

	private void getCurrentPosition(final ContentResolver cr)
	{
		final Cursor cursor = mAdapter.getCursor();
		
		if (cursor == null)
			return;

		mCurrentPosition = 0;

		for (int i = 0; i < cursor.getCount(); i++)
		{
			cursor.moveToPosition(i);
			ImageItem item = ImageProvider.cursorToItem(cr, cursor);
			if (item.getOrgId() == mClickedOrgId)
			{
				mCurrentPosition = i;
				break;
			}
		}

	}

	public void reload(String bucketId, String bucketName,
	        ArrayList<String> list, long clickedOrgId)
	{
		if (mBucketId.equals(bucketId) && mBucketName.equals(bucketName))
		{
			mClickedOrgId = clickedOrgId;
			
			new AsyncTask<String, Long, Boolean>()
			{

				@Override
				protected void onPreExecute()
				{
					mHandler.sendEmptyMessage(MSG_START_LOAD_IMAGE);
					super.onPreExecute();
				}

				@Override
				protected Boolean doInBackground(String... params)
				{
					getCurrentPosition(getActivity().getContentResolver());
					return true;
				}

				@Override
				protected void onPostExecute(Boolean result)
				{
					mHandler.sendEmptyMessage(MSG_END_LOAD_IMAGE);
					super.onPostExecute(result);
				}
			}.execute();
		}
		else
		{
			vp.setAdapter(null);
			mBucketId = bucketId;
			mBucketName = bucketName;
			mSelecteImage = list;
			mClickedOrgId = clickedOrgId;
			updateList();
		}

		
	}

	@Override
	public void onPageScrollStateChanged(int position)
	{
	}

	@Override
	public void onPageScrolled(int position, float arg1, int arg2)
	{
	}

	@Override
	public void onPageSelected(int position)
	{
		updateViewState(position);
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		if (id == R.id.cb_check)
		{
			Cursor cursor = mAdapter.getCursor();
			if (cursor == null)
				return;

			cursor.moveToPosition(mCurrentPosition);
			ImageItem item = ImageProvider.cursorToItem(getActivity()
			        .getContentResolver(), cursor);
			CheckBox cb = (CheckBox) v;
			item.setChecked(!cb.isChecked());
			boolean isInserted = mOnViewerClickListener.onViewerCheckClick(
			        item, mCurrentPosition);
			cb.setChecked(isInserted);
			item.setChecked(isInserted);
		}
		else if (id == R.id.btn_edit)
		{
			if (mOnViewerClickListener != null)
			{
				Cursor cursor = mAdapter.getCursor();
				cursor.moveToPosition(mCurrentPosition);
				ImageItem item = ImageProvider.cursorToItem(getActivity()
				        .getContentResolver(), cursor);
				String path = ImageProvider.getImagePath(getActivity()
				        .getContentResolver(), item.getOrgId() + "");
				mOnViewerClickListener.onViewerEditClick(path);
			}
		}

	}

	public interface onViewerClickListener
	{
		void onViewerEditClick(String path);

		boolean onViewerCheckClick(ImageItem imageItem, int position);
	}

	public void showProgress()
	{
		vp.setVisibility(View.GONE);
		mProgressbar.setVisibility(View.VISIBLE);
	}

	public void hideProgress()
	{
		vp.setVisibility(View.VISIBLE);
		mProgressbar.setVisibility(View.GONE);
	}

	private ProgressHandler mHandler = new ProgressHandler(this);

	private final static int MSG_START_LOAD_IMAGE = 101;
	private final static int MSG_END_LOAD_IMAGE = 102;

	private static class ProgressHandler extends
	        WeakReferenceHandler<ImageViewerFragment>
	{

		public ProgressHandler(ImageViewerFragment reference)
		{
			super(reference);
		}

		@Override
		protected void handleMessage(ImageViewerFragment fragment, Message msg)
		{
			switch (msg.what)
			{
				case MSG_START_LOAD_IMAGE:
				{
					fragment.showProgress();
					break;
				}
				case MSG_END_LOAD_IMAGE:
				{
					fragment.setCurrentItem();
					fragment.hideProgress();

					break;
				}
			}
		}

	}

}
