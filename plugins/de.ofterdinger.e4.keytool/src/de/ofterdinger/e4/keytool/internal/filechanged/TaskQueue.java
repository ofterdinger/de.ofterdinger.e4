package de.ofterdinger.e4.keytool.internal.filechanged;

class TaskQueue {
	private static final int NO_OF_CLONES = 128;
	private TimerTaskClone[] queue = new TimerTaskClone[NO_OF_CLONES];
	private int size = 0;

	TaskQueue() {
	}

	void add(TimerTaskClone task) {
		if (++this.size == this.queue.length) {
			TimerTaskClone[] newQueue = new TimerTaskClone[2 * this.queue.length];
			System.arraycopy(this.queue, 0, newQueue, 0, this.size);
			this.queue = newQueue;
		}
		this.queue[this.size] = task;
		fixUp(this.size);
	}

	void clear() {
		int i = 1;
		while (i <= this.size) {
			this.queue[i] = null;
			++i;
		}
		this.size = 0;
	}

	TimerTaskClone getMin() {
		return this.queue[1];
	}

	boolean isEmpty() {
		return (this.size == 0);
	}

	void removeMin() {
		this.queue[1] = this.queue[this.size];
		this.queue[this.size--] = null;
		fixDown(1);
	}

	void rescheduleMin(long newTime) {
		this.queue[1].setNextExecutionTime(newTime);
		fixDown(1);
	}

	private void fixDown(int k) {
		int j;
		while ((j = k << 1) <= this.size) {
			if (j < this.size && this.queue[j].getNextExecutionTime() > this.queue[j + 1].getNextExecutionTime()) {
				++j;
			}
			if (this.queue[k].getNextExecutionTime() <= this.queue[j].getNextExecutionTime()) {
				break;
			}
			TimerTaskClone tmp = this.queue[j];
			this.queue[j] = this.queue[k];
			this.queue[k] = tmp;
			k = j;
		}
	}

	private void fixUp(int k) {
		while (k > 1) {
			int j = k >> 1;
			if (this.queue[j].getNextExecutionTime() <= this.queue[k].getNextExecutionTime()) {
				break;
			}
			TimerTaskClone tmp = this.queue[j];
			this.queue[j] = this.queue[k];
			this.queue[k] = tmp;
			k = j;
		}
	}
}
