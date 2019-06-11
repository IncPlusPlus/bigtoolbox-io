package io.github.incplusplus.bigtoolbox.io.filesys;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class File1 extends java.io.File
{
	private int numImmediateFiles;
	private int numImmediateFolders;
	private int totalNumFiles;
	private int totalNumFolders;
	private long totalSize = 0;
	//TODO Find out of this needs to be volatile
	private ArrayList<File1> children = new ArrayList<>();
	
	public File1(String pathname)
	{
		super(pathname);
//		updateImmediateChildren();
	}
	
	public File1(String parent, String child)
	{
		super(parent, child);
//		updateImmediateChildren();
	}
	
	public File1(java.io.File parent, String child)
	{
		super(parent, child);
//		updateImmediateChildren();
	}
	
	public File1(URI uri)
	{
		super(uri);
//		updateImmediateChildren();
	}
	
	@Override
	public File1[] listFiles()
	{
		return children == null ? new File1[0] : bulkConvert(super.listFiles());
	}
	
	private File1[] bulkConvert(java.io.File[] arr)
	{
		if (arr == null)
		{
			return new File1[0];
		}
		File1[] out = new File1[arr.length];
		for (int i = 0; i < arr.length; i++)
		{
			out[i] = (File1) arr[i];
		}
		return out;
	}

//	private void updateImmediateChildren()
//	{
//		children = this.listFiles();
//	}
	
	public void index()
	{
		java.io.File[] localContents = super.listFiles();
		if (localContents == null || localContents.length == 0)
		{
			return;
		}
		//TODO test if this can just continue past in the case there are ZERO availableProcessors()
		ExecutorService WORKER_THREAD_POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//		ArrayList<File2> tempDirs = new ArrayList<>(localContents.length);
		List<Callable<File1>> callables = new ArrayList<>();
		for (java.io.File i : localContents)
		{
			if (i.isDirectory())
			{
				File1 tempDir = new File1(i.toURI());
//				tempDir.index();
				Callable<File1> runnableIndexer = runnableIndexerFor(tempDir);
				callables.add(runnableIndexer);
				totalSize += tempDir.getSize();
				children.add(tempDir);
			}
			else if (i.isFile())
			{
				File1 tempFile = new File1(i.toURI());
				totalSize += tempFile.length();
				children.add(tempFile);
			}
		}
		try
		{
			List<Future<File1>> futures = WORKER_THREAD_POOL.invokeAll(callables);
			if (futures.size() > 0)
			{
				boolean allDone = false;
				while (!allDone)
				{
//				System.out.println("in loop!");
					for (Future<File1> future : futures)
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
			for (Future<File1> f3 : futures)
			{
				if (!f3.isDone()) throw new AssertionError();
			}
			for (Future<File1> future : futures)
			{
				totalSize += future.get().getSize();
			}
		}
		catch (InterruptedException | ExecutionException e)
		{
			e.printStackTrace();
		}
	}
	
	Callable<File1> runnableIndexerFor(File1 f)
	{
		class CallableIndexer implements Callable<File1>
		{
			File1 thisFile;
			
			CallableIndexer(File1 file_constructor)
			{
				this.thisFile = file_constructor;
			}
			
			@Override
			public File1 call()
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
	
	public long getSize()
	{
		return totalSize;
	}
}