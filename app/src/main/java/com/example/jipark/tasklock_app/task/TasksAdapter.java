package com.example.jipark.tasklock_app.task;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jipark.tasklock_app.R;
import com.example.jipark.tasklock_app.Task;

import java.util.List;

/**
 * Created by jipark on 10/5/17.
 */

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.MyViewHolder> {
    private List<Task> taskList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView task;

        public MyViewHolder(View view) {
            super(view);
            task = (TextView)view.findViewById(R.id.task);
        }
    }

    public TasksAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.task.setText(task.getTask());
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }
}
