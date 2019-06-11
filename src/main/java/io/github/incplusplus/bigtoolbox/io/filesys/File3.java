package io.github.incplusplus.bigtoolbox.io.filesys;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class File3 extends File
{
	private long totalSize = 0;
	//TODO Find out of this needs to be volatile
	private ArrayList<File3> children = new ArrayList<>();
	
	public File3(String pathname)
	{
		super(pathname);
//		updateImmediateChildren();
	}
	
	public File3(String parent, String child)
	{
		super(parent, child);
//		updateImmediateChildren();
	}
	
	public File3(File parent, String child)
	{
		super(parent, child);
//		updateImmediateChildren();
	}
	
	public File3(URI uri)
	{
		super(uri);
//		updateImmediateChildren();
	}
	
	@Override
	public File3[] listFiles()
	{
		return children == null ? new File3[0] : bulkConvert(super.listFiles());
	}
	
	private File3[] bulkConvert(File[] arr)
	{
		if (arr == null)
		{
			return new File3[0];
		}
		File3[] out = new File3[arr.length];
		for (int i = 0; i < arr.length; i++)
		{
			out[i] = (File3) arr[i];
		}
		return out;
	}

//	private void updateImmediateChildren()
//	{
//		children = this.listFiles();
//	}
	
	public void index()
	{
		File[] localContents = super.listFiles();
		if (localContents == null || localContents.length == 0)
		{
			return;
		}
		//TODO test if this can just continue past in the case there are ZERO availableProcessors()
		ExecutorService WORKER_THREAD_POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
//		ArrayList<File2> tempDirs = new ArrayList<>(localContents.length);
		List<Callable<File3>> callables = new ArrayList<>();
		for (int i1 = 0; i1 < localContents.length; i1++)
		{
			File i = localContents[i1];
			if (i.isDirectory())
			{
				File3 tempDir = new File3(i.toURI());
//				tempDir.index();
				Callable<File3> runnableIndexer = runnableIndexerFor(tempDir);
				callables.add(runnableIndexer);
				totalSize += tempDir.getSize();
				children.add(tempDir);
			}
			else if (i.isFile())
			{
				File3 tempFile = new File3(i.toURI());
				totalSize += tempFile.length();
				children.add(tempFile);
			}
		}
		try
		{
			List<Future<File3>> futures = WORKER_THREAD_POOL.invokeAll(callables);
			if (futures.size() > 0)
			{
				boolean allDone = false;
				while (!allDone)
				{
//				System.out.println("in loop!");
					for (Future<File3> future : futures)
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
			for (Future<File3> f3 : futures)
			{
				if (!f3.isDone()) throw new AssertionError();
			}
			for (Future<File3> future : futures)
			{
				totalSize += future.get().getSize();
			}
		}
		catch (InterruptedException | ExecutionException e)
		{
			e.printStackTrace();
		}
	}
	
	Callable<File3> runnableIndexerFor(File3 f)
	{
		class CallableIndexer implements Callable<File3>
		{
			File3 thisFile;
			
			CallableIndexer(File3 file_constructor)
			{
				this.thisFile = file_constructor;
			}
			
			@Override
			public File3 call()
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