package com.example.lifefit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.lifefit.databinding.ActivityMainBinding;
import com.example.lifefit.databinding.ActivitySignInBinding;
import com.example.lifefit.fragments.DailyFragment;
import com.example.lifefit.fragments.ExerciseFragment;
import com.example.lifefit.fragments.ProfileFragment;
import com.example.lifefit.fragments.SummaryFragment;
import com.example.lifefit.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private UserModel user;
    FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        EdgeToEdge.enable(this);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        Bundle extras = getIntent().getExtras();
        Fragment fragment = new SummaryFragment();
//        fragment.setArguments(extras);
        callFragment(fragment);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.page_1) {
                callFragment(new SummaryFragment());
                return true;
            }

            if (itemId == R.id.page_2) {
                callFragment(new DailyFragment());
                return true;
            }

            if (itemId == R.id.page_3) {
                callFragment(new ExerciseFragment());
                return true;
            }

            if (itemId == R.id.page_4) {
                callFragment(new ProfileFragment());
                return true;
            }

            return false;
        });
    }

    private void callFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(binding.frameLayout.getId(), fragment, null);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    }

    private void redirect(Context packageContext, Class<?> cls) {
        Intent intent = new Intent(packageContext, cls);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            redirect(this, SignInActivity.class);
        }
    }

    private void fetchUserData(String uid) {
        firebaseFirestore.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        user = documentSnapshot.toObject(UserModel.class);
                        if (user != null) {
//                            binding.textView.setText("Hoş geldin, " + user.getFirstName() + "!");
                        }
                    } else {
                        mAuth.signOut();
                        redirect(MainActivity.this, SignInActivity.class);
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FIREBASE_SERVER", "Error fetching user data: ", e);
                    redirect(MainActivity.this, SignInActivity.class);
                });
    }
}