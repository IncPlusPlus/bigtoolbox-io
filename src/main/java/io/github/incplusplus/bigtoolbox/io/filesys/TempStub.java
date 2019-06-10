package io.github.incplusplus.bigtoolbox.io.filesys;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TempStub
{
	public static void main(String[] args) throws IOException
	{
		Path startDir = Path.of("C:\\Users\\clohertyr\\OneDrive - Wentworth Institute of Technology\\Documents\\Adobe");
		MyFileVisitor myVisitor = new MyFileVisitor();
		Files.walkFileTree(startDir, myVisitor);
	}
}
