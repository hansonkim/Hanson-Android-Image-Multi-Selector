package com.moriahtown.imagemultiselection;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.moriahtown.imagemultiselection.AlbumListFragment.OnAlbumClickListener;
import com.moriahtown.imagemultiselection.ImageCursorAdapter.OnListClickListener;
import com.moriahtown.imagemultiselection.ImageViewerFragment.onViewerClickListener;
import com.moriahtown.imagemultiselection.SelectedImageListAdapter.OnDeleteClickListener;
import com.moriahtown.imagemultiselection.dataobj.Album;
import com.moriahtown.imagemultiselection.dataobj.ImageItem;
import com.moriahtown.imagemultiselection.util.ImageProvider;

public class SelectActivity extends FragmentActivity implements
        OnAlbumClickListener, OnListClickListener, OnDeleteClickListener,
        onViewerClickListener
{
	public final static String ACTION_NAME = "com.moriahtown.imagemultiselection";
	
	private final static String TAG = "SelectActivity";

	public final static String KEY_LIMIT_COUNT = "key_limit_count";
	public final static String KEY_SELECTED_IMAGES_IDS = "key_selected_images_ids";
	private final static String KEY_IMAGE_CAPTURE_URI = "key_image_capture_uri";

	public final static String KEY_SELECTED_IMAGES_PATHS = "key_selected_images_uris";

	FrameLayout flContainer;
	GridView mGrid;
	SelectedImageListAdapter mSelecteImageListAdapter;

	private AlbumListFragment mAlbumListFragment;
	private ImageListFragment mImageListFragment;
	private ImageViewerFragment mImageViewerFragment;

	private TextView tvSelectCount;
	private TextView tvLimitCount;
	private Button btnAttach;

	private ArrayList<String> mSelectedImageIds;
	private Uri mImageCaptureUri;
	private int limitCount;

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		Log.d(TAG, "onSaveInstanceState");
		if (mSelectedImageIds != null && mSelectedImageIds.size() > 0)
			outState.putStringArrayList(KEY_SELECTED_IMAGES_IDS,
			        mSelectedImageIds);
		if (mImageCaptureUri != null)
			outState.putString(KEY_IMAGE_CAPTURE_URI, mImageCaptureUri
			        .toString());

		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		Log.d(TAG, "onRestoreInstanceState");
		super.onRestoreInstanceState(savedInstanceState);
		restoreSavedInstanceState(savedInstanceState);
	}

	private void restoreSavedInstanceState(Bundle savedInstanceState)
	{
		if (savedInstanceState.containsKey(KEY_SELECTED_IMAGES_IDS))
		{
			mSelectedImageIds = savedInstanceState
			        .getStringArrayList(KEY_SELECTED_IMAGES_IDS);
		}
		if (savedInstanceState.containsKey(KEY_IMAGE_CAPTURE_URI))
		{
			mImageCaptureUri = Uri.parse(savedInstanceState
			        .getString(KEY_IMAGE_CAPTURE_URI));
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select);

		Intent bundle = getIntent();
		limitCount = bundle.getIntExtra(KEY_LIMIT_COUNT, 4);

		if (savedInstanceState != null)
			restoreSavedInstanceState(savedInstanceState);

		if (findViewById(R.id.fl_container) != null)
		{

			flContainer = (FrameLayout) findViewById(R.id.fl_container);
			mGrid = (GridView) findViewById(R.id.hs_container);

			if (mSelectedImageIds == null)
				mSelectedImageIds = new ArrayList<String>();

			mSelecteImageListAdapter = new SelectedImageListAdapter(this,
			        mSelectedImageIds, this);

			mGrid.setAdapter(mSelecteImageListAdapter);

			FragmentManager fm = getSupportFragmentManager();

			if (hasAlbumListFragment(fm))
			{
				fm.beginTransaction().show(mAlbumListFragment).commit();
			}
			else
			{
				mAlbumListFragment = AlbumListFragment.newInstance(this);
				fm.beginTransaction().add(R.id.fl_container,
				        mAlbumListFragment, AlbumListFragment.TAG).commit();
			}

			tvSelectCount = (TextView) findViewById(R.id.tv_selected_count);
			tvSelectCount.setText(0 + "");
			tvLimitCount = (TextView) findViewById(R.id.tv_limit_count);
			tvLimitCount.setText(limitCount + "");
			btnAttach = (Button) findViewById(R.id.btn_attach);
			btnAttach.setVisibility(View.GONE);

		}

	}

	@Override
	protected void onStart()
	{
		Log.d(TAG, "onStart");
		super.onStart();
	}

	@Override
	protected void onResume()
	{
		Log.d(TAG, "onResume");
		super.onResume();
	}

	private void addImage(final String orgId)
	{
		if (mSelecteImageListAdapter == null)
			Log.d(TAG + ".addImage", "mSelecteImageListAdapter is null");

		if (mSelectedImageIds == null)
			Log.d(TAG + ".addImage", "mSelectedImageIds is null");

		if (tvSelectCount == null)
			Log.d(TAG + ".addImage", "tvSelectCount is null");

		mSelectedImageIds.add(0, orgId);
		mSelecteImageListAdapter.notifyDataSetChanged();
		tvSelectCount.setText(mSelectedImageIds.size() + "");
		
		btnAttach.setVisibility(View.VISIBLE);
	}

	private void removeImage(String orgId)
	{
		mSelecteImageListAdapter.remove(orgId);
		mSelecteImageListAdapter.notifyDataSetChanged();
		tvSelectCount.setText(mSelectedImageIds.size() + "");

		if (mSelectedImageIds == null || mSelectedImageIds.size() == 0)
		{
			hideSelectedImageContainer();
			btnAttach.setVisibility(View.GONE);
		}
		else
		{
			btnAttach.setVisibility(View.VISIBLE);
		}
	}

	private void startImageEditor(String path, int requestCode)
	{
		Intent intent = new Intent(this, ImageEditorActivity.class);
		intent.putExtra(ImageEditorActivity.KEY_IMAGE_PATH, path);
		startActivityForResult(intent, requestCode);
	}

	private void toggleSelectedImageContainer()
	{
		if (mGrid.getVisibility() == View.VISIBLE)
		{
			hideSelectedImageContainer();
		}
		else
		{
			showSelectedImageContainer();
		}
	}

	private void hideSelectedImageContainer()
	{
		//mGrid.startAnimation(AnimationUtils.loadAnimation(this,R.anim.slide_down));
		mGrid.setVisibility(View.GONE);
	}

	private void showSelectedImageContainer()
	{
		//mGrid.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up));
		mGrid.setVisibility(View.VISIBLE);
	}

	private final static int REQUEST_CODE_CAPTURE = 901;
	private final static int REQUEST_CODE_EDITOR_FROM_CAPTURE = 902;
	private final static int REQUEST_CODE_EDITOR_FROM_SELECT = 903;

	@Override
	protected void
	        onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.d(TAG, "onActivityResult");

		if (resultCode != Activity.RESULT_OK)
		{
			return;
		}
		switch (requestCode)
		{
			case REQUEST_CODE_CAPTURE:
			{
				if(resultCode == RESULT_OK)
				{
					long id = ContentUris.parseId(mImageCaptureUri);
					ContentResolver cr = getContentResolver();
					// Wait until MINI_KIND thumbnail is generated.
					Bitmap miniThumb = MediaStore.Images.Thumbnails.getThumbnail(
							cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
					
					long orgId = ImageProvider.getImageOrgId(getContentResolver(),
							mImageCaptureUri);
					if (orgId > 0)
					{
						startImageEditor(ImageProvider.getImagePath(cr, String
								.valueOf(orgId)), REQUEST_CODE_EDITOR_FROM_CAPTURE);
					}
					else
					{
						Toast.makeText(this, "사진을 가져오는데 실패하였습니다.",
								Toast.LENGTH_SHORT).show();
					}
				}
				break;
			}
			case REQUEST_CODE_EDITOR_FROM_CAPTURE:
			{
				if(resultCode == RESULT_OK)
				{
					String path = data
							.getStringExtra(ImageEditorActivity.KEY_IMAGE_PATH);
					long id = ImageProvider.insertImage(getContentResolver(), path);
					addImage(String.valueOf(id));
					showSelectedImageContainer();
					
					FragmentManager fm = getSupportFragmentManager();
					if (hasAlbumListFragment(fm))
					{
						mAlbumListFragment.updateList();
					}
				}
				break;

			}
			case REQUEST_CODE_EDITOR_FROM_SELECT:
			{
				if(resultCode == RESULT_OK)
				{
					String path = data
							.getStringExtra(ImageEditorActivity.KEY_IMAGE_PATH);
					long id = ImageProvider.insertImage(getContentResolver(), path);
					//addImage(String.valueOf(id));
					
					// ImageViewerFragment가 있는 경우
					FragmentManager fm = getSupportFragmentManager();
					
					if (hasImageViewerFragment(fm))
					{
						mImageViewerFragment.setClickedId(id);
						mImageViewerFragment.updateList();
					}
					
					if (hasAlbumListFragment(getSupportFragmentManager()))
					{
						mAlbumListFragment.updateList();
					}
					
					
					if (hasImageListFragment(fm))
					{
						mImageListFragment.updateList();
					}
				}

				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onBackPressed()
	{
		Log.d(TAG, "onBackPressed");

		FragmentManager fm = getSupportFragmentManager();

		if (hasImageViewerFragment(fm) && !mImageViewerFragment.isHidden())
		{
			// fm.beginTransaction().remove(mImageViewerFragment).commit();
			fm.beginTransaction().hide(mImageViewerFragment).commit();

			showSelectedImageContainer();

			return;
		}

		if (hasImageListFragment(fm) && !mImageListFragment.isHidden())
		{
			// fm.beginTransaction().remove(mImageListFragment).commit();
			fm.beginTransaction().hide(mImageListFragment).commit();
			return;
		}

		super.onBackPressed();
	}

	public void onClickBtnCancel(View view)
	{

		FragmentManager fm = getSupportFragmentManager();

		if (hasImageViewerFragment(fm) && !mImageViewerFragment.isHidden())
		{
			// fm.beginTransaction().remove(mImageViewerFragment).commit();
			fm.beginTransaction().hide(mImageViewerFragment).commit();
			showSelectedImageContainer();

		}
		else if (hasImageListFragment(fm) && !mImageListFragment.isHidden())
		{
			// fm.beginTransaction().remove(mImageListFragment).commit();
			fm.beginTransaction().hide(mImageListFragment).commit();
		}
		else
		{
			super.onBackPressed();
		}
	}

	public void onClickBtnCamera(View view)
	{
		if (mSelectedImageIds == null)
			mSelectedImageIds = new ArrayList<String>();

		if (mSelectedImageIds.size() >= limitCount)
		{
			Toast.makeText(
			        this,
			        getResources().getString(R.string.exceed_limit_count,
			                limitCount), Toast.LENGTH_SHORT).show();
			return;
		}
		
		String fileName = System.currentTimeMillis() + ".jpg";
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, fileName);
		values.put(MediaStore.Images.Media.DESCRIPTION,
		        "capture with holyfence image multi selector");
		ContentResolver cr = getContentResolver();
		mImageCaptureUri = cr.insert(
		        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

		String uriString = ImageProvider.getImagePath(cr, ContentUris
		        .parseId(mImageCaptureUri)
		        + "");

		Intent intent = new Intent();
		intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
		startActivityForResult(intent, REQUEST_CODE_CAPTURE);

	}

	public void onClickAttach(View view)
	{
		Log.d(TAG, "onclickAttach was clicked");

		ArrayList<String> pathList = new ArrayList<String>();
		ContentResolver cr = getContentResolver();
		for (String id : mSelectedImageIds)
		{
			String uriString = ImageProvider.getImagePath(cr, id);
			if (!TextUtils.isEmpty(uriString))
				pathList.add(uriString);
		}

		Intent data = new Intent();
		data.putStringArrayListExtra(KEY_SELECTED_IMAGES_IDS, mSelectedImageIds);
		data.putStringArrayListExtra(KEY_SELECTED_IMAGES_PATHS, pathList);
		setResult(RESULT_OK, data);
		finish();
	}

	@Override
	public void onDeleteClick(String orgId, int position)
	{
		removeImage(orgId);
		if (hasImageListFragment(getSupportFragmentManager()))
		{
			mImageListFragment.uncheckImage(orgId);
		}
	}

	@Override
	public void onListImageClick(String bucketId, String bucketName,
	        ImageItem item)
	{
		hideSelectedImageContainer();

		FragmentManager fm = getSupportFragmentManager();
		if (hasImageViewerFragment(fm))
		{
			fm.beginTransaction().show(mImageViewerFragment).commit();
			mImageViewerFragment.reload(bucketId, bucketName,
			        mSelectedImageIds, item.getOrgId());

		}
		else
		{
			mImageViewerFragment = ImageViewerFragment.newInstance(bucketId,
			        bucketName, mSelectedImageIds, item.getOrgId(), this);
			mImageViewerFragment.setOnEditClickListener(this);
			fm.beginTransaction().add(R.id.fl_container, mImageViewerFragment,
			        ImageViewerFragment.TAG).commit();
		}

	}

	@Override
	public boolean onListCheckClick(ImageItem imageItem)
	{
		if (mSelectedImageIds == null)
			mSelectedImageIds = new ArrayList<String>();

		if (!imageItem.isChecked() && mSelectedImageIds.size() >= limitCount)
		{
			Toast.makeText(
			        this,
			        getResources().getString(R.string.exceed_limit_count,
			                limitCount), Toast.LENGTH_SHORT).show();
			return imageItem.isChecked();
		}
		
		boolean result = onCheckClick(imageItem);
		if (mSelectedImageIds != null && mSelectedImageIds.size() > 0)
			showSelectedImageContainer();
		else
			hideSelectedImageContainer();
		return result;
	}

	@Override
	public void onAlbumClick(Album album, int position)
	{
		FragmentManager fm = getSupportFragmentManager();
		if (hasImageListFragment(fm))
		{
			fm.beginTransaction().show(mImageListFragment).commit();
			mImageListFragment.reload(album.getBucketId(), album
			        .getBucketDisplayName(), mSelectedImageIds);
		}
		else
		{
			mImageListFragment = ImageListFragment.newInstance(album
			        .getBucketId(), album.getBucketDisplayName(),
			        mSelectedImageIds, this);
			fm.beginTransaction().add(R.id.fl_container, mImageListFragment,
			        ImageListFragment.TAG).commit();

		}

	}

	@Override
	public void onViewerEditClick(String path)
	{
		startImageEditor(path, REQUEST_CODE_EDITOR_FROM_SELECT);
	}

	@Override
	public boolean onViewerCheckClick(ImageItem imageItem, int position)
	{
		if (mSelectedImageIds == null)
			mSelectedImageIds = new ArrayList<String>();

		if (!imageItem.isChecked() && mSelectedImageIds.size() >= limitCount)
		{
			Toast.makeText(
			        this,
			        getResources().getString(R.string.exceed_limit_count,
			                limitCount), Toast.LENGTH_SHORT).show();
			return false;
		}
		
		boolean b = onCheckClick(imageItem);
		if (hasImageListFragment(getSupportFragmentManager()))
		{
			mImageListFragment.updateList();
		}
		return b;
	}

	private boolean onCheckClick(ImageItem imageItem)
	{

		String orgId = String.valueOf(imageItem.getOrgId());
		if (!mSelectedImageIds.contains(orgId))
		{
			addImage(orgId);
			return true;
		}
		else
		{
			removeImage(orgId);
			return false;
		}
	}

	private boolean hasAlbumListFragment(FragmentManager fm)
	{
		return (mAlbumListFragment = (AlbumListFragment) fm
		        .findFragmentByTag(AlbumListFragment.TAG)) != null;
	}

	private boolean hasImageListFragment(FragmentManager fm)
	{
		return (mImageListFragment = (ImageListFragment) fm
		        .findFragmentByTag(ImageListFragment.TAG)) != null;
	}

	private boolean hasImageViewerFragment(FragmentManager fm)
	{
		return (mImageViewerFragment = (ImageViewerFragment) fm
		        .findFragmentByTag(ImageViewerFragment.TAG)) != null;
	}
}
