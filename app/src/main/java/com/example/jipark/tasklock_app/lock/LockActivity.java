package com.example.jipark.tasklock_app.lock;

import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.example.jipark.tasklock_app.R;
import com.example.jipark.tasklock_app.Utils;

public class LockActivity extends AppCompatActivity implements LockAdapter.LockAdapterCallback {
    private Utils SINGLETON;
    private EditText mEditText;
    private LockAdapter mAdapter;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        SINGLETON = Utils.getInstance();
        initRecyclerView();
        initQuickAddEditView();
        initFloatingActionButton();
    }

    @Override
    public void onMethodCallback() {
        SINGLETON.saveTasks(this);
        if (SINGLETON.checkTasksAllTrue()) {
            int completedTaskCount = SINGLETON.getTaskCount();
            SINGLETON.getTaskList().clear();
            SINGLETON.saveTasks(LockActivity.this);

            AlertDialog alertDialog = new AlertDialog.Builder(LockActivity.this).create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setCancelable(false);

            if(completedTaskCount == 1) {
                alertDialog.setTitle("You completed " + completedTaskCount + " task.");
            }
            else {
                alertDialog.setTitle("You completed " + completedTaskCount + " tasks.");
            }

            alertDialog.setMessage("Congrats! You finished!");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Dismiss",
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

    private boolean initQuickAddEditView() {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 8, 16, 8);
        mEditText = new EditText(this);
        mEditText.setLayoutParams(params);
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

    @Override
    public void onBackPressed() {
        int remainingCount = SINGLETON.getTasksRemaining();

        AlertDialog alertDialog = new AlertDialog.Builder(LockActivity.this).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);

        if(remainingCount == 1) {
            alertDialog.setTitle("You have " + remainingCount + " task remaining.");
        }
        else {
            alertDialog.setTitle("You have " + remainingCount + " tasks remaining.");
        }

        alertDialog.setMessage("Are you sure you want to go back?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
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

    public boolean initFloatingActionButton() {
        FloatingActionButton mFAB = (FloatingActionButton)findViewById(R.id.lock_float_action);
        final AlertDialog alertDialog = new AlertDialog.Builder(LockActivity.this).create();
        alertDialog.setTitle("Quick Add");
        alertDialog.setView(mEditText, 80, 0, 80, 0);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //add to list and notify adapter
                dialogInterface.dismiss();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.show();
            }
        });
        return true;
    }
}
