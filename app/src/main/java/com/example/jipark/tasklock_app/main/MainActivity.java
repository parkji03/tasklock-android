package com.example.jipark.tasklock_app.main;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.jipark.tasklock_app.R;
import com.example.jipark.tasklock_app.app_manager.AppManagerActivity;
import com.example.jipark.tasklock_app.lock.LockActivity;
import com.example.jipark.tasklock_app.task.Task;
import com.example.jipark.tasklock_app.task.TaskActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<Task> taskList = new ArrayList<>();
    private String tasksFileName = "tasks.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //read tasks
        if (isFilePresent(this, tasksFileName)) {
            loadTasks(this, tasksFileName);
        }
    }

    public void launchTaskActivity(View view) {
        Intent intent = new Intent(MainActivity.this, TaskActivity.class);
        intent.putExtra("myTasks", (Serializable)taskList);
        startActivity(intent);
    }

    public void launchAppManagerActivity(View view) {
        Intent intent = new Intent(MainActivity.this, AppManagerActivity.class);
        startActivity(intent);
    }

    public void launchLockActivity(View view) {
        Intent intent = new Intent(MainActivity.this, LockActivity.class);
        intent.putExtra("myTasks", (Serializable)taskList);
        startActivity(intent);
    }

    private void loadTasks(Context context, String fileName) {
        try {
            FileInputStream fis = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            JSONObject jsonTaskListHolder = new JSONObject(sb.toString());
            JSONArray jsonTaskList = jsonTaskListHolder.getJSONArray("tasks");
            JSONObject jsonTask;
            String taskText;
            boolean isComplete;

            for (int i = 0; i < jsonTaskList.length(); i++) {
                jsonTask = jsonTaskList.getJSONObject(i);
                taskText = jsonTask.getString("task");
                isComplete = jsonTask.getBoolean("complete");

                taskList.add(new Task(taskText, isComplete));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch (FileNotFoundException fileNotFound) {
            fileNotFound.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private boolean isFilePresent(Context context, String fileName) {
        String path = context.getFilesDir().getAbsolutePath() + "/" + fileName;
        File file = new File(path);
        return file.exists();
    }

    @Override
    protected void onResume() {
        //Since we might have altered the tasks on resume, load it again
        super.onResume();
        if (isFilePresent(this, tasksFileName)) {
            taskList = new ArrayList<>();
            loadTasks(this, tasksFileName);
        }
    }
}