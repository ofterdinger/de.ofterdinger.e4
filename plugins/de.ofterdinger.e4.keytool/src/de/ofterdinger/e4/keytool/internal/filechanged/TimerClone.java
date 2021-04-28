package de.ofterdinger.e4.keytool.internal.filechanged;

import static de.ofterdinger.e4.keytool.internal.filechanged.TimerTaskClone.SCHEDULED;
import static de.ofterdinger.e4.keytool.internal.filechanged.TimerTaskClone.VIRGIN;

import java.util.Date;

class TimerClone {
  private static final String NEGATIVE_DELAY = "Negative delay.";
  private static final String NON_POSITIVE_PERIOD = "Non-positive period.";
  final TaskQueue queue = new TaskQueue();
  final TimerThread thread = new TimerThread(this.queue);
  private final Object threadReaper =
      new Object() {
        @Override
        protected void finalize() throws Throwable {
          TaskQueue taskQueue = TimerClone.this.queue;
          synchronized (taskQueue) {
            TimerClone.this.thread.setNewTasksMayBeScheduled(false);
            TimerClone.this.queue.notifyAll();
          }
        }
      };

  public TimerClone() {
    this.thread.start();
  }

  public TimerClone(boolean isDaemon) {
    this.thread.setDaemon(isDaemon);
    this.thread.start();
  }

  public void cancel() {
    TaskQueue taskQueue = this.queue;
    synchronized (taskQueue) {
      this.thread.setNewTasksMayBeScheduled(false);
      this.queue.clear();
      this.queue.notifyAll();
      this.threadReaper.toString();
    }
  }

  public void schedule(TimerTaskClone task, Date time) {
    sched(task, time.getTime(), 0);
  }

  public void schedule(TimerTaskClone task, Date firstTime, long period) {
    if (period <= 0) {
      throw new IllegalArgumentException(NON_POSITIVE_PERIOD);
    }
    sched(task, firstTime.getTime(), -period);
  }

  public void schedule(TimerTaskClone task, long delay) {
    if (delay < 0) {
      throw new IllegalArgumentException(NEGATIVE_DELAY);
    }
    sched(task, System.currentTimeMillis() + delay, 0);
  }

  public void schedule(TimerTaskClone task, long delay, long period) {
    if (delay < 0) {
      throw new IllegalArgumentException(NEGATIVE_DELAY);
    }
    if (period <= 0) {
      throw new IllegalArgumentException(NON_POSITIVE_PERIOD);
    }
    sched(task, System.currentTimeMillis() + delay, -period);
  }

  public void scheduleAtFixedRate(TimerTaskClone task, Date firstTime, long period) {
    if (period <= 0) {
      throw new IllegalArgumentException(NON_POSITIVE_PERIOD);
    }
    sched(task, firstTime.getTime(), period);
  }

  public void scheduleAtFixedRate(TimerTaskClone task, long delay, long period) {
    if (delay < 0) {
      throw new IllegalArgumentException(NEGATIVE_DELAY);
    }
    if (period <= 0) {
      throw new IllegalArgumentException(NON_POSITIVE_PERIOD);
    }
    sched(task, System.currentTimeMillis() + delay, period);
  }

  private void sched(TimerTaskClone task, long time, long period) {
    if (time < 0) {
      throw new IllegalArgumentException("Illegal execution time.");
    }
    TaskQueue taskQueue = this.queue;
    synchronized (taskQueue) {
      if (!this.thread.isNewTasksMayBeScheduled()) {
        throw new IllegalStateException("Timer already cancelled.");
      }
      Object object = task.getLock();
      synchronized (object) {
        if (task.getState() != VIRGIN) {
          throw new IllegalStateException("Task already scheduled or cancelled");
        }
        task.setNextExecutionTime(time);
        task.setPeriod(period);
        task.setState(SCHEDULED);
      }
      this.queue.add(task);
      if (this.queue.getMin() == task) {
        this.queue.notifyAll();
      }
    }
  }
}
