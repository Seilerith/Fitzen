package com.example.lifefit.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lifefit.MainActivity;
import com.example.lifefit.R;
import com.example.lifefit.SignInActivity;
import com.example.lifefit.databinding.FragmentSummaryBinding;
import com.example.lifefit.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SummaryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SummaryFragment extends Fragment {
    FragmentSummaryBinding binding;
    private int calorieCount = 0;
    private int dailyCount = 0;
    private int stepCount = 0;
    private int sleepCount = 0;
    private int exerciseCount = 0;
    private int VKICount = 0;

    private ProgressBar
            progressBarCalorie,
            progressBarDaily,
            progressBarStep,
            progressBarSleep,
            progressBarExercise,
            progressBarVKI;
    private TextView
            progressTextCalorie,
            progressTextDaily,
            progressTextStep,
            progressTextSleep,
            progressTextExercise,
            progressTextVKI;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnableCalorie,
            runnableDaily,
            runnableStep,
            runnableSleep,
            runnableExercise,
            runnableVKI;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private UserModel user;

    public SummaryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = binding.inflate(inflater, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

//      Bundle userData = getActivity().getIntent().getExtras();
//      binding.fullname.setText(userData.get("firstName") + " " + userData.get("lastName"));

        if (currentUser != null) {
            firebaseFirestore.collection("users")
                    .document(mAuth.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            user = documentSnapshot.toObject(UserModel.class);
                            if (user != null) {
                                binding.fullname.setText("Merhaba, " + user.getFirstName() + "!");
                                binding.username2.setText(".................................................");
                            }
                        } else {
                            mAuth.signOut();
                            redirect(SummaryFragment.this.getContext(), SignInActivity.class);
                        }
                    }).addOnFailureListener(e -> {
//                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("FIREBASE_SERVER", "Error fetching user data: ", e);
//                        redirect(MainActivity.this, SignInActivity.class);
                    });

            fakeAllProgress();
            startTaskCalorie();
            startTaskDaily();
            startTaskStep();
            startTaskSleep();
            startTaskExercise();
            startTaskVKI();

            // Inflate the layout for this fragment
        } else {
            redirect(SummaryFragment.this.getContext(), SignInActivity.class);
        }
        return binding.getRoot();
    }


    private void redirect(Context packageContext, Class<?> cls) {
        Intent intent = new Intent(packageContext, cls);
        startActivity(intent);
    }


    private void fakeAllProgress() {
        progressBarCalorie = binding.progressBarCalories;
        progressTextCalorie = binding.progressTextCalories;
        progressBarCalorie.setMax(1000);

        progressBarDaily = binding.progressBarCalendaries;
        progressTextDaily = binding.progressTextCalendaries;
        progressBarDaily.setMax(30);

        progressBarStep = binding.progressBarStepCount;
        progressTextStep = binding.progressTextStepCount;
        progressBarStep.setMax(6000);

        progressBarSleep = binding.progressBarSleptTime;
        progressTextSleep = binding.progressTextSleptTime;
        progressBarSleep.setMax(500);

        progressBarExercise = binding.progressBarFitnessAct;
        progressTextExercise = binding.progressTextFitnessAct;
        progressBarExercise.setMax(10);

        progressBarVKI = binding.progressBarVKI;
        progressTextVKI = binding.progressTextVKI;
        progressBarVKI.setMax(40);
    }

    private void startTaskCalorie() {
        runnableCalorie = new Runnable() {
            @Override
            public void run() {
                if (calorieCount <= progressBarCalorie.getMax()) {
                    progressTextCalorie.setText(String.valueOf(calorieCount));
                    progressBarCalorie.setProgress(calorieCount);

                    calorieCount++;
                        handler.postDelayed(this, 200);
                } else {
                    stopTask(runnableCalorie);
                }
            }
        };

        handler.post(runnableCalorie);
    }

    private void startTaskDaily() {
        runnableDaily = new Runnable() {
            @Override
            public void run() {
                if (dailyCount <= progressBarDaily.getMax()) {
                    progressTextDaily.setText(String.valueOf(dailyCount));
                    progressBarDaily.setProgress(dailyCount);

                    dailyCount++;
                    handler.postDelayed(this, 10000);
                } else {
                    stopTask(runnableDaily);
                }
            }
        };

        handler.post(runnableDaily);
    }

    private void startTaskStep() {
        runnableStep = new Runnable() {
            @Override
            public void run() {
                if (stepCount <= progressBarStep.getMax()) {
                    progressTextStep.setText(String.valueOf(stepCount));
                    progressBarStep.setProgress(stepCount);

                    stepCount++;
                    handler.postDelayed(this, 200);
                } else {
                    stopTask(runnableStep);
                }
            }
        };

        handler.post(runnableStep);
    }

    private void startTaskSleep() {
        runnableSleep = new Runnable() {
            @Override
            public void run() {
                if (sleepCount <= progressBarSleep.getMax()) {
                    progressTextSleep.setText(String.valueOf(sleepCount));
                    progressBarSleep.setProgress(sleepCount);

                    sleepCount++;
                    handler.postDelayed(this, 350);
                } else {
                    stopTask(runnableSleep);
                }
            }
        };

        handler.post(runnableSleep);
    }

    private void startTaskExercise() {

        runnableExercise = new Runnable() {
            @Override
            public void run() {
                if (exerciseCount <= progressBarExercise.getMax()) {
                    progressTextExercise.setText(String.valueOf(exerciseCount));
                    progressBarExercise.setProgress(exerciseCount);

                    exerciseCount++;
                    handler.postDelayed(this, 3000);
                } else {
                    stopTask(runnableExercise);
                }
            }
        };

        handler.post(runnableExercise);
    }

    private void startTaskVKI() {
        runnableVKI = new Runnable() {
            @Override
            public void run() {
                if (VKICount <= progressBarVKI.getMax()) {
                    progressTextVKI.setText(String.valueOf(VKICount));
                    progressBarVKI.setProgress(VKICount);

                    VKICount++;
                    handler.postDelayed(this, 200);
                } else {
                    stopTask(runnableVKI);
                }
            }
        };

        handler.post(runnableVKI);
    }

    private void stopTask(Runnable runnable) {
        // Stop the task
        handler.removeCallbacks(runnable);
    }
}