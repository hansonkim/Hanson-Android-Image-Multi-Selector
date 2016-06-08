/**
 * @author owner
 * @date 2012. 7. 23.?占쏀 5:56:41
 */
package com.moriahtown.imagemultiselection.util;


/**
 * @author owner
 *
 */
public class ImageEditorException extends Exception {

	private static final long serialVersionUID = -3862448838315777643L;

	public ImageEditorException(String msg) {
		super(msg);
	}
	
	public ImageEditorException(String msg, Throwable rootCause)
    {
        super(msg, rootCause);
    }
}
