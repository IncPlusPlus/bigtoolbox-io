package io.github.incplusplus.bigtoolbox.io.filesys;

import java.util.concurrent.ExecutionException;

public class FilesExample {
	static Files.FileInfo info;
	
	static {
		try {
			info = Files.getInfo("/home/ryan/IdeaProjects/btb/bigtoolbox-io/src/main/java/");
		}
		catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		System.out.println("Done indexing " + info.getPath());
		System.out.println("Size: " + info.getSize());
	}
}
