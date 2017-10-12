package com.example.jipark.tasklock_app.task;

import java.io.Serializable;

/**
 * Created by jipark on 10/4/17.
 */

public class Task implements Serializable{
    private String task;
    private boolean isComplete;

    public Task(String task, boolean isComplete) {
        this.task = task;
        this.isComplete = isComplete;
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
