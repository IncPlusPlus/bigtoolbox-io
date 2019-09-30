package io.github.incplusplus.bigtoolbox.io.filesys;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * This class allows for creation of a temporary file.
 * <br><br>
 * Note: A good portion of this code came from
 * <a href="https://stackoverflow.com/a/600198/1687436">this SO answer</a>.
 */
public class TempFile
{
	private URI jarURI;
	private URI fileURI;

	/**
	 * Creates a temporary file that is disposed of upon exit.
	 * The file must be a resource in the project. It will be extracted
	 * from the jar to a temporary directory.
	 *
	 * @param fileName      The name of the resource without the extension
	 * @param fileExtension The file extension (without the dot)
	 */
	public TempFile(String fileName, String fileExtension)
	{
		try
		{
			jarURI = getJarURI();
			fileURI = getFile(jarURI, fileName, fileExtension);
		}
		catch(URISyntaxException e)
		{
			e.printStackTrace();
		}
		catch(ZipException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Returns the path to the folder
	 * the temp file was created in.
	 *
	 * @return The path to the created temp file
	 */
	public File getContainingFolder()
	{
		return new File(fileURI).getParentFile();
	}

	/**
	 * Returns the created temp file as a File
	 * @return Get the created temp file as a File
	 */
	public File getAsFile()
	{
		return new File(fileURI);
	}

	/**
	 * @return Returns the full name of the created temp file
	 */
	public String toString()
	{
		return new File(fileURI).getName();
	}

	private static URI getJarURI()
			throws URISyntaxException
	{
		final ProtectionDomain domain;
		final CodeSource source;
		final URL url;
		final URI uri;

		domain = TempFile.class.getProtectionDomain();
		source = domain.getCodeSource();
		url = source.getLocation();
		uri = url.toURI();

		return (uri);
	}

	private static URI getFile(final URI where,
	                           final String fileName, String fileExtension)
			throws ZipException,
			IOException
	{
		final File location;
		final URI fileURI;

		location = new File(where);

		// not in a JAR, just return the path on disk
		if(location.isDirectory())
		{
			fileURI = URI.create(where.toString() + fileName+"."+fileExtension);
		}
		else
		{
			final ZipFile zipFile;

			zipFile = new ZipFile(location);

			try
			{
				fileURI = extract(zipFile, fileName, fileExtension);
			}
			finally
			{
				zipFile.close();
			}
		}

		return (fileURI);
	}

	private static URI extract(final ZipFile zipFile,
	                           final String fileName, final String fileExtension)
			throws IOException
	{
		final File tempFile;
		final ZipEntry entry;
		final InputStream zipStream;
		OutputStream fileStream;

		tempFile = File.createTempFile(fileName+System.currentTimeMillis(), "."+fileExtension);
		tempFile.deleteOnExit();
		entry = zipFile.getEntry(fileName+"."+fileExtension);

		if(entry == null)
		{
			throw new FileNotFoundException("cannot find file: " + fileName+"."+fileExtension + " in archive: " + zipFile.getName());
		}

		zipStream = zipFile.getInputStream(entry);
		fileStream = null;

		try
		{
			final byte[] buf;
			int i;

			fileStream = new FileOutputStream(tempFile);
			buf = new byte[1024];
			i = 0;

			while((i = zipStream.read(buf)) != - 1)
			{
				fileStream.write(buf, 0, i);
			}
		}
		finally
		{
			close(zipStream);
			close(fileStream);
		}

		return (tempFile.toURI());
	}

	private static void close(final Closeable stream)
	{
		if(stream != null)
		{
			try
			{
				stream.close();
			}
			catch(final IOException ex)
			{
				ex.printStackTrace();
			}
		}
	}
}
