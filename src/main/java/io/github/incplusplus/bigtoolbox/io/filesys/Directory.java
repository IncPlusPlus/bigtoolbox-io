package io.github.incplusplus.bigtoolbox.io.filesys;

import java.io.File;
import java.util.ArrayList;
import java.lang.Deprecated;

/**
 * This class represents a single directory (A.K.A. a folder)
 */
@Deprecated(since = "1.0.50", forRemoval = true)
public class Directory extends Entry
{
	// TODO add verbosity option to indexing method
	// TODO Add a function to grab the smallest and largest of the files scanned
	// TODO Add a function to grab the largest and smallest folders???
	private boolean verbose;
	//Assume that changes could happen and the directory should be re-indexed before attempting each action
	private boolean assumeChangesCouldHappen;
	private boolean alreadyIndexed;
	private int numFolders = 0;
	private int numFiles = 0;
	private long totalSize;
	//TODO Check if this is still relevant (it may no longer be)
	private final String[] filesToIgnoreBuiltIn = {"Directory.class", "File.class", "FileRelatedTools.class"};
	private ArrayList<String> excludedFiles;
	private ArrayList<Record> foundExcludedFiles = new ArrayList<>();
	private File thisDirectory;
	private ArrayList<Entry> contents = new ArrayList<>();
	private int numExcludedFound;
	private boolean hasNoContents = false;

	public Directory()
	{
		alreadyIndexed = true;
		assumeChangesCouldHappen = false;
		totalSize = 0;
		excludedFiles = new ArrayList<>();
		numExcludedFound = 0;
		numFolders = 0;
		numFiles = 0;
		hasNoContents = true;
	}

	public Directory(String path)
	{
		this(new File(path));
	}

	public Directory(File path)
	{
		this(false, path, new String[]{}, false);
	}

	public Directory(File path, boolean verbose)
	{
		this(false, path, new String[]{}, verbose);
	}

	public Directory(boolean assumeChangesCouldHappen, File path)
	{
		this(assumeChangesCouldHappen, path, new String[]{}, false);
	}

	public Directory(boolean assumeChangesCouldHappen, File path, String[] filesToIgnore)
	{
		this(assumeChangesCouldHappen, path, filesToIgnore, false);
	}

	public Directory(boolean assumeChangesCouldHappen, File path, String[] filesToIgnore, boolean verbose)
	{
		this.assumeChangesCouldHappen = assumeChangesCouldHappen;
		this.thisDirectory = path;
		this.verbose = verbose;
		excludedFiles = new ArrayList<>();
		for(String i : filesToIgnoreBuiltIn)
		{
			excludedFiles.add(i);
		}
		for(String i : filesToIgnore)
		{
			if(verbose)
			{
				System.out.println("Excluding file: " + i);
			}
			if(!excludedFiles.contains(i))
			{
				excludedFiles.add(i);
			}
		}
		if(verbose)
		{
			System.out.println("New Directory created for " + thisDirectory);
		}
	}

	/**
	 * Index the Directory if it should be re-indexed as defined by assumeChangesCouldHappen
	 */
	void index()
	{
		index(assumeChangesCouldHappen);
	}

	/**
	 * Index the Directory
	 *
	 * @param override Directory will be indexed if true or not already indexed
	 */
	void index(boolean override)
	{
		if(alreadyIndexed)
		{
			if(! override)
			{
				//System.out.println("ALREADY INDEXED " + thisDirectory.getName());
				return;
			}
			alreadyIndexed = false;
		}
		numFiles = 0;
		numFolders = 0;
		foundExcludedFiles = new ArrayList<>();
		contents = new ArrayList<>();

		File[] localContents = thisDirectory.listFiles();
		for(File i : (localContents != null) ? localContents : new File[0])
		{
			if(i.isDirectory())
			{
				numFolders++;
				Directory tempDir = new Directory(assumeChangesCouldHappen, i, excludedFiles.toArray(new String[excludedFiles.size()]), verbose);
				tempDir.index();
				numFiles += tempDir.getNumFiles();
				numFolders += tempDir.getNumFolders();
				totalSize += tempDir.getSize();
				contents.add(tempDir);
			}
			else if(i.isFile())
			{
				// TODO deal with passing excluded files up the chain
				Record tempRec = new Record(i);
				totalSize += tempRec.getSize();
				if(! excludedFiles.contains(tempRec.getName()))
				{
					if(verbose)
					{
						System.out.println("File added: " + tempRec.getName());
					}
					contents.add(tempRec);
					numFiles++;
				}
				else
				{
					numExcludedFound++;
					foundExcludedFiles.add(new Record(i));
				}
			}
		}
		alreadyIndexed = true;
	}

	@Override
	public long getSize()
	{
		index();
		return totalSize;
	}

	/**
	 * @return a Record if this is a Record or an empty Record if this is not a Record
	 */
	@Override
	public Record asRecord()
	{
		return new Record();
	}

	@Override
	public Directory asDirectory()
	{
		return this;
	}

	@Override
	public boolean hasNoContents()
	{
		return hasNoContents;
	}

	@Override
	public String getName()
	{
		return thisDirectory.getName();
	}

	public int getNumFiles()
	{
		index();
		return numFiles;
	}

	public int getNumFolders()
	{
		index();
		return numFolders;
	}

	public int getNumExcludedFound()
	{
		index();
		return numExcludedFound;
	}

	public Record getLargestFile()
	{
		index();
		return getLargestFile(this.asDirectory(), new Record());
	}

	private Record getLargestFile(Directory in, Record mostRecentLargest)
	{
		if(verbose)
		{
			System.out.println("Digging down into " + in.getName());
		}
		Record currentLargestFile = mostRecentLargest;
		for(Entry i : in.getContents())
		{
			if(i instanceof Directory)
			{
				if(verbose && currentLargestFile.hasNoContents())
				{
					System.out.println("Sent a getLargest() with no contents from " + thisDirectory.getName() + " to " + i.getName());
				}
				Record largestInDirectory = getLargestFile(i.asDirectory(), currentLargestFile);
				if(verbose)
				{
					System.out.println(largestInDirectory.getName() + ": " + largestInDirectory.getSize() + " is a directory");
				}
				if(currentLargestFile.hasNoContents())
				{
					//System.out.println("Directory came in with no contents");
					currentLargestFile = largestInDirectory;
				}
				else if(currentLargestFile.getSize() < largestInDirectory.getSize())
				{
					if(verbose)
					{
						System.out.println("currentLargestFile was " + currentLargestFile.getName() + ": " + currentLargestFile.getSize()
								+ " but is now " + largestInDirectory.getName() + ": " + largestInDirectory.getSize());
					}
					currentLargestFile = largestInDirectory;
				}
			}
			else if(i instanceof Record)
			{
				if(verbose && currentLargestFile.hasNoContents())
				{
					System.out.println("Sent a getLargest() with no contents from " + thisDirectory.getName() + " to " + i.getName());
				}
				if(verbose)
				{
					System.out.println(i.getName() + ": " + i.getSize() + " is a record");
				}
				if(currentLargestFile.hasNoContents())
				{
					//System.out.println("File has no contents");
					currentLargestFile = i.asRecord();
					//System.out.println("CurrentLargestFile is now " + currentLargestFile.getName());
				}
				else if(currentLargestFile.getSize() < i.getSize())
				{
					if(verbose)
					{
						System.out.println("currentLargestFile was " + currentLargestFile.getName() + ": " + currentLargestFile.getSize()
								+ " but is now " + i.getName() + ": " + i.getSize());
					}
					currentLargestFile = i.asRecord();
				}
			}
		}
		if(verbose)
		{
			System.out.println("Digging out of " + in.getName());
		}
		return currentLargestFile;
	}

	public long getLargestFileSize()
	{
		index();
		return getLargestFile().getSize();
	}

	private ArrayList<Entry> getContents()
	{
		return this.contents;
	}
}