package com.example.jipark.tasklock_app.lock;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;
import android.view.WindowManager;

import com.example.jipark.tasklock_app.R;
import com.example.jipark.tasklock_app.Utils;

public class LockActivity extends AppCompatActivity implements LockAdapter.LockAdapterCallback {
    private Utils SINGLETON;
    private RecyclerView mRecyclerView;
    private LockAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        SINGLETON = Utils.getInstance();
        initRecyclerView();
    }

    @Override
    public void onMethodCallback() {
        SINGLETON.saveTasks(this);
        if (checkTasksAllTrue()) {
            int completedTaskCount = SINGLETON.getTaskCount();
            SINGLETON.getTaskList().clear();
            SINGLETON.saveTasks(LockActivity.this);

            AlertDialog alertDialog = new AlertDialog.Builder(LockActivity.this).create();

            if(completedTaskCount == 1) {
                alertDialog.setTitle("You completed " + completedTaskCount + " task.");
            }
            else {
                alertDialog.setTitle("You completed " + completedTaskCount + " tasks.");
            }

            alertDialog.setMessage("Congrats! You finished!");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Return",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            finish();
                        }
                    });
            alertDialog.show();

        }
    }

    private boolean initRecyclerView() {
        mRecyclerView = (RecyclerView)findViewById(R.id.lock_task_list);
        mAdapter = new LockAdapter(SINGLETON.getTaskList(), this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        return true;
    }

    private boolean checkTasksAllTrue() {
        for (int i = 0; i < SINGLETON.getTaskList().size(); i++) {
            if(!SINGLETON.getTaskList().get(i).isComplete()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }
}
