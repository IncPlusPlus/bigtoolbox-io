package io.github.incplusplus.bigtoolbox.io.filesys;

import com.google.common.base.Stopwatch;
import com.tree.TreeNode;

import java.io.IOException;

public class TempStub
{
	public static void main(String[] args) throws IOException
	{
		String pathString = "C:\\Users\\clohertyr\\OneDrive - Wentworth Institute of Technology\\Documents";
//		System.out.println(new java.io.File(pathString).length());
//		Path startDir = Path.of(pathString);
//		Stream<Path> pathStream = Files.walk(startDir);
		File1 file = new File1(pathString);
		Directory d = new Directory(pathString);
		Stopwatch sw;
//		DirectoryStream directoryStream = java.nio.file.Files.newDirectoryStream(startDir);
//		System.out.println("Size with streams: " + file.getSize());
//		System.out.println("Size according to my library: " + d.getSize());

//		FileVisitor myVisitor = new FileVisitor();
//		Files.walkFileTree(startDir, myVisitor);
		
		
		
		
//		System.out.println("Set pathString");
//		System.out.println("Set Path named startDir");
//		Stream<Path> pathStream = Files.walk(startDir);
//		System.out.println("Made file");
//		File2 f2 = new File2(pathString);
//		System.out.println("Made Directory");
		System.out.println("Starting!");
		
		
		sw = Stopwatch.createStarted();
		file.index();
		sw.stop();
		System.out.println("Time with File1: " + sw.toString());
		System.out.println("Size according to File1: " + file.getSize());
		
		sw = Stopwatch.createStarted();
		d.getSize();
		sw.stop();
		System.out.println("Time with my library: " + sw.toString());
		System.out.println("Size according to my library: " + d.getSize());
		
		sw = Stopwatch.createStarted();
//		f2.index();
		TreeNode.main(new String[]{pathString});
		sw.stop();
		System.out.println("Time with Tree: " + sw.toString());
//		System.out.println("Size according to Tree: " + f2.getSize());
	}
}
