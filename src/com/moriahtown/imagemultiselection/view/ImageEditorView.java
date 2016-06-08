package com.moriahtown.imagemultiselection.view;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.moriahtown.imagemultiselection.R;
import com.moriahtown.imagemultiselection.util.FileResizeUtil;
import com.moriahtown.imagemultiselection.util.ImageEditorException;

/**
 * @author owner
 * 
 */
public class ImageEditorView extends SurfaceView implements Callback
{

	private Context mContext;
	private Bitmap mImageBitmap;
	private Bitmap mOrgBitmap;
	private SurfaceHolder mSurfaceHolder;
	private ManageThread mThread;

	private int mViewWidth;
	private int mViewHeight;

	private float mScale = 1f;

	private boolean mSelectWidthFix = false;
	private boolean mSelectHeightFix = false;

	/**
	 * 筌ㅒ�혣 ?�좎럡由�
	 */
	private final static int[] BOUNDARY_DIMENSION =
	{ 150, 150 };
	private final static int DRWABLE_SELECTOR = R.drawable.btn_photo_size;

	private float m_lastX[] = null;
	private float m_lastY[] = null;
	private float m_curX[] = null;
	private float m_curY[] = null;

	private Rect mImageRect = new Rect();
	private Rect mSelectRect = new Rect();

	private boolean m_isFirstPointer[] =
	{ false, false };
	private boolean m_isSecondPointer[] =
	{ false, false };
	private boolean m_isMovePointer = false;

	private boolean m_isDrawSelection = true;

	private Bitmap mBmpSelector;
	private int mSelectorWidth;
	private int mSelectorHeight;

	private boolean selectorPositionReset;

	private String mFilePath;
	private String mEditPath;
	private int mDegree = 0;
	private int totalDegree = 0;

	public ImageEditorView(Context context, String filePath, String editPath)
	        throws ImageEditorException, IOException
	{
		super(context);
		mContext = context;
		mFilePath = filePath;
		mEditPath = editPath;
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		mBmpSelector = ((BitmapDrawable) mContext.getResources().getDrawable(
		        DRWABLE_SELECTOR)).getBitmap();
		mSelectorWidth = mBmpSelector.getWidth();
		mSelectorHeight = mBmpSelector.getHeight();

		mViewWidth = ImageEditorView.this.getWidth() - mSelectorWidth;
		mViewHeight = ImageEditorView.this.getHeight() - mSelectorHeight;

		// ?�좎럥���좎럩혶��?혨 筌띉욃칰혣 bitmap 餓Β��먯삕?
		/*
		 * int degree = FileResizeUtil.getPhotoOrientationDegree(mFilePath); if
		 * (degree != 0) {
		 * 
		 * mOrgBitmap = FileResizeUtil.getAdjustToThresholdBitmap(mFilePath);
		 * mOrgBitmap = FileResizeUtil.getRotatedBitmap(mOrgBitmap, degree); }
		 * else { mOrgBitmap =
		 * FileResizeUtil.getAdjustToThresholdBitmap(mFilePath); }
		 */
		mOrgBitmap = FileResizeUtil.getAdjustToThresholdBitmap(mFilePath);

		if (mOrgBitmap.getWidth() < BOUNDARY_DIMENSION[0]
		        || mOrgBitmap.getHeight() < BOUNDARY_DIMENSION[1])
		{
			m_isDrawSelection = false;
		}

		int[] resizedArray = FileResizeUtil.getResizedSizeArrayByLength(
		        mOrgBitmap.getWidth(), mOrgBitmap.getHeight(), mViewWidth,
		        mViewHeight);
		mImageBitmap = Bitmap.createScaledBitmap(mOrgBitmap, resizedArray[0],
		        resizedArray[1], true);

		if (mImageBitmap.getWidth() < BOUNDARY_DIMENSION[0])
			mSelectWidthFix = true;
		if (mImageBitmap.getHeight() < BOUNDARY_DIMENSION[1])
			mSelectHeightFix = true;

		mThread = new ManageThread(mContext, mSurfaceHolder);
		mThread.setIsRunning(true);
		selectorPositionReset = true;
		mThread.start();
	}

	public void rotateClockWise()
	{
		mDegree = 90;
		totalDegree += 90;
		selectorPositionReset = true;
	}

	public void rotateAntiClockWise()
	{
		mDegree = -90;
		totalDegree -= 90;
		selectorPositionReset = true;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int arg1, int arg2,
	        int arg3)
	{

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		boolean done = true;
		mThread.setIsRunning(false);
		if (mThread != null)
		{
			while (done)
			{
				try
				{
					mThread.join();
					done = false;
				}
				catch (InterruptedException e)
				{
					Log.e("talktab.error", e.getMessage());
				}
				finally
				{
					mThread = null;
					if (mImageBitmap != null)
					{
						mImageBitmap.recycle();
						mImageBitmap = null;
					}
					if (mOrgBitmap != null)
					{
						mOrgBitmap.recycle();
						mOrgBitmap = null;
					}
					System.gc();
				}
			}
		}

	}

	private void drawSelection(Canvas canvas)
	{

		canvas.save();
		canvas.clipRect(new Rect(mImageRect.left, mImageRect.top,
		        mImageRect.right, mImageRect.bottom));
		canvas.clipRect(mSelectRect, Region.Op.XOR);
		canvas.drawColor(0x55000000);
		canvas.restore();

		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(2.0f);
		canvas.drawRect(mSelectRect, paint);

		canvas.drawBitmap(mBmpSelector, mSelectRect.left - mSelectorWidth / 2,
		        mSelectRect.top - mSelectorHeight / 2, null);
		canvas.drawBitmap(mBmpSelector, mSelectRect.right - mSelectorWidth / 2,
		        mSelectRect.bottom - mSelectorHeight / 2, null);
	}

	/**
	 * ?�좎럩田┑�??�좎룞����? 獄쎛�혱?�좎럥혢?
	 * 
	 * @param format
	 *            The format of the compressed image
	 * @param quality
	 *            Hint to the compressor, 0-100. 0 meaning compress for small
	 *            size, 100 meaning compress for max quality. Some formats, like
	 *            PNG which is lossless, will ignore the quality setting
	 * @throws IOException
	 * @out OutputStream The outputstream to write the compressed data
	 */
	public void writeEditedImageToOutputStream(CompressFormat format,
	        int quality, OutputStream out) throws IOException
	{
		int x = (int) ((mSelectRect.left - mImageRect.left) / mScale);
		int y = (int) ((mSelectRect.top - mImageRect.top) / mScale);
		int width = (int) ((mSelectRect.right - mSelectRect.left) / mScale);
		int height = (int) ((mSelectRect.bottom - mSelectRect.top) / mScale);

		int bitmapWidth = mImageBitmap.getWidth();
		if ((totalDegree % 360) != 0)
		{
			Matrix m = new Matrix();
			m.setRotate(totalDegree % 360, (float) mOrgBitmap.getWidth() / 2,
			        (float) mOrgBitmap.getHeight() / 2);
			mOrgBitmap = Bitmap.createBitmap(mOrgBitmap, 0, 0, mOrgBitmap
			        .getWidth(), mOrgBitmap.getHeight(), m, true);
		}
		float scale = (float) mOrgBitmap.getWidth() / (float) bitmapWidth;
		x = Math.max((int) (x * scale), 0);
		y = Math.max((int) (y * scale), 0);
		width = Math.min((int) (width * scale), (mOrgBitmap.getWidth() - x));
		height = Math.min((int) (height * scale), (mOrgBitmap.getHeight() - y));
		Bitmap editedBitmap = Bitmap.createBitmap(mOrgBitmap, x, y, width,
		        height, null, true);
		editedBitmap.compress(format, quality, out);
		out.flush();
		out.close();

		if (editedBitmap != null)
		{
			editedBitmap.recycle();
			editedBitmap = null;
		}

	}

	public String writeEditedImage(CompressFormat format, int quality)
	        throws IOException
	{
		FileOutputStream out = new FileOutputStream(mEditPath);
		int x = (int) ((mSelectRect.left - mImageRect.left) / mScale);
		int y = (int) ((mSelectRect.top - mImageRect.top) / mScale);
		int width = (int) ((mSelectRect.right - mSelectRect.left) / mScale);
		int height = (int) ((mSelectRect.bottom - mSelectRect.top) / mScale);

		int bitmapWidth = mImageBitmap.getWidth();
		if ((totalDegree % 360) != 0)
		{
			Matrix m = new Matrix();
			m.setRotate(totalDegree % 360, (float) mOrgBitmap.getWidth() / 2,
			        (float) mOrgBitmap.getHeight() / 2);
			mOrgBitmap = Bitmap.createBitmap(mOrgBitmap, 0, 0, mOrgBitmap
			        .getWidth(), mOrgBitmap.getHeight(), m, true);
		}
		float scale = (float) mOrgBitmap.getWidth() / (float) bitmapWidth;
		x = Math.max((int) (x * scale), 0);
		y = Math.max((int) (y * scale), 0);
		width = Math.min((int) (width * scale), (mOrgBitmap.getWidth() - x));
		height = Math.min((int) (height * scale), (mOrgBitmap.getHeight() - y));
		Bitmap editedBitmap = Bitmap.createBitmap(mOrgBitmap, x, y, width,
		        height, null, true);
		editedBitmap.compress(format, quality, out);
		out.flush();
		out.close();

		if (editedBitmap != null)
		{
			editedBitmap.recycle();
			editedBitmap = null;
		}

		return mEditPath;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (m_curX == null || m_curY == null || m_lastX == null
		        || m_lastY == null)
		{
			m_curX = new float[2];
			m_curY = new float[2];
			m_lastX = new float[2];
			m_lastY = new float[2];
		}

		int keyAction = (event.getAction() & MotionEvent.ACTION_MASK);
		int pointerCount = event.getPointerCount();

		switch (keyAction)
		{
			case MotionEvent.ACTION_DOWN:
			{

				m_curX[0] = event.getX(0);
				m_lastX[0] = event.getX(0);
				m_curY[0] = event.getY(0);
				m_lastY[0] = event.getY(0);

				if (m_lastX[0] >= mSelectRect.left - mSelectorWidth / 2
				        && m_lastX[0] <= mSelectRect.left + mSelectorWidth / 2
				        && m_lastY[0] >= mSelectRect.top - mSelectorHeight / 2
				        && m_lastY[0] <= mSelectRect.top + mSelectorHeight / 2)
				{
					m_isFirstPointer[0] = true;
				}
				else if (m_lastX[0] >= mSelectRect.right - mSelectorWidth / 2
				        && m_lastX[0] <= mSelectRect.right + mSelectorWidth / 2
				        && m_lastY[0] >= mSelectRect.bottom - mSelectorHeight
				                / 2
				        && m_lastY[0] <= mSelectRect.bottom + mSelectorHeight
				                / 2)
				{
					m_isFirstPointer[1] = true;
				}
				else if (m_lastX[0] > mSelectRect.left
				        && m_lastX[0] < mSelectRect.right
				        && m_lastY[0] > mSelectRect.top
				        && m_lastY[0] < mSelectRect.bottom)
				{
					m_isMovePointer = true;
				}

				break;

			}
			case MotionEvent.ACTION_POINTER_DOWN:
			{
				m_curX[1] = event.getX(1);
				m_lastX[1] = event.getX(1);
				m_curY[1] = event.getY(1);
				m_lastY[1] = event.getY(1);
				if (m_lastX[1] >= mSelectRect.left - mSelectorWidth / 2
				        && m_lastX[1] <= mSelectRect.left + mSelectorWidth / 2
				        && m_lastY[1] >= mSelectRect.top - mSelectorHeight / 2
				        && m_lastY[1] <= mSelectRect.top + mSelectorHeight / 2)
				{
					m_isSecondPointer[0] = true;
				}
				else if (m_lastX[1] >= mSelectRect.right - mSelectorWidth / 2
				        && m_lastX[1] <= mSelectRect.right + mSelectorWidth / 2
				        && m_lastY[1] >= mSelectRect.bottom - mSelectorHeight
				                / 2
				        && m_lastY[1] <= mSelectRect.bottom + mSelectorHeight
				                / 2)
				{
					m_isSecondPointer[1] = true;
				}

				break;
			}
			case MotionEvent.ACTION_MOVE:
			{

				m_curX[0] = event.getX(0);
				m_curY[0] = event.getY(0);

				if (m_isMovePointer)
				{

					// move select box
					float distX = m_curX[0] - m_lastX[0];
					float distY = m_curY[0] - m_lastY[0];

					if (mSelectRect.left + distX >= mImageRect.left
					        && mSelectRect.right + distX <= mImageRect.right)
					{
						mSelectRect.left += distX;
						mSelectRect.right += distX;
					}
					if (mSelectRect.top + distY >= mImageRect.top
					        && mSelectRect.bottom + distY <= mImageRect.bottom)
					{
						mSelectRect.top += distY;
						mSelectRect.bottom += distY;
					}
				}

				// Todo curX, Y 筌Ｂ섊뵳?
				if (m_isFirstPointer[0])
				{
					if (mSelectWidthFix)
					{
						mSelectRect.left = mImageRect.left;
					}
					else if (m_curX[0] > mImageRect.left)
					{
						mSelectRect.left = Math.min((int) m_curX[0],
						        mSelectRect.right - BOUNDARY_DIMENSION[0]);
					}
					else
					{
						mSelectRect.left = mImageRect.left;
					}

					if (mSelectHeightFix)
					{
						mSelectRect.top = mImageRect.top;
					}
					else if (m_curY[0] > mImageRect.top)
					{
						mSelectRect.top = Math.min((int) m_curY[0],
						        mSelectRect.bottom - BOUNDARY_DIMENSION[1]);
					}
					else
					{
						mSelectRect.top = mImageRect.top;
					}
				}

				if (m_isFirstPointer[1])
				{
					if (mSelectWidthFix)
					{
						mSelectRect.right = mImageRect.right;
					}
					else if (m_curX[0] < mImageRect.right)
					{
						mSelectRect.right = Math.max((int) m_curX[0],
						        mSelectRect.left + BOUNDARY_DIMENSION[0]);
					}
					else
					{
						mSelectRect.right = mImageRect.right;
					}

					if (mSelectHeightFix)
					{
						mSelectRect.bottom = mImageRect.bottom;
					}
					else if (m_curY[0] < mImageRect.bottom)
					{
						mSelectRect.bottom = Math.max((int) m_curY[0],
						        mSelectRect.top + BOUNDARY_DIMENSION[1]);
					}
					else
					{
						mSelectRect.bottom = mImageRect.bottom;
					}

				}

				// lastX, lastY??curX,Y
				m_lastX[0] = m_curX[0];
				m_lastY[0] = m_curY[0];

				if (pointerCount > 1)
				{
					m_curX[1] = event.getX(1);
					m_curY[1] = event.getY(1);

					// Todo curX, Y 筌Ｂ섊뵳?
					if (m_isSecondPointer[0])
					{
						if (m_curX[1] > mImageRect.left)
						{
							mSelectRect.left = Math.min((int) m_curX[1],
							        mSelectRect.right - BOUNDARY_DIMENSION[0]);
						}
						else
						{
							mSelectRect.left = mImageRect.left;
						}

						if (m_curY[1] > mImageRect.top)
						{
							mSelectRect.top = Math.min((int) m_curY[1],
							        mSelectRect.bottom - BOUNDARY_DIMENSION[1]);
						}
						else
						{
							mSelectRect.top = mImageRect.top;
						}
					}

					if (m_isSecondPointer[1])
					{
						if (m_curX[1] < mImageRect.right)
						{
							mSelectRect.right = Math.max((int) m_curX[1],
							        mSelectRect.left + BOUNDARY_DIMENSION[0]);
						}
						else
						{
							mSelectRect.right = mImageRect.right;
						}

						if (m_curY[1] < mImageRect.bottom)
						{
							mSelectRect.bottom = Math.max((int) m_curY[1],
							        mSelectRect.top + BOUNDARY_DIMENSION[1]);
						}
						else
						{
							mSelectRect.bottom = mImageRect.bottom;
						}
					}

					// lastX, lastY??curX,Y
					m_lastX[1] = m_curX[1];
					m_lastY[1] = m_curY[1];
				}

				break;
			}
			case MotionEvent.ACTION_UP:
			{

				Log.d("talktab", "action_up\t" + event.getActionIndex() + "");

				m_isFirstPointer[0] = false;
				m_isFirstPointer[1] = false;
				m_isSecondPointer[0] = false;
				m_isSecondPointer[1] = false;

				m_isMovePointer = false;

				break;
			}
			case MotionEvent.ACTION_POINTER_UP:
			{
				int actionIdx = event.getActionIndex();

				if (actionIdx == 0)
				{
					m_isFirstPointer[0] = false;
					m_isFirstPointer[1] = false;
				}
				else
				{
					m_isSecondPointer[0] = false;
					m_isSecondPointer[1] = false;
				}

				Log.d("talktab", "action_pointer_up\t" + actionIdx + "");

				break;
			}
		}

		return true;
	}

	private class ManageThread extends Thread
	{

		SurfaceHolder tHolder;
		boolean tIsRunning;

		public ManageThread(Context context, SurfaceHolder surfaceHoler)
		{
			tHolder = surfaceHoler;
			tIsRunning = true;
		}

		public void run()
		{
			while (tIsRunning)
			{
				if (selectorPositionReset || m_isFirstPointer[0]
				        || m_isFirstPointer[1] || m_isSecondPointer[0]
				        || m_isSecondPointer[1] || m_isMovePointer)
				{
					Canvas canvas = tHolder.lockCanvas();
					canvas.drawColor(0, Mode.CLEAR);
					try
					{
						synchronized (tHolder)
						{

							if (mDegree != 0)
							{
								Matrix m = new Matrix();
								m.setRotate(mDegree, (float) mImageBitmap
								        .getWidth() / 2, (float) mImageBitmap
								        .getHeight() / 2);
								mImageBitmap = Bitmap.createBitmap(
								        mImageBitmap, 0, 0, mImageBitmap
								                .getWidth(), mImageBitmap
								                .getHeight(), m, true);

								if (mImageBitmap.getWidth() < BOUNDARY_DIMENSION[0])
								{
									mSelectWidthFix = true;
								}
								else
								{
									mSelectWidthFix = false;
								}
								if (mImageBitmap.getHeight() < BOUNDARY_DIMENSION[1])
								{
									mSelectHeightFix = true;
								}
								else
								{
									mSelectHeightFix = false;
								}

								mDegree = 0;
							}

							float imageRatio = (float) mImageBitmap.getWidth()
							        / (float) mImageBitmap.getHeight();
							float screenRatio = (float) mViewWidth
							        / (float) mViewHeight;

							if (imageRatio >= screenRatio)
							{

								mScale = (float) mViewWidth
								        / (float) mImageBitmap.getWidth();
								Matrix m = new Matrix();

								mImageRect.left = mSelectorWidth / 2;
								mImageRect.top = (int) (mViewHeight - mImageBitmap
								        .getHeight()
								        * mScale)
								        / 2 + mSelectorHeight / 2;

								mImageRect.right = mImageRect.left
								        + (int) (mImageBitmap.getWidth() * mScale);
								mImageRect.bottom = mImageRect.top
								        + (int) (mImageBitmap.getHeight() * mScale);

								m.postScale(mScale, mScale);
								m.postTranslate(mImageRect.left, mImageRect.top);
								canvas.drawBitmap(mImageBitmap, m, null);

							}
							else
							{

								mScale = (float) mViewHeight
								        / (float) mImageBitmap.getHeight();

								Matrix m = new Matrix();

								mImageRect.left = (int) (mViewWidth - mImageBitmap
								        .getWidth()
								        * mScale)
								        / 2 + mSelectorWidth / 2;
								mImageRect.top = mSelectorHeight / 2;

								mImageRect.right = mImageRect.left
								        + (int) (mImageBitmap.getWidth() * mScale);
								mImageRect.bottom = mImageRect.top
								        + (int) (mImageBitmap.getHeight() * mScale);

								m.postScale(mScale, mScale);
								m.postTranslate(mImageRect.left, mImageRect.top);
								canvas.drawBitmap(mImageBitmap, m, null);

							}

							if (selectorPositionReset)
							{

								mSelectRect = new Rect(mImageRect);

								selectorPositionReset = false;

							}

							if (m_isDrawSelection)
							{
								drawSelection(canvas);
							}

						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					finally
					{
						tHolder.unlockCanvasAndPost(canvas);
					}
				}
			}
		}

		public void setIsRunning(boolean b)
		{
			this.tIsRunning = b;
		}
	}

}
