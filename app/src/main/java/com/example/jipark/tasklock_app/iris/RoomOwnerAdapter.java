package com.example.jipark.tasklock_app.iris;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.jipark.tasklock_app.R;
import com.example.jipark.tasklock_app.task.Task;

import java.util.List;

/**
 * Created by jipark on 10/19/17.
 */

public class RoomOwnerAdapter extends RecyclerView.Adapter<com.example.jipark.tasklock_app.iris.RoomOwnerAdapter.MyViewHolder>{
    private List<Task> taskList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public CheckBox mCheckBox;
        public TextView mTask;

        public MyViewHolder(View view) {
            super(view);
            mCheckBox = view.findViewById(R.id.received_task_check_box);
            mTask = view.findViewById(R.id.received_task_text);
            this.setIsRecyclable(false);
        }
    }

    public RoomOwnerAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.received_list_row, parent, false);
        return new com.example.jipark.tasklock_app.iris.RoomOwnerAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.mCheckBox.setChecked(task.isComplete());
        holder.mTask.setText(task.getTask());

    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }
}
