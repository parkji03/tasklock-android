package com.example.jipark.tasklock_app.app_manager;

import java.io.Serializable;

/**
 * Created by Scott on 10/17/2017.
 */

public class App  implements Serializable{

    private String name;
    private boolean isDisabled;

    public App(String name, boolean isDisabled) {
        this.name = name;
        this.isDisabled = isDisabled;
    }

    // getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public void setDisabled(boolean disabled) { isDisabled = disabled; }
}
