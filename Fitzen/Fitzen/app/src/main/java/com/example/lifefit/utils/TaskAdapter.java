package com.example.lifefit.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifefit.R;
import com.example.lifefit.models.TaskModel;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<TaskModel> taskList;
    private OnTaskDeleteListener deleteListener;

    public TaskAdapter(List<TaskModel> taskList, OnTaskDeleteListener deleteListener) {
        this.taskList = taskList;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskModel task = taskList.get(position);
        holder.bind(task);
        holder.deleteButton.setOnClickListener(v -> deleteListener.onTaskDelete(position));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskDescription;
        public Button deleteButton;

        public TaskViewHolder(View itemView) {
            super(itemView);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            deleteButton = itemView.findViewById(R.id.deleteTaskButton);
        }

        public void bind(TaskModel task) {
            taskDescription.setText(task.getPrefix() + " " + task.getDescription());
        }
    }

    public interface OnTaskDeleteListener {
        void onTaskDelete(int position);
    }
}
