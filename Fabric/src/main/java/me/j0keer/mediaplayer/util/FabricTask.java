package me.j0keer.mediaplayer.util;

public interface FabricTask {
    /**
     * Returns true if the Task is a sync task.
     *
     * @return true if the task is run by main thread
     */
    public boolean isSync();

    /**
     * Returns true if this task has been cancelled.
     *
     * @return true if the task has been cancelled
     */
    public boolean isCancelled();

    /**
     * Will attempt to cancel this task.
     */
    public void cancel();
}