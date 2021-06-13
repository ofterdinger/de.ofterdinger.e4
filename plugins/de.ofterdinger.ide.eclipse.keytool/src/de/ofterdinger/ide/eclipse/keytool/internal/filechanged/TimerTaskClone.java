package de.ofterdinger.ide.eclipse.keytool.internal.filechanged;

abstract class TimerTaskClone implements Runnable {
  public static final int SCHEDULED = 1;
  public static final int VIRGIN = 0;
  static final int CANCELLED = 3;
  static final int EXECUTED = 2;
  private final Object lock = new Object();
  private long nextExecutionTime;
  private long period = 0;
  private int state = VIRGIN;

  protected TimerTaskClone() {}

  public boolean cancel() {
    Object object = this.lock;
    synchronized (object) {
      boolean result = this.state == SCHEDULED;
      this.state = CANCELLED;
      return result;
    }
  }

  public Object getLock() {
    return this.lock;
  }

  public long getNextExecutionTime() {
    return this.nextExecutionTime;
  }

  public long getPeriod() {
    return this.period;
  }

  public int getState() {
    return this.state;
  }

  public long scheduledExecutionTime() {
    Object object = this.lock;
    synchronized (object) {
      return this.period < 0
          ? this.nextExecutionTime + this.period
          : this.nextExecutionTime - this.period;
    }
  }

  public void setNextExecutionTime(long nextExecutionTime) {
    this.nextExecutionTime = nextExecutionTime;
  }

  public void setPeriod(long period) {
    this.period = period;
  }

  public void setState(int state) {
    this.state = state;
  }
}
