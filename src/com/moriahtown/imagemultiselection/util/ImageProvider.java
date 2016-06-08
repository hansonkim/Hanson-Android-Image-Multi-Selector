package com.moriahtown.imagemultiselection.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.text.TextUtils;
import android.util.Log;

import com.moriahtown.imagemultiselection.AlbumListFragment;
import com.moriahtown.imagemultiselection.dataobj.Album;
import com.moriahtown.imagemultiselection.dataobj.ImageItem;

public class ImageProvider
{

	private final static String[] albumProjection =
	{ MediaStore.Images.ImageColumns._ID,
	        MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
	        MediaStore.Images.ImageColumns.DATA,
	        MediaStore.Images.ImageColumns.DATE_TAKEN,
	        MediaStore.Images.ImageColumns.BUCKET_ID };

	private static String[] allProjection =
	{ "*" };

	private static String selection = "_id NOT NULL) GROUP BY ("
	        + MediaStore.Images.ImageColumns.BUCKET_ID;

	/**
	 * 기기내의 모든 미디어 이미지 폴더를 반환한다.
	 * 
	 * @param context
	 * @return
	 */
	public static ArrayList<Album> getAlbumList(ContentResolver cr)
	{
		ArrayList<Album> albumList = new ArrayList<Album>();

		albumList.addAll(getAlbumList(cr,
		        MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
		albumList.addAll(getAlbumList(cr,
		        MediaStore.Images.Media.INTERNAL_CONTENT_URI));

		return albumList;
	}

	/**
	 * storage uri 에서 미디어 이미지가 들어 있는 디렉토리를 찾아 Album list로 반환한다.
	 * 
	 * @param context
	 * @param storageuri
	 * @return
	 */
	public static ArrayList<Album> getAlbumList(ContentResolver cr,
	        Uri storageuri)
	{
		ArrayList<Album> list = new ArrayList<Album>();
		Cursor cursor = cr.query(storageuri, allProjection, selection, null,
				MediaStore.Images.ImageColumns.DATE_MODIFIED + " desc");

		if (cursor != null)
		{
			for (int j = 0; j < cursor.getColumnCount(); j++)
			{
				Log.d(AlbumListFragment.class.getSimpleName(), "column name : "
				        + cursor.getColumnName(j));
			}

			int[] columnIndex = new int[albumProjection.length];

			for (int i = 0; i < albumProjection.length; i++)
			{
				columnIndex[i] = cursor.getColumnIndex(albumProjection[i]);
			}

			while (cursor.moveToNext())
			{
				Album album = new Album();
				album.setId(cursor.getLong(columnIndex[0]));
				album.setBucketDisplayName(cursor.getString(columnIndex[1]));

				// get thumb uri
				Cursor c = MediaStore.Images.Thumbnails.queryMiniThumbnail(cr,
				        album.getId(), MediaStore.Images.Thumbnails.MINI_KIND,
				        allProjection);

				if (c != null && c.moveToNext())
				{
					String uriString = c.getString(c
					        .getColumnIndex(MediaStore.Images.Thumbnails.DATA));
					album.setData(uriString);
				}
				else
				{
					album.setData(cursor.getString(columnIndex[2]));
				}

				album.setDateTaken(cursor.getLong(columnIndex[3]));
				album.setBucketId(cursor.getString(columnIndex[4]));

				c = getImagesInBucket(cr, storageuri, album.getBucketId());
				if (c != null)
				{
					album.setCount(c.getCount());
				}

				list.add(album);

				if (c != null)
					c.close();
			}
		}

		if (cursor != null)
			cursor.close();

		return list;
	}

	public static Cursor getImagesCursorInBucket(ContentResolver cr,
	        String bucketId)
	{
		Cursor c = null;

		c = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
		        allProjection, MediaStore.Images.ImageColumns.BUCKET_ID + "="
		                + bucketId, null,
		        MediaStore.Images.ImageColumns.DATE_MODIFIED + " desc");
		if (c != null && c.getCount() > 0)
		{
			return c;
		}
		else
		{
			c = cr.query(MediaStore.Images.Media.INTERNAL_CONTENT_URI,
			        allProjection, MediaStore.Images.ImageColumns.BUCKET_ID
			                + "=" + bucketId, null,
			        MediaStore.Images.ImageColumns.DATE_MODIFIED + " desc");
		}

		return c;
	}

	/**
	 * 해당 bucket_id에 들어 있는 이미지를 찾아서 cursor를 반환
	 * 
	 * @param context
	 * @param uri
	 * @param bucketId
	 * @return
	 */
	public static Cursor getImagesInBucket(ContentResolver cr, Uri uri,
	        String bucketId)
	{
		Cursor c = cr.query(uri, allProjection,
		        MediaStore.Images.ImageColumns.BUCKET_ID + "=" + bucketId,
		        null, MediaStore.Images.ImageColumns.DATE_MODIFIED + " desc");

		return c;
	}

	public static ArrayList<ImageItem> getImagesInBucket(ContentResolver cr,
	        String bucketId)
	{
		ArrayList<ImageItem> list = new ArrayList<ImageItem>();

		list.addAll(getImageItem(cr,
		        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, bucketId));
		list.addAll(getImageItem(cr,
		        MediaStore.Images.Media.INTERNAL_CONTENT_URI, bucketId));

		return list;
	}

	private static Object mFileLock = new Object();

	private static ArrayList<ImageItem> checkFileExsits(ContentResolver cr,
	        ArrayList<ImageItem> list)
	{
		synchronized (mFileLock)
		{
			for (ImageItem item : list)
			{
				String path = ImageProvider.getImagePath(cr, item.getOrgId()
				        + "");
				File file = new File(path);
				if (!file.exists())
				{
					deleteImage(cr, item.getOrgId() + "");
					list.remove(item);
				}
			}
		}

		return list;
	}

	public static void deleteImage(ContentResolver cr, String orgId)
	{
		String where = ImageColumns._ID + "=" + orgId;
		cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null);
		cr.delete(MediaStore.Images.Media.INTERNAL_CONTENT_URI, where, null);
	}

	public static ArrayList<ImageItem> getImageItem(ContentResolver cr,
	        Uri uri, String bucketId)
	{
		ArrayList<ImageItem> list = new ArrayList<ImageItem>();

		Cursor cursor = getImagesInBucket(cr, uri, bucketId);
		if (cursor != null)
		{
			while (cursor.moveToNext())
			{
				ImageItem item = new ImageItem();

				long orgId = cursor.getLong(cursor
				        .getColumnIndex(MediaStore.Images.ImageColumns._ID));
				item.setOrgId(orgId);

				String orgUriString = cursor.getString(cursor
				        .getColumnIndex(MediaStore.Images.ImageColumns.DATA));
				item.setOrgUri(Uri.parse(orgUriString));
				String thumbUriString = getThumbUriString(cr, orgId);
				if (!TextUtils.isEmpty(thumbUriString))
					item.setThumbUri(Uri.parse(thumbUriString));
				else
					item.setThumbUri(null);

				list.add(item);
			}

			cursor.close();
		}

		return list;
	}

	public static String getThumbUriString(ContentResolver cr, long orgId)
	{
		String uriString = null;

		Cursor c = MediaStore.Images.Thumbnails.queryMiniThumbnail(cr, orgId,
		        MediaStore.Images.Thumbnails.MINI_KIND, allProjection);

		if (c != null && c.moveToNext())
		{
			uriString = c.getString(c
			        .getColumnIndex(MediaStore.Images.Thumbnails.DATA));

			c.close();
		}

		return uriString;
	}

	public static long getImageOrgId(ContentResolver cr, Uri uri)
	{
		Cursor cursor = cr.query(uri, allProjection, null, null, null);

		if (cursor != null)
		{
			cursor.moveToNext();
			long orgId = cursor.getLong(cursor
			        .getColumnIndex(MediaStore.Images.ImageColumns._ID));

			cursor.close();
			return orgId;
		}
		else
		{
			return -1;
		}
	}

	public static String getImagePath(ContentResolver cr, String id)
	{
		String uriString;
		Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
		        allProjection, MediaStore.Images.ImageColumns._ID + "=" + id,
		        null, null);

		if (cursor != null)
		{
			cursor.moveToNext();
			uriString = cursor.getString(cursor
			        .getColumnIndex(MediaStore.Images.ImageColumns.DATA));

			cursor.close();
			return uriString;
		}

		cursor = cr.query(MediaStore.Images.Media.INTERNAL_CONTENT_URI,
		        allProjection, MediaStore.Images.ImageColumns._ID + "=" + id,
		        null, null);

		if (cursor != null)
		{
			cursor.moveToNext();
			uriString = cursor.getString(cursor
			        .getColumnIndex(MediaStore.Images.ImageColumns.DATA));

			cursor.close();
			return uriString;
		}

		return null;
	}

	public static void updateRotation(ContentResolver cr, Uri uri)
	{
		String fileName;
		Cursor cursor = cr.query(uri, allProjection, null, null, null);
		if (cursor != null)
		{
			cursor.moveToFirst();
			fileName = cursor.getString(cursor
			        .getColumnIndex(MediaStore.Images.ImageColumns.DATA));
			try
			{
				ExifInterface exif = new ExifInterface(fileName);
				int orientation = exif.getAttributeInt(
				        ExifInterface.TAG_ORIENTATION, -1);
				int degree = 0;
				if (orientation != -1)
				{
					switch (orientation)
					{
						case ExifInterface.ORIENTATION_ROTATE_90:
							degree = 90;
							break;

						case ExifInterface.ORIENTATION_ROTATE_180:
							degree = 180;
							break;

						case ExifInterface.ORIENTATION_ROTATE_270:
							degree = 270;
							break;
					}

				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

	}

	public static long insertImage(ContentResolver cr, String path)
	{
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.TITLE, path);
		values.put(MediaStore.Images.Media.DATA, path);
		values.put(MediaStore.Images.Media.DESCRIPTION,
		        "capture with holyfence image editor");
		Uri uri = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
		        values);

		long id = ContentUris.parseId(uri);
		// Wait until MINI_KIND thumbnail is generated.
		Bitmap miniThumb = MediaStore.Images.Thumbnails.getThumbnail(cr, id,
		        MediaStore.Images.Thumbnails.MINI_KIND, null);

		return id;

	}

	public String getRealPathFromURI(ContentResolver cr, Uri contentUri)
	{
		String[] proj =
		{ MediaStore.Images.Media.DATA };
		Cursor cursor = cr.query(contentUri, proj, null, null, null);
		int column_index = cursor
		        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);

	}

	public static ImageItem cursorToItem(ContentResolver cr, Cursor cursor)
	{
		final ImageItem item = new ImageItem();

		int idx = cursor
		        .getColumnIndex(MediaStore.Images.ImageColumns._ID);
		long orgId = cursor.getLong(idx);

		item.setOrgId(orgId);
		String orgUriString = cursor.getString(cursor
		        .getColumnIndex(MediaStore.Images.ImageColumns.DATA));
		item.setOrgUri(Uri.parse(orgUriString));

		String thumbUriString = getThumbUriString(cr, orgId);
		if (!TextUtils.isEmpty(thumbUriString))
		{
			item.setThumbUri(Uri.parse(thumbUriString));
		}
		else
		{
			item.setThumbUri(null);
		}

		return item;
	}
}
