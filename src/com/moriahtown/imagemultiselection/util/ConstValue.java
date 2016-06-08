/**
 * @author owner
 * @date 2012. 7. 17.오후 2:59:20
 */
package com.moriahtown.imagemultiselection.util;

import java.io.File;

import android.os.Environment;

/**
 * @author owner
 * 
 */
public class ConstValue {

	public static final int BITMAP_THRESHOLD = 1024 *1024;

	public final static String getExternalImagePath() {
		File uploadDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.maekpoong.imageeditor/.upload/");
		if (!uploadDir.exists())
			uploadDir.mkdir();

		return uploadDir.getAbsolutePath();
	}

}
