package com.example.jipark.tasklock_app.task;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.jipark.tasklock_app.R;

import java.util.List;

/**
 * Created by jipark on 10/5/17.
 */

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.MyViewHolder> {
    private List<Task> taskList;
    private TasksAdapterCallback mAdapterCallback;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mTask;
        public ImageButton mRemoveButton;

        public MyViewHolder(View view) {
            super(view);
            mTask = view.findViewById(R.id.task);
            mRemoveButton = view.findViewById(R.id.delete_task);
        }
    }

    public TasksAdapter(List<Task> taskList, Context context) {
        this.taskList = taskList;
        try {
            this.mAdapterCallback = ((TasksAdapterCallback) context);
        }
        catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement AdapterCallback.");
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_list_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        Task task = taskList.get(position);
        holder.mTask.setText(task.getTask());

        holder.mRemoveButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                taskList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, taskList.size());
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

    public static interface TasksAdapterCallback {
        void onMethodCallback();
    }
}
