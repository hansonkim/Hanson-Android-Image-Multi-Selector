package com.moriahtown.imagemultiselection.dataobj;

import android.net.Uri;

public class ImageItem
{
	private long orgId;
	private Uri orgUri;
	private Uri thumbUri;
	private boolean isChecked;

	public long getOrgId()
	{
		return orgId;
	}

	public void setOrgId(long orgId)
	{
		this.orgId = orgId;
	}

	public Uri getOrgUri()
	{
		return orgUri;
	}

	public void setOrgUri(Uri orgUri)
	{
		this.orgUri = orgUri;
	}

	public Uri getThumbUri()
	{
		return thumbUri;
	}

	public void setThumbUri(Uri thumbUri)
	{
		this.thumbUri = thumbUri;
	}

	public boolean isChecked()
	{
		return isChecked;
	}

	public void setChecked(boolean isChecked)
	{
		this.isChecked = isChecked;
	}

}
