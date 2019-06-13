package io.github.incplusplus.bigtoolbox.io.filesys;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

class File1Test
{
	
	@BeforeEach
	void setUp()
	{
	}
	
	@AfterEach
	void tearDown()
	{
	}
	
	@Test
	void isRoot()
	{
	}
	
	@Test
	void listFiles()
	{
	}
	
	@Test
	void getSize()
	{
	}
	
	@Test
	void index()
	{
	}
	
	@Test
	void runnableIndexerFor()
	{
	}
	
	@Test
	void getLargestFile()
	{
	}
	
	@Test
	void iterator()
	{
	}
	
	@Test
	void getFormattedSize()
	{
	}
	
	@Test
	void getFormattedSize1()
	{
	}
	
	@Test
	void getFormattedSize2()
	{
	}
	
	@Test
	void renderDirectoryTree()
	{
		try
		{
			URL resourceLoc = File1Test.class.getResource("/sample-structure");
			String resourcePath = new File(resourceLoc.toURI()).getAbsolutePath();
			File1 f1 = new File1(resourcePath);
//			f1.index();
			System.out.println(f1.renderDirectoryTree());
			assertEquals("sample-structure\n" +
					"├── A\n" +
					"│   ├── A\n" +
					"│   │   └── A.txt\n" +
					"│   ├── A.txt\n" +
					"│   └── B\n" +
					"│       └── B.txt\n" +
					"└── B\n" +
					"    └── B.txt\n", f1.renderDirectoryTree());
		}
		catch (URISyntaxException e)
		{
			e.printStackTrace();
		}
	}
}