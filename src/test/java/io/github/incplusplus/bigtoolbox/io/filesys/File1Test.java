package io.github.incplusplus.bigtoolbox.io.filesys;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;

public class File1Test
{
	
	@BeforeEach
	public void setUp()
	{
	}
	
	@AfterEach
	public void tearDown()
	{
	}
	
	@Test
	public void isRoot()
	{
	}
	
	@Test
	public void listFiles()
	{
	}
	
	@Test
	public void getSize()
	{
	}
	
	@Test
	public void index()
	{
	}
	
	@Test
	public void runnableIndexerFor()
	{
	}
	
	@Test
	public void getLargestFile()
	{
	}
	
	@Test
	public void iterator()
	{
	}
	
	@Test
	public void getFormattedSize()
	{
	}
	
	@Test
	public void getFormattedSize1()
	{
	}
	
	@Test
	public void getFormattedSize2()
	{
	}
	
	@Test
	public void renderDirectoryTree()
	{
		URL resource = File1Test.class.getResource("/sample-structure");
		File1 f1 = new File1(String.valueOf(resource));
		f1.index();
		System.out.println(f1.renderDirectoryTree());
	}
}