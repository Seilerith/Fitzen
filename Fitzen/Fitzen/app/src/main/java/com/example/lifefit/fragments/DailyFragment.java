package com.example.lifefit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lifefit.R;
import com.example.lifefit.databinding.FragmentDailyBinding;
import com.example.lifefit.models.TaskModel;
import com.example.lifefit.utils.TaskAdapter;
import com.example.lifefit.utils.ViewHandler;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DailyFragment extends Fragment implements TaskAdapter.OnTaskDeleteListener {
    private FragmentDailyBinding binding;
    private List<TaskModel> taskList;
    private TaskAdapter taskAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String taskLoadError;
    private String addTaskFail;
    private String deleteTaskSuccess;
    private String deleteTaskFail;
    private static final String USERS_COLLECTION = "users";
    private static final String TASKS_COLLECTION = "tasks";
    private static final String PREFIX_FIELD = "prefix";
    private static final String DESCRIPTION_FIELD = "description";

    public DailyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        taskLoadError = getString(R.string.add_task_fail);
        addTaskFail = getString(R.string.add_task_fail);
        deleteTaskSuccess = getString(R.string.delete_task_success);
        deleteTaskFail = getString(R.string.delete_task_fail);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDailyBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList, this);

        RecyclerView recyclerView = binding.tasksRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(taskAdapter);

        Button addTaskButton = binding.addTaskButton;
        TextInputEditText newTaskInput = binding.newTaskInput;
        TextView emptyTextView = binding.emptyTextView;

        addTaskButton.setOnClickListener(v -> {
            String taskDescription = newTaskInput.getText().toString().trim();
            if (!taskDescription.isEmpty()) {
                String prefix = String.valueOf(taskList.size() + 1) + ".";
                TaskModel newTask = new TaskModel(prefix, taskDescription);
                taskList.add(newTask);
                taskAdapter.notifyItemInserted(taskList.size() - 1);
                newTaskInput.setText("");
                emptyTextView.setVisibility(taskList.isEmpty() ? View.VISIBLE : View.GONE);
                addTaskToFirestore(newTask, mAuth.getUid());
                recyclerView.scrollToPosition(taskList.size() - 1);
            }
        });

        loadTasksFromFirestore();
        return view;
    }

    private void loadTasksFromFirestore() {
        String userId = mAuth.getUid();

        db.collection(USERS_COLLECTION).document(userId).collection(TASKS_COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        taskList.clear();
                        int id = 1;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String prefix = document.getString(PREFIX_FIELD);
                            String description = document.getString(DESCRIPTION_FIELD);
                            TaskModel taskModel = new TaskModel(String.format("%s.", id), description);
                            taskList.add(taskModel);
                            id++;
                        }
                        taskAdapter.notifyDataSetChanged();
                        binding.emptyTextView.setVisibility(taskList.isEmpty() ? View.VISIBLE : View.GONE);
                    } else {
                        Snackbar.make(binding.getRoot(), taskLoadError, Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void addTaskToFirestore(TaskModel task, String userId) {
        String taskId = db.collection(USERS_COLLECTION).document(userId).collection(TASKS_COLLECTION).document().getId();

        Map<String, Object> taskData = new HashMap<>();
        taskData.put(PREFIX_FIELD, task.getPrefix());
        taskData.put(DESCRIPTION_FIELD, task.getDescription());

        db.collection(USERS_COLLECTION).document(userId).collection(TASKS_COLLECTION).document(taskId)
                .set(taskData)
                .addOnSuccessListener(documentReference -> {})
                .addOnFailureListener(e -> {
                    Snackbar.make(binding.getRoot(), addTaskFail, Snackbar.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onTaskDelete(int position) {
        lockLayout();

        TaskModel taskToDelete = taskList.get(position);
        String userId = mAuth.getUid();

        db.collection(USERS_COLLECTION).document(userId).collection(TASKS_COLLECTION)
                .whereEqualTo(DESCRIPTION_FIELD, taskToDelete.getDescription())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        if (position >= 0 && position < taskList.size()) {
                                            taskList.remove(position);
                                            taskAdapter.notifyItemRemoved(position);
                                        }
                                        binding.emptyTextView.setVisibility(taskList.isEmpty() ? View.VISIBLE : View.GONE);
                                        loadTasksFromFirestore(); // -> no need for delay
                                        unlockLayout();
                                        Snackbar.make(binding.getRoot(), deleteTaskSuccess, Snackbar.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Snackbar.make(binding.getRoot(), deleteTaskFail, Snackbar.LENGTH_SHORT).show();
                                        unlockLayout();
                                    });
                        }
                    }
                });
    }

    private void lockLayout() {
        ViewHandler.lockInputLayout(binding.newTaskInput);
        ViewHandler.lockButton(binding.addTaskButton);

        for (int i = 0; i < binding.tasksRecyclerView.getChildCount(); i++) {
            View itemView = binding.tasksRecyclerView.getChildAt(i);
            TaskAdapter.TaskViewHolder taskViewHolder = (TaskAdapter.TaskViewHolder) binding.tasksRecyclerView.getChildViewHolder(itemView);
            ViewHandler.lockButton(taskViewHolder.deleteButton);
        }
    }

    private void unlockLayout() {
        ViewHandler.unlockInputLayout(binding.newTaskInput);
        ViewHandler.unlockButton(binding.addTaskButton);

        for (int i = 0; i < binding.tasksRecyclerView.getChildCount(); i++) {
            View itemView = binding.tasksRecyclerView.getChildAt(i);
            TaskAdapter.TaskViewHolder taskViewHolder = (TaskAdapter.TaskViewHolder) binding.tasksRecyclerView.getChildViewHolder(itemView);
            ViewHandler.unlockButton(taskViewHolder.deleteButton);
        }
    }
}
