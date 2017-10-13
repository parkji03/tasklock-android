package com.example.jipark.tasklock_app.main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.jipark.tasklock_app.R;
import com.example.jipark.tasklock_app.Utils;
import com.example.jipark.tasklock_app.app_manager.AppManagerActivity;
import com.example.jipark.tasklock_app.lock.LockActivity;
import com.example.jipark.tasklock_app.task.TaskActivity;


public class MainActivity extends AppCompatActivity {
    private String tasksFileName = "tasks.json";
    private Utils SINGLETON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SINGLETON = Utils.getInstance();

        if (SINGLETON.isFilePresent(this, tasksFileName)) {
            SINGLETON.loadTasks(this);
        }
    }

    public void launchTaskActivity(View view) {
        Intent intent = new Intent(MainActivity.this, TaskActivity.class);
        startActivity(intent);
    }

    public void launchAppManagerActivity(View view) {
        Intent intent = new Intent(MainActivity.this, AppManagerActivity.class);
        startActivity(intent);
//        Toast.makeText(this, "Coming soon!", Toast.LENGTH_LONG).show();
    }

    public void launchLockActivity(View view) {
        Intent intent = new Intent(MainActivity.this, LockActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        //Since we might have altered the tasks on resume, load it again
        super.onResume();
        if (SINGLETON.isFilePresent(this, tasksFileName)) {
            SINGLETON.resetTaskList();
            SINGLETON.loadTasks(this);
        }
    }
}