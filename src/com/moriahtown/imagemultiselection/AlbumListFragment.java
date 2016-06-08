package com.moriahtown.imagemultiselection;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.moriahtown.imagemultiselection.dataobj.Album;
import com.moriahtown.imagemultiselection.util.ImageProvider;
import com.moriahtown.imagemultiselection.util.WeakReferenceHandler;

public class AlbumListFragment extends Fragment implements OnItemClickListener
{
	public final static String TAG = "AlbumListFragment";

	private OnAlbumClickListener mOnAlbumClickListener;

	private ListView mListView;
	private ArrayList<Album> mList;
	private ProgressBar mProgressbar;

	public static AlbumListFragment newInstance(
	        OnAlbumClickListener onAlbumClickListener)
	{
		AlbumListFragment fragment = new AlbumListFragment();
		fragment.setOnAlbumClickListener(onAlbumClickListener);
		return fragment;

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
		View view = initView(inflater);

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
		View view = inflater.inflate(R.layout.fragment_album_list, null);
		mListView = (ListView) view.findViewById(R.id.lv_folder);
		mProgressbar = (ProgressBar) view.findViewById(R.id.progressbar);

		return view;
	}

	public void loadImages(ArrayList<Album> list)
	{
		if (list == null)
			return;

		mList = list;
		mListView.setAdapter(new AlbumListAdapter(getActivity(), mList));
		mListView.setOnItemClickListener(this);
	}

	public void updateList()
	{
		new AsyncTask<String, Long, ArrayList<Album>>()
		{

			@Override
			protected void onPreExecute()
			{
				mHandler.sendEmptyMessage(MSG_START_LOAD_IMAGE);
				super.onPreExecute();
			}

			@Override
			protected ArrayList<Album> doInBackground(String... params)
			{
				if (getActivity() != null)
					return ImageProvider.getAlbumList(getActivity()
					        .getContentResolver());
				else
					return null;
			}

			@Override
			protected void onPostExecute(ArrayList<Album> result)
			{
				Message msg = new Message();
				msg.what = MSG_END_LOAD_IMAGE;
				msg.obj = result;
				mHandler.sendMessage(msg);
				super.onPostExecute(result);
			}

		}.execute();
	}

	public void showProgress()
	{
		mProgressbar.setVisibility(View.VISIBLE);
	}

	public void hideProgress()
	{
		mProgressbar.setVisibility(View.GONE);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
	        long id)
	{

		AlbumListAdapter adapter = (AlbumListAdapter) parent.getAdapter();
		Album album = adapter.getItem(position);

		if (mOnAlbumClickListener != null)
			mOnAlbumClickListener.onAlbumClick(album, position);
	}

	public void setOnAlbumClickListener(
	        OnAlbumClickListener mOnAlbumClickListener)
	{
		this.mOnAlbumClickListener = mOnAlbumClickListener;
	}

	public interface OnAlbumClickListener
	{
		void onAlbumClick(Album album, int position);
	}

	private ProgressHandler mHandler = new ProgressHandler(this);

	private final static int MSG_START_LOAD_IMAGE = 101;
	private final static int MSG_END_LOAD_IMAGE = 102;

	private static class ProgressHandler extends
	        WeakReferenceHandler<AlbumListFragment>
	{

		public ProgressHandler(AlbumListFragment reference)
		{
			super(reference);
		}

		@Override
		protected void handleMessage(AlbumListFragment fragment, Message msg)
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
					fragment.loadImages((ArrayList<Album>) msg.obj);
					fragment.hideProgress();
					break;
				}
			}
		}

	}
}
