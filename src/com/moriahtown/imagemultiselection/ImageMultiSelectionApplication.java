package com.moriahtown.imagemultiselection;

import android.app.Application;

import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

public class ImageMultiSelectionApplication extends Application
{
	public static final int[] STORY_UPLOAD_SIZE =
	{ 1280, 1280 };
	public static final int[] STORY_THUMBNAIL_SIZE =
	{ 400, 400 };
	public static final int[] WRITE_STORY_THUMBNAIL_SIZE =
	{ 100, 100 };
	public static final int LRU_MEM_CACHE_SIZE = 3 * 1024 * 1024;
	public static final int MEM_CACHE_SIZE = 3 * 1024 * 1024;
	public static final int DISK_CACHE_SIZE = 50 * 1024 * 1024;
	public static final int DISK_CACH_FILE_COUNT = 300;

	@Override
	public void onCreate()
	{
		super.onCreate();

		/*
		 * Setting for ImageLoader
		 */
		DisplayImageOptions options = new DisplayImageOptions.Builder()
		        .cacheInMemory(true)
		        .cacheOnDisk(false)
		        .displayer(new SimpleBitmapDisplayer()).build();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
		        this)
		        .memoryCacheExtraOptions(STORY_UPLOAD_SIZE[0],
		                STORY_UPLOAD_SIZE[1])
		        .threadPoolSize(3)
		        .threadPriority(Thread.NORM_PRIORITY - 1)
		        .tasksProcessingOrder(QueueProcessingType.FIFO)
		        .denyCacheImageMultipleSizesInMemory().memoryCache(
		                new LruMemoryCache(LRU_MEM_CACHE_SIZE))
		        .memoryCacheSizePercentage(13)
		        // default
		        .imageDownloader(new BaseImageDownloader(this)) // default
		        .imageDecoder(new BaseImageDecoder(true)) // default
		        // .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
		        // // default
		        .defaultDisplayImageOptions(options).writeDebugLogs().build();

		ImageLoader.getInstance().init(config);

	}
}
