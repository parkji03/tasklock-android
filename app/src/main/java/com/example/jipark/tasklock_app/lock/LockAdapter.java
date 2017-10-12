package com.example.jipark.tasklock_app.lock;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.jipark.tasklock_app.R;
import com.example.jipark.tasklock_app.task.Task;

import java.util.List;

/**
 * Created by jipark on 10/11/17.
 */

public class LockAdapter extends RecyclerView.Adapter<com.example.jipark.tasklock_app.lock.LockAdapter.MyViewHolder>{
    private List<Task> taskList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public CheckBox mCheckBox;
        public TextView mTask;

        public MyViewHolder(View view) {
            super(view);
            mCheckBox = view.findViewById(R.id.lock_checkbox);
            mTask = view.findViewById(R.id.task);
        }
    }

    public LockAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    @Override
    public com.example.jipark.tasklock_app.lock.LockAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_list_row, parent, false);
        return new com.example.jipark.tasklock_app.lock.LockAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(com.example.jipark.tasklock_app.lock.LockAdapter.MyViewHolder holder, final int position) {
        Task task = taskList.get(position);

        holder.mTask.setText(task.getTask());
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }
}

