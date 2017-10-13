package com.example.jipark.tasklock_app.lock;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.jipark.tasklock_app.R;
import com.example.jipark.tasklock_app.Utils;
import com.example.jipark.tasklock_app.task.Task;

import java.util.ArrayList;
import java.util.List;

public class LockActivity extends AppCompatActivity {
    private List<Task> taskList = new ArrayList<>();
    private Utils SINGLETON;
    private RecyclerView mRecyclerView;
    private LockAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        SINGLETON = Utils.getInstance();
        taskList = SINGLETON.getTaskList();
        initRecyclerView();
    }

    private boolean initRecyclerView() {
        mRecyclerView = (RecyclerView)findViewById(R.id.lock_task_list);
        mAdapter = new LockAdapter(taskList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        return true;
    }


    /* TODO:    always have this display on
       TODO:    show a list of tasks
       TODO:    have check mark boxes to each task
       TODO:    save tasks to "tasks.json" if checked...
       TODO:    on completion, all done, return to main activity
            check with a loop every time the user checks a check mark, check how many is left, and if 0, we're done


    */



}
