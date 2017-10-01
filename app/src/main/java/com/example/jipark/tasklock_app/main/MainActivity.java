package com.example.jipark.tasklock_app.main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.example.jipark.tasklock_app.R;
import com.example.jipark.tasklock_app.app_manager.AppManagerActivity;
import com.example.jipark.tasklock_app.lock.LockActivity;
import com.example.jipark.tasklock_app.task.TaskActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void launchTaskActivity() {
        Intent intent = new Intent(this, TaskActivity.class);
        startActivity(intent);
    }

    public void launchAppManagerActivity() {
        Intent intent = new Intent(this, AppManagerActivity.class);
        startActivity(intent);
    }

    public void launchLockActivity() {
        Intent intent = new Intent(this, LockActivity.class);
        startActivity(intent);
    }
}