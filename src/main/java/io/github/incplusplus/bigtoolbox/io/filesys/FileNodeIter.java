package io.github.incplusplus.bigtoolbox.io.filesys;

import java.util.Iterator;

public class FileNodeIter<T> implements Iterator<FileNode> {

	enum ProcessStages {
		ProcessParent, ProcessChildCurNode, ProcessChildSubNode
	}

	private FileNode FileNode;

	public FileNodeIter(FileNode FileNode) {
		this.FileNode = FileNode;
		this.doNext = ProcessStages.ProcessParent;
		this.childrenCurNodeIter = FileNode.children.iterator();
	}

	private ProcessStages doNext;
	private FileNode next;
	private Iterator<FileNode> childrenCurNodeIter;
	private Iterator<FileNode> childrenSubNodeIter;

	@Override
	public boolean hasNext() {

		if (this.doNext == ProcessStages.ProcessParent) {
			this.next = this.FileNode;
			this.doNext = ProcessStages.ProcessChildCurNode;
			return true;
		}

		if (this.doNext == ProcessStages.ProcessChildCurNode) {
			if (childrenCurNodeIter.hasNext()) {
				FileNode childDirect = childrenCurNodeIter.next();
				childrenSubNodeIter = childDirect.iterator();
				this.doNext = ProcessStages.ProcessChildSubNode;
				return hasNext();
			}

			else {
				this.doNext = null;
				return false;
			}
		}
		
		if (this.doNext == ProcessStages.ProcessChildSubNode) {
			if (childrenSubNodeIter.hasNext()) {
				this.next = childrenSubNodeIter.next();
				return true;
			}
			else {
				this.next = null;
				this.doNext = ProcessStages.ProcessChildCurNode;
				return hasNext();
			}
		}

		return false;
	}

	@Override
	public FileNode next() {
		return this.next;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
