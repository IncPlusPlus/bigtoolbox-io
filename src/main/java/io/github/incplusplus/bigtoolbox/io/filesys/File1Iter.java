package io.github.incplusplus.bigtoolbox.io.filesys;

import java.util.Iterator;

public class File1Iter implements Iterator<File1> {

  enum ProcessStages {
    ProcessParent,
    ProcessChildCurNode,
    ProcessChildSubNode
  }

  private File1 File1;

  public File1Iter(File1 file) {
    this.File1 = file;
    this.doNext = ProcessStages.ProcessParent;
    this.childrenCurNodeIter = file.children.iterator();
  }

  private ProcessStages doNext;
  private File1 next;
  private Iterator<File1> childrenCurNodeIter;
  private Iterator<File1> childrenSubNodeIter;

  @Override
  public boolean hasNext() {

    if (this.doNext == ProcessStages.ProcessParent) {
      this.next = this.File1;
      this.doNext = ProcessStages.ProcessChildCurNode;
      return true;
    }

    if (this.doNext == ProcessStages.ProcessChildCurNode) {
      if (childrenCurNodeIter.hasNext()) {
        File1 childDirect = childrenCurNodeIter.next();
        childrenSubNodeIter = childDirect.iterator();
        this.doNext = ProcessStages.ProcessChildSubNode;
        return hasNext();
      } else {
        this.doNext = null;
        return false;
      }
    }

    if (this.doNext == ProcessStages.ProcessChildSubNode) {
      if (childrenSubNodeIter.hasNext()) {
        this.next = childrenSubNodeIter.next();
        return true;
      } else {
        this.next = null;
        this.doNext = ProcessStages.ProcessChildCurNode;
        return hasNext();
      }
    }

    return false;
  }

  @Override
  public File1 next() {
    return this.next;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
