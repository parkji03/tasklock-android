package com.example.jipark.tasklock_app;

/**
 * Created by jipark on 10/4/17.
 */

public class Task {
    private String task;
    private boolean isComplete;

    public Task(String task) {
        this.task = task;
        isComplete = false;
    }

    public boolean markedAsComplete() {
        return false;
    }

    // getters and setters
    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }
}
