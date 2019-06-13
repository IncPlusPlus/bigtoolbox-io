package io.github.incplusplus.bigtoolbox.io.filesys;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;

public class File1Test
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
		URL resource = File1Test.class.getResource("/sample-structure");
		File1 f1 = new File1(String.valueOf(resource));
		f1.index();
		System.out.println(f1.renderDirectoryTree());
	}
}