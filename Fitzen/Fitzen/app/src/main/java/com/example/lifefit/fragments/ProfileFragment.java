package com.example.lifefit.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.lifefit.R;
import com.example.lifefit.SignInActivity;
import com.example.lifefit.databinding.FragmentProfileBinding;
import com.example.lifefit.utils.Validator;
import com.example.lifefit.utils.ViewHandler;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment {
    FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        binding.logoutButton.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                lockLayout();

                Snackbar.make(binding.getRoot(), "Çıkış yapılıyor, tekrar görüşmek üzere!", Snackbar.LENGTH_SHORT).show();

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    mAuth.signOut();
                    Intent intent = new Intent(ProfileFragment.this.getContext(), SignInActivity.class);
                    startActivity(intent);
                }, 3000);
            }
        });

        binding.deleteButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Hesabı Sil");
            builder.setMessage("Hesabınızı silmek istediğinizden emin misiniz?");
            builder.setPositiveButton("Evet", (dialog, which) -> {
                deleteUserData();
            });
            builder.setNegativeButton("Hayır", (dialog, which) -> {
                dialog.dismiss();
            });
            builder.show();
        });

        binding.updateButton.setOnClickListener(v -> {
            checkInput();
        });

//        ViewHandler.lockButton(binding.deleteButton);

        loadData();
        return binding.getRoot();
    }

    private void deleteUserData() {
        lockLayout();
        binding.registerProgressIndicator.show();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            firebaseFirestore.collection("users").document(user.getUid())
                    .delete()
                    .addOnSuccessListener(a -> {
                        deleteAccount(user);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Kullanıcı verileri silinemedi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void deleteAccount(FirebaseUser user) {
        user.delete()
                .addOnSuccessListener(aVoid -> {
                    Snackbar.make(binding.getRoot(), "Hesap başarıyla silindi", Snackbar.LENGTH_SHORT).show();

                    Context context = binding.getRoot().getContext();
                    Intent intent = new Intent(context, SignInActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(binding.getRoot(), "Hesap silinemedi: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                });
    }


    private void loadData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            firebaseFirestore.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> userData = documentSnapshot.getData();
                            if (userData != null) {
                                binding.firstNameInput.setText((String) userData.get("firstName"));
                                binding.lastNameInput.setText((String) userData.get("lastName"));
                                binding.emailInput.setText((String) userData.get("email"));
                                binding.weightInput.setText((String) userData.get("weight"));
                                binding.sizeInput.setText((String) userData.get("size"));
                                // You can add more fields here if needed
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Veri alınırken hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void checkInput() {
        String firstName = binding.firstNameInput.getText().toString().trim();
        String lastName = binding.lastNameInput.getText().toString().trim();
        String emailAddress = binding.emailInput.getText().toString().trim().toLowerCase(Locale.ROOT);
        String weight = binding.weightInput.getText().toString();
        String size = binding.sizeInput.getText().toString();

        setAllBoundError();

        boolean isEmpty = false;
        boolean nameValidatorErrorExist = false;
        boolean emailValidatorErrorExist = false;

        // Handle first name
        if (firstName.isEmpty()) {
            isEmpty = true;
            binding.firstNameTextField.setError(getString(R.string.name_required, "ad"));
        } else {
            if (!Validator.isNameValid(firstName)) {
                nameValidatorErrorExist = true;
                binding.firstNameTextField.setError(getString(R.string.name_alpha, "Ad"));
            }
        }

        // Handle last name
        if (lastName.isEmpty()) {
            isEmpty = true;
            binding.lastNameTextField.setError(getString(R.string.name_required, "soyadı"));
        } else {
            if (!Validator.isNameValid(lastName)) {
                nameValidatorErrorExist = true;
                binding.lastNameTextField.setError(getString(R.string.name_alpha, "Soyadı"));
            }
        }

        // Handle email address
        if (emailAddress.isEmpty()) {
            isEmpty = true;
            binding.emailTextField.setError(getString(R.string.email_required));
        } else {
            if (!Validator.isEmailValid(emailAddress)) {
                emailValidatorErrorExist = true;
                binding.emailTextField.setError(getString(R.string.email_invalid));
            }
        }

        // Handle weight
        if (weight.isEmpty()) {
            isEmpty = true;
            binding.weightTextField.setError(getString(R.string.weight_required));
        }

        // Handle size
        if (size.isEmpty()) {
            isEmpty = true;
            binding.sizeTextField.setError(getString(R.string.size_required));
        }

        if (isEmpty || nameValidatorErrorExist || emailValidatorErrorExist) {
            return;
        }

        lockLayout();
        binding.registerProgressIndicator.show();

        updateData(firstName, lastName, emailAddress, weight, size);
    }

    private void setAllBoundError() {
        binding.firstNameTextField.setError(null);
        binding.firstNameTextField.setErrorEnabled(false);

        binding.lastNameTextField.setError(null);
        binding.lastNameTextField.setErrorEnabled(false);

        binding.emailTextField.setError(null);
        binding.emailTextField.setErrorEnabled(false);

        binding.weightTextField.setError(null);
        binding.weightTextField.setErrorEnabled(false);

        binding.sizeTextField.setError(null);
        binding.sizeTextField.setErrorEnabled(false);
    }

    private void lockLayout() {
        ViewHandler.lockInputLayout(binding.firstNameTextField);
        ViewHandler.lockInputLayout(binding.lastNameTextField);
        ViewHandler.lockInputLayout(binding.emailTextField);
        ViewHandler.lockInputLayout(binding.weightTextField);
        ViewHandler.lockInputLayout(binding.sizeTextField);
        ViewHandler.lockButton(binding.updateButton);
        ViewHandler.lockButton(binding.logoutButton);
    }

    private void unlockLayout() {
        ViewHandler.unlockInputLayout(binding.firstNameTextField);
        ViewHandler.unlockInputLayout(binding.lastNameTextField);
        ViewHandler.unlockInputLayout(binding.emailTextField);
        ViewHandler.unlockInputLayout(binding.weightTextField);
        ViewHandler.unlockInputLayout(binding.sizeTextField);
        ViewHandler.unlockButton(binding.updateButton);
        ViewHandler.unlockButton(binding.logoutButton);
    }

    private void updateData(String firstName, String lastName, String email, String weight, String size) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Kullanıcı oturumu açık değil.", Toast.LENGTH_SHORT).show();
            unlockLayout();
            binding.registerProgressIndicator.hide();
            return;
        }

        firebaseFirestore.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> existingData = documentSnapshot.getData();
                        if (existingData != null) {
                            existingData.put("firstName", firstName);
                            existingData.put("lastName", lastName);
                            existingData.put("email", email);
                            existingData.put("weight", weight);
                            existingData.put("size", size);

                            firebaseFirestore.collection("users").document(user.getUid())
                                    .set(existingData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Bilgiler güncellendi.", Toast.LENGTH_SHORT).show();
                                        unlockLayout();
                                        binding.registerProgressIndicator.hide();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Güncelleme başarısız: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        unlockLayout();
                                        binding.registerProgressIndicator.hide();
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Veri alınırken hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    unlockLayout();
                    binding.registerProgressIndicator.hide();
                });
    }
}
