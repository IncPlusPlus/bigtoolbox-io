package io.github.incplusplus.bigtoolbox.io.filesys;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static org.apache.commons.io.FileUtils.sizeOfAsBigInteger;

//TODO: Add a fluid API to manipulate files based on various properties

/**
 * Mostly utility methods to get information. There are plans for a fluid
 * API that will allow for recursive modification to files based on various properties.
 */
public class Files {
	static LoadingCache<String, FileInfo> fileInfoCache = CacheBuilder.newBuilder()
			.expireAfterAccess(10, TimeUnit.MINUTES)
			.build(
					new CacheLoader<String, FileInfo>() {
						public FileInfo load(String key) { // no checked exception
							return generateFileInfo(key);
						}
					});
	public static FileInfo getInfo(String path) throws ExecutionException {
		return fileInfoCache.get(path);
	}
	
	private static FileInfo generateFileInfo(String key) {
		
		return new FileInfo(key).index();
	}
	
	static class FileInfo {
		private final String path;
		
		public String getPath() {
			return path;
		}
		
		public boolean isIndexed() {
			return indexed;
		}
		
		public int getNumChildren() {
			return numChildren;
		}
		
		public int getSize() {
			return size;
		}
		
		private boolean indexed = false;
		private int numChildren;
		private int size;
		
		public FileInfo(String path) {
			this.path = path;
		}
		
		/**
		 * Trigger the indexing process to gather info about
		 * the provided path.
		 *
		 * @return this instance after indexing is complete
		 */
		private FileInfo index() {
			try (Stream<Path> stream = java.nio.file.Files.walk(Paths.get(this.path))) {
				stream.filter(java.nio.file.Files::isRegularFile)
						.forEach(path1 -> {
							try {
								this.size += java.nio.file.Files.size(path1);
							}
							catch (IOException e) {
								this.size += 0;
							}
						});
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			//			File thisFile = new File(path);
			//			if (thisFile.isFile()) {
			//				this.size =
			//			}
			//			else if (thisFile.isDirectory()) {
			//				final File[] files = thisFile.listFiles();
			//				if (files != null) {
			//					this.numChildren = files.length;
			//					for (final File file : files) {
			//						if (!java.nio.file.Files.isSymbolicLink(file.toPath())) {
			//							this.size += file.length();
			//						}
			//					}
			//
			//				}
			//				String[] namesList = thisFile.list();
			//				this.numChildren = isNull(namesList) ? 0 : namesList.length;
			//			}
			//			else {}
			//			this.indexed = true;
			return this;
		}
	}
}
