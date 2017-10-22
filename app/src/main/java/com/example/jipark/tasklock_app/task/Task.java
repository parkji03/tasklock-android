package com.example.jipark.tasklock_app.task;

import java.io.Serializable;

/**
 * Created by jipark on 10/4/17.
 */

public class Task implements Serializable{
    public String task;
    public boolean complete;

    public Task(String task, boolean isComplete) {
        this.task = task;
        this.complete = isComplete;
    }

    // getters and setters
    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}
