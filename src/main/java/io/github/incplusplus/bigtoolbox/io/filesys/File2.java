package io.github.incplusplus.bigtoolbox.io.filesys;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class File2 extends java.io.File
{
	private final String DEFAULT_DIGIT_GROUP_SEPARATOR = ",";
	private int numImmediateFiles;
	private int numImmediateFolders;
	private int totalNumFiles;
	private int totalNumFolders;
	private long totalSize = 0;
	//TODO Find out of this needs to be volatile
	private ArrayList<File2> children = new ArrayList<>();
	
	public File2(String pathname)
	{
		super(pathname);
//		updateImmediateChildren();
	}
	
	public File2(String parent, String child)
	{
		super(parent, child);
//		updateImmediateChildren();
	}
	
	public File2(java.io.File parent, String child)
	{
		super(parent, child);
//		updateImmediateChildren();
	}
	
	public File2(URI uri)
	{
		super(uri);
//		updateImmediateChildren();
	}
	
	@Override
	public File2[] listFiles()
	{
		ensureIndexed();
		return children == null ? new File2[0] : bulkConvert(super.listFiles());
	}
	
	private File2[] bulkConvert(java.io.File... arr)
	{
		if (arr == null)
		{
			return new File2[0];
		}
		File2[] out = new File2[arr.length];
		for (int i = 0; i < arr.length; i++)
		{
			out[i] = (File2) arr[i];
		}
		return out;
	}
	
	private void ensureIndexed()
	{
		
		//todo
	}
	
	public long getSize()
	{
		ensureIndexed();
		return totalSize;
	}
	
	private void index()
	{
		java.io.File[] localContents = super.listFiles();
		if (localContents == null || localContents.length == 0)
		{
			return;
		}
		//TODO test if this can just continue past in the case there are ZERO availableProcessors()
		ExecutorService WORKER_THREAD_POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//		ArrayList<File2> tempDirs = new ArrayList<>(localContents.length);
		List<Callable<File2>> callables = new ArrayList<>();
		for (java.io.File i : localContents)
		{
			if (i.isDirectory())
			{
				File2 tempDir = new File2(i.toURI());
//				tempDir.index();
				
				Callable<File2> runnableIndexer = runnableIndexerFor(tempDir);
				callables.add(runnableIndexer);
				totalSize += tempDir.getSize();
				children.add(tempDir);
			}
			else if (i.isFile())
			{
				File2 tempFile = new File2(i.toURI());
				totalSize += tempFile.length();
				children.add(tempFile);
			}
		}
		try
		{
			List<Future<File2>> futures = WORKER_THREAD_POOL.invokeAll(callables);
			if (futures.size() > 0)
			{
				boolean allDone = false;
				while (!allDone)
				{
//				System.out.println("in loop!");
					for (Future<File2> future : futures)
					{
						if (!future.isDone())
						{
//						System.out.println("Not all done ;-;");
							allDone = false;
							break;
						}
						else
						{
							allDone = true;
//						System.out.println(future.get().getName() + " is done!");
						}
					}
				}
			}
			WORKER_THREAD_POOL.shutdownNow();
			/*
			 * If we've gotten here. All the threads should be done.
			 * If they are not done and we're here, something has gone horribly wrong.
			 */
			for (Future<File2> f1 : futures)
			{
				if (!f1.isDone()) throw new AssertionError();
			}
			for (Future<File2> future : futures)
			{
				totalSize += future.get().getSize();
			}
		}
		catch (InterruptedException | ExecutionException e)
		{
			e.printStackTrace();
		}
	}
	
	public File2 getLargestFile()
	{
		index();
//		children.parallelStream()
		return null;
//		return getLargestFile();
	}
	
	Callable<File2> runnableIndexerFor(File2 f)
	{
		class CallableIndexer implements Callable<File2>
		{
			File2 thisFile;
			
			CallableIndexer(File2 file_constructor)
			{
				this.thisFile = file_constructor;
			}
			
			@Override
			public File2 call()
			{
//				System.out.println("About to index " + this.thisFile.getName());
				this.thisFile.index();
//				System.out.println("Finished indexing " + this.thisFile.getName());
//				if (Thread.currentThread().isInterrupted())
//				{
//					// Cannot use InterruptedException since it's checked
//					throw new RuntimeException();
//				}
				return thisFile;
			}
		}
		return new CallableIndexer(f);
	}
	
	private String getSuffix()
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