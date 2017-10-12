package com.example.jipark.tasklock_app.lock;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.example.jipark.tasklock_app.R;
import com.example.jipark.tasklock_app.task.Task;

import java.util.ArrayList;
import java.util.List;

public class LockActivity extends AppCompatActivity {
    private List<Task> taskList = new ArrayList<>();
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
    }

//    private void init

    /* TODO:    always have this display on
       TODO:    show a list of tasks
       TODO:    have check mark boxes to each task
       TODO:    on completion, all done, return to main activity
            check with a loop every time the user checks a check mark, check how many is left, and if 0, we're done


    */



}
