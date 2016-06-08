package com.moriahtown.imagemultiselection.dataobj;

public class Album
{
	private long id;
	private String bucketDisplayName;
	private String bucketId;
	private long dateTaken;
	private String data;
	private int count;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getBucketDisplayName()
	{
		return bucketDisplayName;
	}

	public void setBucketDisplayName(String bucketDisplayName)
	{
		this.bucketDisplayName = bucketDisplayName;
	}

	public String getBucketId()
	{
		return bucketId;
	}

	public void setBucketId(String bucketId)
	{
		this.bucketId = bucketId;
	}

	public long getDateTaken()
	{
		return dateTaken;
	}

	public void setDateTaken(long dateTaken)
	{
		this.dateTaken = dateTaken;
	}

	public String getData()
	{
		return data;
	}

	public void setData(String data)
	{
		this.data = data;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	@Override
	public String toString()
	{
		return new String() + this.id + ", " + this.bucketDisplayName + ", "
		        + this.bucketId + ", " + this.dateTaken + "," + this.data + "," + this.count;
	}

}
