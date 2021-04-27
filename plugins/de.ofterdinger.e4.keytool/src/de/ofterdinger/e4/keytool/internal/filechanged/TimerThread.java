package de.ofterdinger.e4.keytool.internal.filechanged;

import static de.ofterdinger.e4.keytool.internal.KeytoolPlugin.PLUGIN_ID;
import static de.ofterdinger.e4.keytool.internal.filechanged.TimerTaskClone.CANCELLED;
import static de.ofterdinger.e4.keytool.internal.filechanged.TimerTaskClone.EXECUTED;
import static org.eclipse.core.runtime.IStatus.ERROR;

import de.ofterdinger.e4.keytool.internal.KeytoolPlugin;
import org.eclipse.core.runtime.Status;

class TimerThread extends Thread {
  private static int counter = 0;
  private boolean newTasksMayBeScheduled = true;
  private final TaskQueue queue;

  TimerThread(TaskQueue queue) {
    this.queue = queue;
    if (getPriority() > 1) {
      setPriority(getPriority() - 1);
    }
    setName("TimerThread" + ++counter); 
  }

  public boolean isNewTasksMayBeScheduled() {
    return this.newTasksMayBeScheduled;
  }

  @Override
  public void run() {
    try {
      mainLoop();
    } catch (Exception throwable) {
      TaskQueue taskQueue = this.queue;
      synchronized (taskQueue) {
        this.newTasksMayBeScheduled = false;
        this.queue.clear();
      }
      throw throwable;
    }
    TaskQueue taskQueue = this.queue;
    synchronized (taskQueue) {
      this.newTasksMayBeScheduled = false;
      this.queue.clear();
    }
  }

  public void setNewTasksMayBeScheduled(boolean newTasksMayBeScheduled) {
    this.newTasksMayBeScheduled = newTasksMayBeScheduled;
  }

  private void mainLoop() {
    block8:
    do {
      try {
        do {
          boolean taskFired;
          TimerTaskClone task;
          TaskQueue taskQueue = this.queue;
          synchronized (taskQueue) {
            long currentTime;
            long executionTime;
            while (this.queue.isEmpty() && this.newTasksMayBeScheduled) {
              this.queue.wait();
            }
            if (this.queue.isEmpty()) {
              break block8;
            }
            task = this.queue.getMin();
            Object object = task.getLock();
            synchronized (object) {
              if (task.getState() == CANCELLED) {
                this.queue.removeMin();
                continue;
              }
              currentTime = System.currentTimeMillis();
              executionTime = task.getNextExecutionTime();
              taskFired = executionTime <= currentTime;
              if (taskFired) {
                if (task.getPeriod() == 0) {
                  this.queue.removeMin();
                  task.setState(EXECUTED);
                } else {
                  this.queue.rescheduleMin(
                      task.getPeriod() < 0
                          ? currentTime - task.getPeriod()
                          : executionTime + task.getPeriod());
                }
              }
            }
            if (!taskFired) {
              this.queue.wait(executionTime - currentTime);
            }
          }
          if (!taskFired) {
            continue;
          }
          task.run();
        } while (true);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        KeytoolPlugin.getDefault().getLog().log(new Status(ERROR, PLUGIN_ID, e.getMessage(), e));
      }
    } while (true);
  }
}
