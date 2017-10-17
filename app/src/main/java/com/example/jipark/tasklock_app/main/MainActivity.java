package com.example.jipark.tasklock_app.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.jipark.tasklock_app.R;
import com.example.jipark.tasklock_app.Utils;
import com.example.jipark.tasklock_app.app_manager.AppManagerActivity;
import com.example.jipark.tasklock_app.iris.IrisActivity;
import com.example.jipark.tasklock_app.lock.LockActivity;
import com.example.jipark.tasklock_app.task.TaskActivity;

//TODO: lock feature, add more lock screen things like time, phone
//give app the same id, set up rule for firebase, just to give permission to write to the database

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
    }

    public void launchLockActivity(View view) {
        if(SINGLETON.getTaskList().isEmpty()) {
            Toast.makeText(this, "No tasks are available to start!", Toast.LENGTH_SHORT).show();
        }
        else {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            if(SINGLETON.getTaskCount() == 1) {
                alertDialog.setTitle("You have " + SINGLETON.getTaskCount() + " task.");
            }
            else {
                alertDialog.setTitle("You have " + SINGLETON.getTaskCount() + " tasks.");
            }
            alertDialog.setMessage("You're selected apps will be disabled.  Are you sure you want to start?");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Confirm",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            Intent intent = new Intent(MainActivity.this, LockActivity.class);
                            startActivity(intent);
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    @Override
    protected void onResume() {
        // Since we might have altered the tasks on resume, load it again.
        super.onResume();
        if (SINGLETON.isFilePresent(this, tasksFileName)) {
            SINGLETON.resetTaskList();
            SINGLETON.loadTasks(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_eye:
                Intent intent = new Intent(MainActivity.this, IrisActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_about:
                Toast.makeText(this, "Coming Soon!", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_settings:
                Toast.makeText(this, "Coming Soon!", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //TODO: add method to send to database that the client disconnected...
    }
}