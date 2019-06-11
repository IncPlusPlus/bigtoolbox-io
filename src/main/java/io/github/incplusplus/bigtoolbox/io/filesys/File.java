package io.github.incplusplus.bigtoolbox.io.filesys;

import java.net.URI;
import java.util.Arrays;

public class File extends java.io.File
{
	//TODO Find out of this needs to be volatile
	private volatile File[] children;
	
	public File(String pathname)
	{
		super(pathname);
//		updateImmediateChildren();
	}
	
	public File(String parent, String child)
	{
		super(parent, child);
//		updateImmediateChildren();
	}
	
	public File(java.io.File parent, String child)
	{
		super(parent, child);
//		updateImmediateChildren();
	}
	
	public File(URI uri)
	{
		super(uri);
//		updateImmediateChildren();
	}
	
	@Override
	public File[] listFiles()
	{
		//TODO also check if a sub-child is no longer valid (path has changed)
		java.io.File[] initialList = super.listFiles();
		if (initialList == null)
		{
			return new File[0];
		}
		children = new File[initialList.length];
		for (int i = 0; i < children.length; i++)
		{
			children[i]=new File(this,initialList[i].getName());
		}
		return children;
	}
	
	private void updateImmediateChildren()
	{
		children = this.listFiles();
	}
	
	public Long getSize()
	{
		if (this.isFile())
		{
			return this.length();
		}
		else if(this.isDirectory())
		{
			return Arrays.stream(listFiles()).map(File::getSize).reduce(Long::sum).orElse(0L);
		}
//		return Arrays.stream(listFiles()).map(File::getSize).reduce(BigInteger::add).orElse(BigInteger.ZERO);
		return null;
	}
	
}
