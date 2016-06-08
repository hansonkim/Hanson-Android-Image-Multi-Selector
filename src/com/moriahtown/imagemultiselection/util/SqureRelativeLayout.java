package com.moriahtown.imagemultiselection.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class SqureRelativeLayout extends RelativeLayout
{

	public SqureRelativeLayout(Context context, AttributeSet attrs)
    {
	    super(context, attrs);
    }

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightmeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, widthMeasureSpec);
	}
}
