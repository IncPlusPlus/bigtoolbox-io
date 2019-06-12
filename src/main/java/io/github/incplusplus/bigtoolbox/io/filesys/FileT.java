package io.github.incplusplus.bigtoolbox.io.filesys;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FileT extends java.io.File
{
	private final String DEFAULT_DIGIT_GROUP_SEPARATOR = ",";
	private int numImmediateFiles;
	private int numImmediateFolders;
	private int totalNumFiles;
	private int totalNumFolders;
	private long totalSize = 0;
	private FileT parent;
	//TODO Find out of this needs to be volatile
	private ArrayList<FileT> children = new ArrayList<>();
	
	public FileT(String pathname)
	{
		super(pathname);
//		updateImmediateChildren();
	}
	
	public FileT(String parent, String child)
	{
		super(parent, child);
//		updateImmediateChildren();
	}
	
	public FileT(java.io.File parent, String child)
	{
		super(parent, child);
//		updateImmediateChildren();
	}
	
	public FileT(URI uri)
	{
		super(uri);
//		updateImmediateChildren();
	}
	
	public boolean isRoot()
	{
		return parent == null;
	}
	
	@Override
	public FileT[] listFiles()
	{
		ensureIndexed();
		return children == null ? new FileT[0] : bulkConvert(super.listFiles());
	}
	
	private FileT[] bulkConvert(java.io.File... arr)
	{
		if (arr == null)
		{
			return new FileT[0];
		}
		FileT[] out = new FileT[arr.length];
		for (int i = 0; i < arr.length; i++)
		{
			out[i] = (FileT) arr[i];
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
		//TODO Determine if the children already exist so they can be cleared!!!
		java.io.File[] localContents = super.listFiles();
		if (localContents == null || localContents.length == 0)
		{
			return;
		}
		//TODO test if this can just continue past in the case there are ZERO availableProcessors()
		ExecutorService WORKER_THREAD_POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//		ArrayList<File2> tempDirs = new ArrayList<>(localContents.length);
		List<Callable<FileT>> callables = new ArrayList<>();
		for (java.io.File i : localContents)
		{
			if (i.isDirectory())
			{
				FileT tempDir = new FileT(i.toURI());
//				tempDir.index();
				
				Callable<FileT> runnableIndexer = runnableIndexerFor(tempDir);
				callables.add(runnableIndexer);
				totalSize += tempDir.getSize();
				children.add(tempDir);
			}
			else if (i.isFile())
			{
				FileT tempFile = new FileT(i.toURI());
				totalSize += tempFile.length();
				children.add(tempFile);
			}
		}
		try
		{
			List<Future<FileT>> futures = WORKER_THREAD_POOL.invokeAll(callables);
			if (futures.size() > 0)
			{
				boolean allDone = false;
				while (!allDone)
				{
//				System.out.println("in loop!");
					for (Future<FileT> future : futures)
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
			for (Future<FileT> f1 : futures)
			{
				if (!f1.isDone()) throw new AssertionError();
			}
			for (Future<FileT> future : futures)
			{
				totalSize += future.get().getSize();
			}
		}
		catch (InterruptedException | ExecutionException e)
		{
			e.printStackTrace();
		}
	}
	
	public FileT getLargestFile()
	{
		index();
//		children.parallelStream()
		return null;
//		return getLargestFile();
	}
	
	Callable<FileT> runnableIndexerFor(FileT f)
	{
		class CallableIndexer implements Callable<FileT>
		{
			FileT thisFile;
			
			CallableIndexer(FileT file_constructor)
			{
				this.thisFile = file_constructor;
			}
			
			@Override
			public FileT call()
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
		if (size <= Math.pow(1024, 1))
		{
			return "B";
		}
		else if (size <= Math.pow(1024, 2))
		{
			return "KB";
		}
		else if (size <= Math.pow(1024, 3))
		{
			return "MB";
		}
		else if (size <= Math.pow(1024, 4))
		{
			return "GB";
		}
		else if (size <= Math.pow(1024, 5))
		{
			return "TB";
		}
		else if (size <= Math.pow(1024, 6))
		{
			return "PB";
		}
		else if (size <= Math.pow(1024, 7))
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