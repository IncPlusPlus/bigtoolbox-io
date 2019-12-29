package io.github.incplusplus.bigtoolbox.io.filesys;

public class FileOperationBuilder {
	private boolean expectListAfterCompletion;
	
	/**
	 * Whether the {@link FileOperation.OperationResult} returned by {@link FileOperation#apply(String)}
	 * should keep track of which files the operation was applied to and which it was not applied to.
	 * @param expectListAfterCompletion set to true if you intend to use the lists of processed and
	 *                                  unprocessed files. Set to false otherwise to save memory.
	 */
	public void expectListAfterCompletion(boolean expectListAfterCompletion){
		this.expectListAfterCompletion = expectListAfterCompletion;
	}
	
	FileOperation build() {
		return null;
	}
}
