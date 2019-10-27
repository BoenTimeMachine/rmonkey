package com.demo.rmonkey.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Debouncer<T> {
    private final ScheduledExecutorService sched = Executors.newScheduledThreadPool(1);
    private final ConcurrentHashMap<T, TimerTask> delayedMap = new ConcurrentHashMap<T, TimerTask>();
    private final Callback<T> callback;
    private final int interval;

    public interface Callback<T> {
        void call(T param);
    }

    public Debouncer(Callback<T> c, int interval) {
        this.callback = c;
        this.interval = interval;
    }

    public void call(T param) {
        TimerTask task = new TimerTask(param);

        TimerTask prev;
        do {
            prev = delayedMap.putIfAbsent(param, task);
            if (prev == null)
                sched.schedule(task, interval, TimeUnit.MILLISECONDS);
        }
        while (prev != null && !prev.extend()); // Exit only if new task was added to map, or existing task was extended successfully
    }

    public void terminate() {
        sched.shutdownNow();
    }

    // The task that wakes up when the wait time elapses
    private class TimerTask implements Runnable {
        private final T param;
        private long dueTime;
        private final Object lock = new Object();

        public TimerTask(T param) {
            this.param = param;
            extend();
        }

        public boolean extend() {
            synchronized (lock) {
                if (dueTime < 0) // Task has been shutdown
                    return false;
                dueTime = System.currentTimeMillis() + interval;
                return true;
            }
        }

        public void run() {
            synchronized (lock) {
                long remaining = dueTime - System.currentTimeMillis();
                if (remaining > 0) { // Re-schedule task
                    sched.schedule(this, remaining, TimeUnit.MILLISECONDS);
                } else { // Mark as terminated and invoke callback
                    dueTime = -1;
                    try {
                        callback.call(param);
                    } finally {
                        delayedMap.remove(param);
                    }
                }
            }
        }
    }
}
