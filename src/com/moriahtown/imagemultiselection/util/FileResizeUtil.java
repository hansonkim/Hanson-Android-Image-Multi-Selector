package com.moriahtown.imagemultiselection.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

public class FileResizeUtil {

	private static int ruleWidth = 640;
	private static int ruleHeight = 960;

	public synchronized static int getPhotoOrientationDegree(String filepath) {
		int degree = 0;
		ExifInterface exif = null;

		try {
			exif = new ExifInterface(filepath);
		} catch (IOException e) {
			// Log.d(PhotoUtil.class.getSimpleName(), "Error: "+e.getMessage());
		}

		if (exif != null) {
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);

			if (orientation != -1) {
				switch (orientation) {
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
		// Log.d(PhotoUtil.class.getSimpleName(), "Photo Degree: "+degree);
		return degree;
	}

	/**
	 * 이미지를 특정 각도로 회전
	 * 
	 * @param bitmap
	 * @param degrees
	 * @return
	 */
	public synchronized static Bitmap getRotatedBitmap(Bitmap bitmap, int degrees) {
		if (degrees != 0 && bitmap != null) {
			Matrix m = new Matrix();
			m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
			try {
				Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
				if (bitmap != b2) {
					bitmap.recycle();
					bitmap = b2;
				}
			} catch (OutOfMemoryError e) {
				// Log.d(PhotoUtil.class.getSimpleName(),
				// "Error: "+e.getMessage());
			}
		}

		return bitmap;
	}

	public synchronized static Bitmap getRotatedBitmapthumbnail(Bitmap bitmap, int degrees) {
		int height = bitmap.getHeight();
		int width = bitmap.getWidth();
		// Toast.makeText(this, width + " , " + height,
		// Toast.LENGTH_SHORT).show();
		Log.i("GGtt", "!!");
		while (height > 118) {
			bitmap = Bitmap.createScaledBitmap(bitmap, (width * 118) / height, 118, true);
			height = bitmap.getHeight();
			Log.i("GGtt", "22");
			width = bitmap.getWidth();
		}
		if (degrees != 0 && bitmap != null) {
			Log.i("GGtt", "33");
			Matrix m = new Matrix();
			m.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
			try {
				Log.i("GGtt", "44");
				String resizedFilePath = ConstValue.getExternalImagePath() + "/thumbnailImage.jpg";
				Bitmap b2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);

				try {
					Log.i("GGtt", "55");
					FileOutputStream out = new FileOutputStream(resizedFilePath, false);

					b2.compress(CompressFormat.JPEG, 100, out);
					// out.close();

				} catch (FileNotFoundException e) {
					e.printStackTrace();
					Log.i("GGtt", "!66");
				}

				if (bitmap != b2) {
					bitmap.recycle();
					bitmap = b2;
				}
			} catch (OutOfMemoryError e) {
				Log.i("GGtt", "88");
				// Log.d(PhotoUtil.class.getSimpleName(),
				// "Error: "+e.getMessage());
			}
		} else {
			if (bitmap == null) {
				Log.i("GGtt", "22xxx");
			}

			String resizedFilePath = ConstValue.getExternalImagePath() + "/thumbnailImage.jpg";
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(resizedFilePath, false);
			} catch (FileNotFoundException e) {

				e.printStackTrace();
			}

			bitmap.compress(CompressFormat.JPEG, 100, out);
		}

		return bitmap;
	}

	public static String saveResizedImage(String filePath) {

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = getSampleSizeAdjustToThreshold(getImageSize(filePath));
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

		//int degree = getPhotoOrientationDegree(filePath);

		int currentWidth = bitmap.getWidth();
		int currentHeight = bitmap.getHeight();
		int[] orgSizeArray = getImageSize(filePath);
		int[] resizedArray = getResizedSizeArrayByArea(currentWidth, currentHeight, ruleWidth, ruleHeight);

		String resizedFilePath = ConstValue.getExternalImagePath() + "/resizedImage.jpg";

		Bitmap resized = null;
		if (!orgSizeArray.equals(resizedArray)) {
			resized = Bitmap.createScaledBitmap(bitmap, resizedArray[0], resizedArray[1], true);
			//resized = getRotatedBitmap(resized, degree);

			try {

				FileOutputStream out = new FileOutputStream(resizedFilePath);

				resized.compress(CompressFormat.JPEG, 100, out);
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				
				if (resized != null) {
					resized.recycle();
					resized = null;
				}
				if (bitmap != null) {
					bitmap.recycle();
					bitmap = null;
				}
			}
		}

		return resizedFilePath;

	}

	public static String saveResizedImage(String filePath, int width, int height) {
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = getSampleSizeAdjustToThreshold(getImageSize(filePath));
		Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
		
		//int degree = getPhotoOrientationDegree(filePath);
		
		int currentWidth = bitmap.getWidth();
		int currentHeight = bitmap.getHeight();
		int[] orgSizeArray = getImageSize(filePath);
		int[] resizedArray = getResizedSizeArrayByArea(currentWidth, currentHeight, width, height);
		
		String resizedFilePath = ConstValue.getExternalImagePath() + "/resizedImage.jpg";
		
		Bitmap resized = null;
		if (!orgSizeArray.equals(resizedArray)) {
			resized = Bitmap.createScaledBitmap(bitmap, resizedArray[0], resizedArray[1], true);
			//resized = getRotatedBitmap(resized, degree);
			
			try {
				
				FileOutputStream out = new FileOutputStream(resizedFilePath);
				
				resized.compress(CompressFormat.JPEG, 100, out);
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				
				if (resized != null) {
					resized.recycle();
					resized = null;
				}
				if (bitmap != null) {
					bitmap.recycle();
					bitmap = null;
				}
			}
		}
		
		return resizedFilePath;
		
	}

	/**
	 * 면적으로 계산하여 줄인 길이를 반환한다.
	 * 
	 * @param currentWidth
	 * @param currentHeight
	 * @param ruleWidth
	 * @param ruleHeight
	 * @return
	 */
	public static int[] getResizedSizeArrayByArea(int currentWidth, int currentHeight, int ruleWidth, int ruleHeight) {

		int[] resizedArray = new int[2];
		double currentArea, ruleArea;
		currentArea = currentWidth * currentHeight;
		ruleArea = ruleWidth * ruleHeight;
		double shrinkRatio = Math.sqrt(ruleArea / currentArea);

		if (currentArea <= ruleArea) {
			Log.i("shrinkRatio", "shrinkRatio");
			resizedArray[0] = currentWidth;
			resizedArray[1] = currentHeight;
		} else {

			Log.i("shrinkRatio", "shrinkRatio");
			resizedArray[0] = (int) ((double) shrinkRatio * (double) currentWidth);
			resizedArray[1] = (int) ((double) shrinkRatio * (double) currentHeight);
		}

		return resizedArray;
	}

	/**
	 * ruleWidth, ruleHeigth 틀에 맞는 길이를 반환한다.
	 * 
	 * @param currentWidth
	 * @param currentHeight
	 * @param ruleWidth
	 * @param ruleHeight
	 * @return
	 */
	public static int[] getResizedSizeArrayByLength(int currentWidth, int currentHeight, int ruleWidth, int ruleHeight) {

		int[] resizedArray = new int[2];

		if (currentWidth <= ruleWidth && currentHeight <= ruleHeight) {
			resizedArray[0] = currentWidth;
			resizedArray[1] = currentHeight;

		} else {
			double currentRatio = (double) currentWidth / (double) currentHeight;
			double ruleRatio = (double) ruleWidth / (double) ruleHeight;

			if (currentRatio >= ruleRatio) {
				resizedArray[0] = ruleWidth;
				resizedArray[1] = (int) ((double) ruleWidth / currentRatio);

			} else {
				resizedArray[0] = (int) (currentRatio * (double) ruleHeight);
				resizedArray[1] = ruleHeight;
			}
		}

		return resizedArray;
	}

	/**
	 * 메모리 모자란다고 안할때까지 줄여서 비트맵 생성
	 * 
	 * @param filePath
	 * @param sampleSize
	 * @return
	 */
	public static Bitmap getCompressedBitmap(String filePath, int sampleSize) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = sampleSize;
		try {
			return BitmapFactory.decodeFile(filePath, options);
		} catch (OutOfMemoryError e) {
			Log.e("talktab.error", "OutOfMemoryError");
			options.inSampleSize = sampleSize * 2;
			return BitmapFactory.decodeFile(filePath, options);
		}
	}

	public static Bitmap getCompressedBitmap(InputStream is, int sampleSize) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = sampleSize;
		try {
			return BitmapFactory.decodeStream(is, null, options);
		} catch (OutOfMemoryError e) {
			Log.e("talktab.error", "OutOfMemoryError");
			options.inSampleSize = sampleSize * 2;	
			return BitmapFactory.decodeStream(is, null, options);
		}
	}
	
	/**
	 * ConstValue.BITMAP_THREHOLD 의 크기를 넘지않는 sampleSize를 반환합니다.
	 * @param imageSize
	 * @return
	 */
	public static int getSampleSizeAdjustToThreshold(int[] imageSize) {
		int sampleSize = 1;
		int bitmapSize = imageSize[0] * imageSize[1] * 4;
		int curSize = bitmapSize;
		while(curSize >= ConstValue.BITMAP_THRESHOLD) {
			sampleSize++;
			curSize =  bitmapSize / (sampleSize*sampleSize);
		}
		return sampleSize;
	}
	
	/**
	 * ConstValue.BITMAP_THREHOLD 의 크기를 넘지않는 비트맵을 반환한다.
	 * @param filePath
	 * @return
	 */
	public static Bitmap getAdjustToThresholdBitmap(String filePath) {
		int[] size = getImageSize(filePath);
		
 		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = getSampleSizeAdjustToThreshold(size);
		return BitmapFactory.decodeFile(filePath, options);
	}

	/**
	 * ConstValue.BITMAP_THREHOLD 의 크기를 넘지않는 비트맵을 반환한다.
	 * @param filePath
	 * @param size
	 * @return
	 */
	public static Bitmap getAdjustToThresholdBitmap(String filePath, int[] size) {
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = getSampleSizeAdjustToThreshold(size);
		return BitmapFactory.decodeFile(filePath, options);
	}
	
	
	/**
	 * 화면 사이즈에 맞는 비트맵을 반환한다.
	 * @param filePath
	 * @param screenSize
	 * @return
	 */
	public static Bitmap getScreenSizeBitmap(String filePath, int[] screenSize) {
		BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds=false;
        options.inSampleSize = getSampleSizeAdjustToScreen(filePath, screenSize);
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        return bitmap;
	}
	
	/**
	 * 화면 사이즈에 ㅄ?sampleSize를 반환합니다.
	 * @param filePath
	 * @param screenSize
	 * @return
	 */
	public static int getSampleSizeAdjustToScreen(String filePath, int[] screenSize) {
		BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        int w=(int) Math.ceil(options.outWidth/(float)screenSize[0]);
        int h=(int) Math.ceil(options.outHeight/(float)screenSize[1]);

        if(h>1 || w>1){
            if(h>w){
                options.inSampleSize=h;

            }else{
                options.inSampleSize=w;
            }
        }
        return options.inSampleSize;
	}
	
	
	

	/**
	 * 특정 디바이스에서는 동일 context 내에서 중복 호출시 inputstream을 close 못 하는 경우가 발생합니다.
	 * 연속된 사용을 자제해야하고 반환된 값은 저장하여 다시 씁시다.
	 * @param filePath
	 * @return
	 */
	public static int[] getImageSize(String filePath) {
		int[] size = new int[2];

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(new File(filePath));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			size[0] = size[1] = -1;
			return size;
		}
		try{
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(fis, null, options);
			size[0] = options.outWidth;
			size[1] = options.outHeight;
		} catch(Exception e) {
			e.printStackTrace();
			size[0] = size[1] = -1;
			return size;
		} finally {
			try {
				if(fis != null) {
					fis.close();
					fis = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return size;
	}

}
