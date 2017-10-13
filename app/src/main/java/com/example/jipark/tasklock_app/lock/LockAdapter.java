package com.example.jipark.tasklock_app.lock;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.jipark.tasklock_app.R;
import com.example.jipark.tasklock_app.task.Task;

import java.util.List;

/**
 * Created by jipark on 10/11/17.
 */

public class LockAdapter extends RecyclerView.Adapter<com.example.jipark.tasklock_app.lock.LockAdapter.MyViewHolder>{
    private List<Task> taskList;
    private LockAdapterCallback mAdapterCallback;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public CheckBox mCheckBox;
        public TextView mTask;

        public MyViewHolder(View view) {
            super(view);
            mCheckBox = view.findViewById(R.id.lock_checkbox);
            mTask = view.findViewById(R.id.lock_task);
        }
    }

    public LockAdapter(List<Task> taskList, Context context) {
        this.taskList = taskList;
        try {
            this.mAdapterCallback = ((LockAdapterCallback) context);
        }
        catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement AdapterCallback.");
        }
    }

    @Override
    public com.example.jipark.tasklock_app.lock.LockAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lock_list_row, parent, false);
        return new com.example.jipark.tasklock_app.lock.LockAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(com.example.jipark.tasklock_app.lock.LockAdapter.MyViewHolder holder, final int position) {
        Task task = taskList.get(position);
        holder.mCheckBox.setChecked(task.isComplete());
        holder.mTask.setText(task.getTask());

        holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                taskList.get(position).setComplete(b);
                try {
                    mAdapterCallback.onMethodCallback();
                }
                catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static interface LockAdapterCallback {
        void onMethodCallback();
    }
}

