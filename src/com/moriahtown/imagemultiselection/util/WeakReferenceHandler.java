package com.moriahtown.imagemultiselection.util;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;

public abstract class WeakReferenceHandler<T> extends Handler
{
	private WeakReference<T> mReference;

    public WeakReferenceHandler(T reference)
    {
        mReference = new WeakReference<T>(reference);
    }

    @Override
    public void handleMessage(Message msg)
    {
        if (mReference.get() == null)
            return;
        handleMessage(mReference.get(), msg);
    }

    protected abstract void handleMessage(T reference, Message msg);
}
