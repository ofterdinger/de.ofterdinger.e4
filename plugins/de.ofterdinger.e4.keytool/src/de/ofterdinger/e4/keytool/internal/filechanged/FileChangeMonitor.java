package de.ofterdinger.e4.keytool.internal.filechanged;

import static de.ofterdinger.e4.keytool.internal.filechanged.FileChangedEvent.FILE_ADDED;
import static de.ofterdinger.e4.keytool.internal.filechanged.FileChangedEvent.FILE_REMOVED;
import static de.ofterdinger.e4.keytool.internal.filechanged.FileChangedEvent.FILE_UPDATED;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileChangeMonitor {
  private static final int DEFAULT_QUIESCENT_SCANS = 2;
  private static final long DEFAULT_SCAN_RATE = 60000;
  private static final String CAN_ONLY_INCLUDE_SUBDIRS_FOR_DIRECTORIES =
      "Can only include subdirs for directories ";
  private static final String IS_A_FILE = " is a file.";
  private int curQuiescentCount;
  private final Set<IFileChangeListener> listeners = new HashSet<>();
  private final List<SubMonitor> monitors = new ArrayList<>();
  private final ArrayList<FileChangedEvent> pendingUpdates = new ArrayList<>();
  private int quiescentScans;
  private long scanRate;
  private TimerClone timer = null;

  public FileChangeMonitor() {
    this(DEFAULT_SCAN_RATE, DEFAULT_QUIESCENT_SCANS);
  }

  public FileChangeMonitor(long theScanRate, int theQuiescentScans) {
    this.scanRate = theScanRate;
    this.quiescentScans = theQuiescentScans;
  }

  public final void addFileChangeListener(IFileChangeListener listener) {
    this.listeners.add(listener);
  }

  public final void addSource(File file) {
    this.addSource(file, false, (FileFilter) null);
  }

  public final void addSource(File file, boolean includeSubDirs, FileFilter filter) {
    if (file.isFile()) {
      if (includeSubDirs) {
        throw new IllegalArgumentException(
            CAN_ONLY_INCLUDE_SUBDIRS_FOR_DIRECTORIES + file.getAbsolutePath() + IS_A_FILE);
      }
      if (filter != null) {
        throw new IllegalArgumentException(
            "FileFilter only allowed for directories " + file.getAbsolutePath() + IS_A_FILE);
      }
      this.monitors.add(new FileMonitor(file));
    } else {
      this.monitors.add(new DirMonitor(file, null, filter, includeSubDirs));
    }
  }

  public final void addSource(File file, boolean includeSubDirs, FilenameFilter filenameFilter) {
    if (file.isFile()) {
      if (includeSubDirs) {
        throw new IllegalArgumentException(
            CAN_ONLY_INCLUDE_SUBDIRS_FOR_DIRECTORIES + file.getAbsolutePath() + IS_A_FILE);
      }
      if (filenameFilter != null) {
        throw new IllegalArgumentException(
            "FilenameFilter only allowed for directories " + file.getAbsolutePath() + IS_A_FILE);
      }
      this.monitors.add(new FileMonitor(file));
    } else {
      this.monitors.add(new DirMonitor(file, filenameFilter, null, includeSubDirs));
    }
  }

  public final void cancel() {
    if (this.timer != null) {
      stopMonitor();
    }
  }

  public final void postFileChangedEvent(FileChangedEvent evt) {
    this.pendingUpdates.add(evt);
    firePendingFileChangedEvents();
  }

  public final boolean removeFileChangeListener(IFileChangeListener listener) {
    return this.listeners.remove(listener);
  }

  public final void setQuiescentScans(int quiescentScans) {
    this.quiescentScans = quiescentScans;
  }

  public final void setScanRate(long scanRate) {
    this.scanRate = scanRate;
  }

  public final void startMonitor() {
    this.curQuiescentCount = 0;
    for (SubMonitor monitor : this.monitors) {
      monitor.gatherTimestamps();
    }
    this.timer = new TimerClone(true);
    this.timer.schedule(
        new TimerTaskClone() {

          @Override
          public void run() {
            FileChangeMonitor.this.monitor();
          }
        },
        this.scanRate,
        this.scanRate);
  }

  public final void stopMonitor() {
    firePendingFileChangedEvents();
    this.pendingUpdates.clear();
    if (this.timer != null) {
      this.timer.cancel();
      this.timer = null;
    }
  }

  @Override
  protected final void finalize() throws Throwable {
    cancel();
    super.finalize();
  }

  protected final void monitor() {
    int currentPending = this.pendingUpdates.size();
    for (Runnable monitor : this.monitors) {
      monitor.run();
    }
    if (currentPending != this.pendingUpdates.size()) {
      this.curQuiescentCount = 0;
    }
    ++this.curQuiescentCount;
    if (this.curQuiescentCount >= this.quiescentScans) {
      firePendingFileChangedEvents();
    }
  }

  private final void firePendingFileChangedEvents() {
    if (!this.pendingUpdates.isEmpty()) {
      for (IFileChangeListener listener : this.listeners) {
        listener.fileChanged(
            this.pendingUpdates.toArray(new FileChangedEvent[this.pendingUpdates.size()]));
      }
      this.pendingUpdates.clear();
    }
  }

  class DirMonitor implements SubMonitor {
    private final File dir;
    private boolean exists;
    private final FileFilter fileFilter;
    private final FilenameFilter filenameFilter;
    private final Map<File, SubMonitor> fileToMonitorMap;
    private final boolean includeSubDirs;

    public DirMonitor(
        File theDir,
        FilenameFilter theFilenameFilter,
        FileFilter theFileFilter,
        boolean theIncludeSubDirs) {
      this.fileToMonitorMap = new HashMap<>();
      this.dir = theDir;
      this.filenameFilter = theFilenameFilter;
      this.fileFilter = theFileFilter;
      this.includeSubDirs = theIncludeSubDirs;
      if (this.dir.exists() && !this.dir.isDirectory()) {
        throw new IllegalArgumentException(
            "DirMonitor can only run on directories " + theDir.getAbsolutePath());
      }
    }

    @Override
    public void gatherTimestamps() {
      this.fileToMonitorMap.clear();
      this.exists = this.dir.exists();
      if (!this.exists) {
        return;
      }
      File[] files = getFiles();
      int i = 0;
      while (i < files.length) {
        SubMonitor subMonitor = addSubMonitor(files[i]);
        if (subMonitor != null) {
          subMonitor.gatherTimestamps();
        }
        ++i;
      }
    }

    @Override
    public void run() {
      boolean currentExists = this.dir.exists();
      if (currentExists != this.exists) {
        this.exists = currentExists;
        if (currentExists) {
          postFileChangedEvent(new FileChangedEvent(FILE_ADDED, this.dir.getAbsolutePath()));
          gatherTimestamps();
        } else {
          postFileChangedEvent(new FileChangedEvent(FILE_REMOVED, this.dir.getAbsolutePath()));
        }
        return;
      }
      if (!currentExists) {
        return;
      }
      File[] files = getFiles();
      int i = 0;
      while (i < files.length) {
        SubMonitor subMonitor;
        if ((!files[i].isDirectory() || this.includeSubDirs)
            && this.fileToMonitorMap.get(files[i]) == null) {
          postFileChangedEvent(new FileChangedEvent(FILE_ADDED, files[i].getAbsolutePath()));
          subMonitor = addSubMonitor(files[i]);
          if (subMonitor != null) {
            subMonitor.gatherTimestamps();
          }
        }
        ++i;
      }
      for (Runnable subMonitor : this.fileToMonitorMap.values()) {
        subMonitor.run();
      }
    }

    private SubMonitor addSubMonitor(File file) {
      SubMonitor retVal = null;
      if (file.isFile()) {
        retVal = new FileMonitor(file);
      } else if (this.includeSubDirs) {
        retVal = new DirMonitor(file, this.filenameFilter, this.fileFilter, this.includeSubDirs);
      }
      if (retVal != null) {
        this.fileToMonitorMap.put(file, retVal);
      }
      return retVal;
    }

    private File[] getFiles() {
      if (this.filenameFilter != null) return this.dir.listFiles(this.filenameFilter);
      return this.fileFilter != null ? this.dir.listFiles(this.fileFilter) : this.dir.listFiles();
    }
  }

  class FileMonitor implements SubMonitor {
    private boolean exists;
    private final File file;
    private long lastModified;

    public FileMonitor(File f) {
      this.file = f;
      if (this.file.isDirectory()) {
        throw new IllegalArgumentException("FileMonitor can only monitor files.");
      }
    }

    @Override
    public void gatherTimestamps() {
      this.exists = this.file.exists();
      if (this.exists) {
        this.lastModified = this.file.lastModified();
      }
    }

    @Override
    public void run() {
      boolean currentExists = this.file.exists();
      if (currentExists != this.exists) {
        this.exists = currentExists;
        if (!currentExists) {
          postFileChangedEvent(new FileChangedEvent(FILE_REMOVED, this.file.getAbsolutePath()));
          return;
        }
        this.lastModified = this.file.lastModified();
        postFileChangedEvent(new FileChangedEvent(FILE_ADDED, this.file.getAbsolutePath()));
        return;
      }
      if (!this.exists) {
        return;
      }
      long curLastModified = this.file.lastModified();
      if (this.lastModified != curLastModified) {
        this.lastModified = curLastModified;
        postFileChangedEvent(new FileChangedEvent(FILE_UPDATED, this.file.getAbsolutePath()));
      }
    }
  }

  interface SubMonitor extends Runnable {
    void gatherTimestamps();
  }
}
