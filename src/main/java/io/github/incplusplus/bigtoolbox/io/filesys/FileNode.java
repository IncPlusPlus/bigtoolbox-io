package io.github.incplusplus.bigtoolbox.io.filesys;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

public class FileNode extends File implements Iterable<FileNode>
{
	
	//	public File data;
	public FileNode parent;
	public List<FileNode> children;
	
	public boolean isRoot()
	{
		return parent == null;
	}
	
	public boolean isLeaf()
	{
		return children.size() == 0;
	}
	private long size = 0L;

	private List<FileNode> elementsIndex;
	
	public FileNode(File data)
	{
		super(data.toURI());
//		this.data = data;
		this.children = new LinkedList<FileNode>();
		this.elementsIndex = new LinkedList<FileNode>();
		this.elementsIndex.add(this);
	}
	
	public FileNode(String pathname)
	{
		super(pathname);
		this.children = new LinkedList<FileNode>();
		this.elementsIndex = new LinkedList<FileNode>();
		this.elementsIndex.add(this);
	}
	
	public FileNode(String parent, String child)
	{
		super(parent, child);
		this.children = new LinkedList<FileNode>();
		this.elementsIndex = new LinkedList<FileNode>();
		this.elementsIndex.add(this);
	}
	
	public FileNode(java.io.File parent, String child)
	{
		super(parent, child);
		this.children = new LinkedList<FileNode>();
		this.elementsIndex = new LinkedList<FileNode>();
		this.elementsIndex.add(this);
	}
	
	public FileNode(URI uri)
	{
		super(uri);
		this.children = new LinkedList<FileNode>();
		this.elementsIndex = new LinkedList<FileNode>();
		this.elementsIndex.add(this);
	}
	
	private FileNode addChild(File child)
	{
		FileNode childNode = new FileNode(child);
		childNode.parent = this;
		this.children.add(childNode);
		this.registerChildForSearch(childNode);
		return childNode;
	}
	
	private int getLevel()
	{
		if (this.isRoot())
			return 0;
		else
			return parent.getLevel() + 1;
	}
	
	private void registerChildForSearch(FileNode node)
	{
		elementsIndex.add(node);
		if (parent != null)
			parent.registerChildForSearch(node);
	}
	
	public FileNode findTreeNode(File illusiveFile)
	{
		for (FileNode element : this.elementsIndex)
		{
			if (illusiveFile.getAbsolutePath().equals(element.getAbsolutePath()))
				return element;
		}
		return null;
	}
	
	@Override
	public String toString()
	{
		return super.toString();
	}
	
	@Override
	public Iterator<FileNode> iterator()
	{
		FileNodeIter<Object> iter = new FileNodeIter<Object>(this);
		return iter;
	}
	
	@Override
	public long length()
	{
		return 0L;
	}
	
	public void index()
	{
		if(!this.exists())
		{
			System.out.println("SEVERE: FileNode created for file that doesn't exist: " + this);
		}
		if (this.isFile())
		{
			//nothing to do
			return;
		}
		for (File file : safeListFiles(this))
		{
			if (file.isDirectory())
			{
				appendDirTree(file, this);
			}
			else if(file.isFile())
			{
				appendFile(file, this);
			}
		}
	}
	
	private void appendDirTree(File folder, FileNode DirRoot)
	{
		ExecutorService WORKER_THREAD_POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		List<Callable<FileNode>> callables = new ArrayList<>();
		for (File file : safeListFiles(DirRoot.addChild(folder)))
		{
			if (file.isDirectory())
			{
				appendDirTree(file,
						DirRoot.children.get(DirRoot.children.size() - 1));
			}
			else if(file.isFile())
			{
				appendFile(file,
						DirRoot.children.get(DirRoot.children.size() - 1));
			}
		}
		try
		{
			List<Future<FileNode>> futures = WORKER_THREAD_POOL.invokeAll(callables);
			if (futures.size() > 0)
			{
				boolean allDone = false;
				while (!allDone)
				{
//				System.out.println("in loop!");
					for (Future<FileNode> future : futures)
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
			for (Future<FileNode> f1 : futures)
			{
				if (!f1.isDone()) throw new AssertionError();
			}
			for (Future<FileNode> future : futures)
			{
				size += future.get().length();
			}
		}
		catch (InterruptedException | ExecutionException e)
		{
			e.printStackTrace();
		}
	}
	
	
	private File[] safeListFiles(FileNode currentNode)
	{
		File[] out = currentNode.listFiles();
		return out == null ? new File[0] : out;
	}
	
	private void appendFile(File file, FileNode filenode)
	{
		filenode.addChild(file);
	}
	
	Callable<FileNode> runnableIndexerFor(FileNode f)
	{
		class CallableIndexer implements Callable<FileNode>
		{
			FileNode thisFile;
			
			CallableIndexer(FileNode file_constructor)
			{
				this.thisFile = file_constructor;
			}
			
			@Override
			public FileNode call()
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
	
	public String renderDirectoryTree()
	{
		List<StringBuilder> lines = renderDirectoryTreeLines(this);
		String newline = System.getProperty("line.separator");
		StringBuilder sb = new StringBuilder(lines.size() * 20);
		for (StringBuilder line : lines)
		{
			sb.append(line);
			sb.append(newline);
		}
		return sb.toString();
	}
	
	private List<StringBuilder> renderDirectoryTreeLines(FileNode tree)
	{
		List<StringBuilder> result = new LinkedList<>();
		result.add(new StringBuilder().append(tree.getName()));
		Iterator<FileNode> iterator = tree.children.iterator();
		while (iterator.hasNext())
		{
			List<StringBuilder> subtree =
					renderDirectoryTreeLines(iterator.next());
			if (iterator.hasNext())
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

//	public static void main(String[] args) {
//		File file = new File("./");
//		FileNode DirTree = index(file);
//		String result = renderDirectoryTree(DirTree);
//		System.out.println(result);
//		try {
//			File newTextFile = new File("./DirectoryTree.txt");
//
//			FileWriter fw = new FileWriter(newTextFile);
//			fw.write(result);
//			fw.close();
//
//		} catch (IOException iox) {
//			iox.printStackTrace();
//		}
//
//	}
}