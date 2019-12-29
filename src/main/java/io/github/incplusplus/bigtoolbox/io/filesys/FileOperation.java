package io.github.incplusplus.bigtoolbox.io.filesys;

import java.io.IOException;

public interface FileOperation {
	OperationResult apply(String path) throws IOException;
	
	class OperationResult {
		long filesSeen;
		long filesChanged;
	}
}
