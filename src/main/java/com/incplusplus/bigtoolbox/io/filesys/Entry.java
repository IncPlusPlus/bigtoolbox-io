package com.incplusplus.bigtoolbox.io.filesys;

public abstract class Entry
{
	private final String DEFAULT_DIGIT_GROUP_SEPARATOR = ",";

	abstract long getSize();

	/**
	 * @return a Record if this is a Record or an empty Record if this is not a Record
	 */
	abstract Record asRecord();

	abstract Directory asDirectory();

	abstract boolean hasNoContents();

	abstract String getName();

	public String getSuffix()
	{
		long size = this.getSize();
		if(size <= Math.pow(1024, 1))
		{
			return "B";
		}
		else if(size <= Math.pow(1024, 2))
		{
			return "KB";
		}
		else if(size <= Math.pow(1024, 3))
		{
			return "MB";
		}
		else if(size <= Math.pow(1024, 4))
		{
			return "GB";
		}
		else if(size <= Math.pow(1024, 5))
		{
			return "TB";
		}
		else if(size <= Math.pow(1024, 6))
		{
			return "PB";
		}
		else if(size <= Math.pow(1024, 7))
		{
			return "EB";
		}
		else
		{
			return "??";
		}
	}

	public String getFormattedSize()
	{
		return getFormattedSize(false, false);
	}

	public String getFormattedSize(boolean createSpaces, boolean addSeparator)
	{
		return getFormattedSize(createSpaces, addSeparator, DEFAULT_DIGIT_GROUP_SEPARATOR);
	}

	public String getFormattedSize(boolean createSpaces, boolean addSeparator, String separator)
	{
		//TODO add implementation
		return this.getSize() + " " + this.getSuffix();
	}
}
