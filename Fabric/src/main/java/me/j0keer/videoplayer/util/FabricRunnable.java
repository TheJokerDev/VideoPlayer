package me.j0keer.videoplayer.util;

import java.util.Timer;
import java.util.TimerTask;

public abstract class FabricRunnable implements FabricTask {
    public abstract void run();

    private TimerTask task;
    private boolean isSync = true;

    public FabricTask runTaskLater(long delay) {
        if (delay == 0)
            delay = 1;

        Timer timer = new Timer();
        timer.schedule(
                task = new TimerTask() {
                    @Override
                    public void run() {
                        FabricRunnable.this.run();
                    }
                }
                , delay * 50);
        return this;
    }

    public FabricTask runTask() {
        run();
        return this;
    }

    public FabricTask runTaskAsync() {
        new Thread(this::run).start();
        isSync = false;
        return this;
    }

    public FabricRunnable runTaskTimer(long delay, long period) {
        Timer timer = new Timer();
        timer.schedule(
                task = new TimerTask() {
                    @Override
                    public void run() {
                        FabricRunnable.this.run();
                    }
                }
                , delay * 50, period * 50);
        return this;
    }

    public FabricTask runTaskTimerAsynchronously(long delay, long period) {
        Thread thread = new Thread(() -> {
            Timer timer = new Timer();
            timer.schedule(
                    task = new TimerTask() {
                        @Override
                        public void run() {
                            FabricRunnable.this.run();
                        }
                    }
                    , delay * 50, period * 50);
        });
        thread.start();
        isSync = false;
        return this;
    }

    public void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    @Override
    public boolean isSync() {
        return isSync;
    }

    public boolean isCancelled() {
        return task == null;
    }
}