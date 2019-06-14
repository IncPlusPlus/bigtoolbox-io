package io.github.incplusplus.bigtoolbox.io.filesys;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class File1 extends java.io.File implements Iterable<File1>
{
	private static final String DEFAULT_DIGIT_GROUP_SEPARATOR = ",";
	private int numImmediateFiles;
	private int numImmediateFolders;
	private int totalNumFiles;
	private int totalNumFolders;
	private long totalSize = 0;
	private File1 parent;
	private boolean indexed = false;
	private AtomicBoolean indexInProgress = new AtomicBoolean(false);
	//TODO Find out of this needs to be volatile
	List<File1> children = new LinkedList<>();
	
	public File1(String pathname)
	{
		super(pathname);
//		updateImmediateChildren();
	}
	
	public File1(java.io.File in)
	{
		super(in.getAbsolutePath());
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
	
	public boolean isRoot()
	{
		return parent == null;
	}
	
	@Override
	public File1[] listFiles()
	{
//		ensureIndexed();
		return children == null ? new File1[0] : bulkConvert(super.listFiles());
	}
	
	private File1[] bulkConvert(java.io.File... arr)
	{
		if (arr == null)
		{
			return new File1[0];
		}
		File1[] out = new File1[arr.length];
		for (int i = 0; i < arr.length; i++)
		{
			out[i] = new File1(arr[i]);
		}
		return out;
	}
	
	private void ensureIndexed()
	{
		if (!indexed)
		{
			//If indexing is not in progress, set indexInProgress to true and index!
			if(!indexInProgress.compareAndExchange(false,true))
			{
				index();
				//Now we want to set indexInProgress back to false
				//If indexInProgress isn't still true, something has gone horribly wrong
				assert(indexInProgress.compareAndExchange(true, false));
			}
			indexed=true;
		}
		//todo
	}
	
	public long getSize()
	{
//		ensureIndexed();
		return totalSize;
	}
	
	private void setParent(File1 parent)
	{
		this.parent = parent;
	}
	
	void index()
	{
		//TODO Determine if the children already exist so they can be cleared!!!
		java.io.File[] localContents = super.listFiles();
		if (localContents == null || localContents.length == 0)
		{
			return;
		}
		//TODO test if this can just continue past in the case there are ZERO availableProcessors()
		ExecutorService WORKER_THREAD_POOL = Executors.newFixedThreadPool(10);
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
				tempDir.setParent(this);
				children.add(tempDir);
			}
			else if (i.isFile())
			{
				File1 tempFile = new File1(i.toURI());
				totalSize += tempFile.length();
				tempFile.setParent(this);
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
			for (Future<File1> f1 : futures)
			{
				if (!f1.isDone()) throw new AssertionError();
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
				this.thisFile.ensureIndexed();
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
	
	public File1 getLargestFile()
	{
		//todo
//		children.parallelStream()
		return null;
//		return getLargestFile();
	}
	
	@Override
	public Iterator<File1> iterator()
	{
		ensureIndexed();
//		ensureIndexed();
		File1Iter iter = new File1Iter(this);
		return iter;
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
//		ensureIndexed();
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
	
	public String renderDirectoryTree()
	{
		List<StringBuilder> lines = renderDirectoryTreeLines(this);
//		String newline = System.getProperty("line.separator");
		String newline = "\n";
		StringBuilder sb = new StringBuilder(lines.size() * 20);
		for (StringBuilder line : lines)
		{
			sb.append(line);
			sb.append(newline);
		}
		return sb.toString();
	}
	
	private List<StringBuilder> renderDirectoryTreeLines(File1 tree)
	{
		tree.ensureIndexed();
		List<StringBuilder> result = new LinkedList<>();
		result.add(new StringBuilder().append(tree.getName()));
//		Iterator<File1> iterator = tree.children.iterator();
		Iterator<File1> sortedIterator = tree.children.stream().sorted().collect(Collectors.toList()).iterator();
		while (sortedIterator.hasNext())
		{
			List<StringBuilder> subtree =
					renderDirectoryTreeLines(sortedIterator.next());
			if (sortedIterator.hasNext())
			{
				addSubtree(result, subtree);
			}
			else
			{
				addLastSubtree(result, subtree);
			}
		}
		return result;
	}
	
	private void addSubtree(List<StringBuilder> result,
	                        List<StringBuilder> subtree)
	{
		Iterator<StringBuilder> iterator = subtree.iterator();
		//subtree generated by renderDirectoryTreeLines has at least one line which is tree.getData()
		result.add(iterator.next().insert(0, "├── "));
		while (iterator.hasNext())
		{
			result.add(iterator.next().insert(0, "│   "));
		}
	}
	
	private void addLastSubtree(List<StringBuilder> result,
	                            List<StringBuilder> subtree)
	{
		Iterator<StringBuilder> iterator = subtree.iterator();
		//subtree generated by renderDirectoryTreeLines has at least one line which is tree.getData()
		result.add(iterator.next().insert(0, "└── "));
		while (iterator.hasNext())
		{
			result.add(iterator.next().insert(0, "    "));
		}
	}
}