package io.github.incplusplus.bigtoolbox.io.filesys;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.Deprecated;

/**
 * This class represents a file within a Directory.
 * This class is named Record to avoid conflicts with Java's own File class.
 */
@Deprecated(since = "1.0.50", forRemoval = true)
public class Record extends Entry
{
	/*
	 * TODO Add various hashing/checksum techniques
	 * TODO Add a compare function that uses hashing
	 * TODO Implement some kind of warning system IF the user is on Windows (re: path length limit)
	 *  Maybe check for registry (and/or group policy) entry that expands path length
	 *  It would likely be good to have this be part of a VERBOSE flag
	 *  See https://www.howtogeek.com/266621/how-to-make-windows-10-accept-file-paths-over-260-characters/
	 *  Also see https://docs.microsoft.com/en-us/windows/desktop/fileio/naming-a-file
	 */

	private File thisFile;
	private boolean hasNoContents = false;

	public Record()
	{
		thisFile = null;
		hasNoContents = true;
	}

	public Record(String filename)
	{
		this(new File(filename));
	}

	public Record(File in)
	{
		if(in == null)
		{
			try
			{
				throw new FileNotFoundException("File doesn't exist!");
			}
			catch(FileNotFoundException e)
			{
				e.printStackTrace();
			}
		}
		else if(in.exists())
		{
			if(in.isFile())
			{
				thisFile = in;
			}
			else
			{
				try
				{
					throw new FileNotFoundException("File exists but isn't a file!");
				}
				catch(FileNotFoundException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public long getSize()
	{
		if(thisFile != null)
		{
			return thisFile.length();
		}
		else
		{
			return 0;
		}
	}

	/**
	 * @return a Record if this is a Record or an empty Record if this is not a Record
	 */
	@Override
	public Record asRecord()
	{
		return this;
	}

	@Override
	public Directory asDirectory()
	{
		return new Directory();
	}

	@Override
	public boolean hasNoContents()
	{
		return hasNoContents;
	}

	@Override
	public String getName()
	{
		return thisFile.getName();
	}
}
