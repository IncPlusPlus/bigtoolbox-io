package io.github.incplusplus.bigtoolbox.io.filesys;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class File2 extends java.io.File
{
	private long totalSize = 0;
	//TODO Find out of this needs to be volatile
	private volatile ArrayList<File2> children = new ArrayList<>();
	
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
		return children == null ? new File2[0] : bulkConvert(super.listFiles());
	}
	
	private File2[] bulkConvert(File[] arr)
	{
		if(arr==null)
		{
			return new File2[0];
		}
		File2[] out = new File2[arr.length];
		for(int i = 0; i < arr.length; i++)
		{
			out[i]=(File2)arr[i];
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
		ExecutorService WORKER_THREAD_POOL = Executors.newFixedThreadPool(1);
		ArrayList<File2> tempDirs = new ArrayList<>(localContents.length);
		List<Callable<File2>> callables = new ArrayList<>();
		for (File i : localContents)
		{
			if (i.isDirectory())
			{
				File2 tempDir = new File2(i.toURI());
//				tempDir.index();
				callables.add(runnableIndexerFor(tempDir));
				totalSize += tempDir.getSize();
				children.add(tempDir);
			} else if (i.isFile())
			{
				// TODO deal with passing excluded files up the chain
				io.github.incplusplus.bigtoolbox.io.filesys.File2 tempFile = new File2(i.toURI());
				totalSize += tempFile.length();
				children.add(tempFile);
			}
		}
		try
		{
			List<Future<File2>> futures = WORKER_THREAD_POOL.invokeAll(callables);
			boolean allDone = false;
			while(!allDone)
			{
//				System.out.println("in loop!");
				for(int i = 0; i < futures.size(); i++)
				{
					if(!futures.get(i).isDone())
					{
						System.out.println("Not all done ;-;");
						allDone=false;
						break;
					}
					else
					{
						allDone=true;
						System.out.println(futures.get(i).get().getName() + " is done!");
					}
				}
				allDone=true;
			}
			WORKER_THREAD_POOL.shutdownNow();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		catch (ExecutionException e)
		{
			e.printStackTrace();
		}
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
				System.out.println("About to index " + this.thisFile.getName());
				this.thisFile.index();
				System.out.println("Finished indexing " + this.thisFile.getName());
				if (Thread.currentThread().isInterrupted()) {
					// Cannot use InterruptedException since it's checked
					throw new RuntimeException();
				}
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
