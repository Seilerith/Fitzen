package com.example.lifefit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lifefit.databinding.ActivitySignInBinding;
import com.example.lifefit.models.UserModel;
import com.example.lifefit.utils.FirebaseUtils;
import com.example.lifefit.utils.Validator;
import com.example.lifefit.utils.ViewHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth.AuthStateListener authListener;
    private UserModel user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(binding.signin, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        if (user == null) {
            detectClick();
            setAllListeners();
        } else {
            redirect(SignInActivity.this, MainActivity.class);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            redirect(this, MainActivity.class);
        }
    }

    private void setAllListeners() {
        binding.passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // pass
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                binding.passwordTextField.setError(null);
                binding.passwordTextField.setErrorEnabled(false);
                binding.passwordTextField.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // pass
            }
        });

        binding.loginButton.setOnClickListener(v -> {
            loginUserAccount();
        });

        binding.resetTextField.setOnClickListener(v -> {
            resetUserPassword();
        });

        binding.resetTextField.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View view, MotionEvent motionEvent) {
                int eventAction = motionEvent.getAction();

                if (eventAction == MotionEvent.ACTION_HOVER_ENTER) {
                    ((TextView) view).setTextColor(Color.parseColor("#135FBD")); // Example color
                    return true;
                }

                if (eventAction == MotionEvent.ACTION_HOVER_EXIT) {
                    ((TextView) view).setTextColor(Color.parseColor("#FFFFFFFF")); // Example color
                    return true;
                }

                return false;
            }
        });
    }

    private String checkInput(boolean skipValidatePassword) {
        String emailAddress = binding.emailInput.getText().toString().trim().toLowerCase(Locale.ROOT);
        String password = binding.passwordInput.getText().toString();

        setAllBoundError();

        boolean isEmpty = false;
        boolean emailValidatorErrorExist = false;
        boolean passwordValidatorErrorExist = false;

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

        // Handle password
        if (!skipValidatePassword) {
            if (password.isEmpty()) {
                isEmpty = true;
                binding.passwordTextField.setError(getString(R.string.password_required));
            } else {
                if (!Validator.isPasswordLengthValid(password)) {
                    passwordValidatorErrorExist = true;
                    binding.passwordTextField.setError(getString(R.string.password_length));
                } else if (!Validator.isPasswordCharacterSetValid(password) || !Validator.doesPasswordContainSpecialCharacter(password)) {
                    passwordValidatorErrorExist = true;
                    binding.passwordTextField.setError(getString(R.string.password_complexity));
                } else if (Validator.doesPasswordContainSpaces(password)) {
                    passwordValidatorErrorExist = true;
                    binding.passwordTextField.setError(getString(R.string.password_no_spaces));
                }
            }
        }

        return (isEmpty || emailValidatorErrorExist || passwordValidatorErrorExist) ? "NO_SKIP" : "SKIP";
    }

    private void resetUserPassword() {
        String emailAddress = binding.emailInput.getText().toString().trim().toLowerCase(Locale.ROOT);

        String InputResult = checkInput(true);
        if (InputResult.equals("NO_SKIP")) return;

        lockLayout();
        binding.registerProgressIndicator.show();

        mAuth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.w("FIREBASE_SERVER", "resetUserPassword:success");

                            AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
                            builder.setTitle("Şifre Sıfırlama");
                            builder.setMessage(getString(R.string.reset_mail_sent, emailAddress));
                            builder.setCancelable(true);
                            builder.setPositiveButton("Tamam", null);
                            builder.show();

                            // Redirect to main activity after 4 seconds
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    unlockLayout();
                                    binding.registerProgressIndicator.hide();
                                }
                            }, 4000);
                        } else {
                            String err = task.getException().getMessage();
                            Log.w("FIREBASE_SERVER", "resetUserPassword:failure:" + err);

                            binding.signInResult.setTextColor(Color.parseColor("#B00020"));
                            binding.signInResult.setText(getString(R.string.internal_error));

                            unlockLayout();
                            binding.registerProgressIndicator.hide();
                        }
                    }
                });
    }

    private void loginUserAccount() {
        String emailAddress = binding.emailInput.getText().toString().trim().toLowerCase(Locale.ROOT);
        String password = binding.passwordInput.getText().toString();

        String InputResult = checkInput(false);
        if (InputResult.equals("NO_SKIP")) return;

        lockLayout();
        binding.registerProgressIndicator.show();
        mAuth.signInWithEmailAndPassword(emailAddress, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.w("FIREBASE_SERVER", "loginUserWithEmail:success");

                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.d("login-info", user.toString());

                            if (user != null) {
                                fetchUserData(user.getUid());
                            }

                            // Redirect to main activity after 4 seconds
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    unlockLayout();
                                    binding.registerProgressIndicator.hide();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 4000);
                        } else {
                            String err = task.getException().getMessage();
                            Log.w("FIREBASE_SERVER", "loginUserWithEmail:failure:" + err);

                            if (err.startsWith("The supplied auth credential is incorrect")) {
                                binding.signInResult.setTextColor(Color.parseColor("#B00020"));
                                binding.signInResult.setText(getString(R.string.invalid_credentials));
                            } else {
                                binding.signInResult.setError(getString(R.string.internal_error));
                                Toast.makeText(SignInActivity.this, err, Toast.LENGTH_SHORT).show();
                            }

                            unlockLayout();
                            binding.registerProgressIndicator.hide();
                        }
                    }
                });
    }

    private void fetchUserData(String uid) {
        firebaseFirestore.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        user = documentSnapshot.toObject(UserModel.class);
                        if (user != null) {
                            binding.signInResult.setTextColor(Color.parseColor("#FFFFFFFF"));
                            binding.signInResult.setText(getString(R.string.sign_up_success, user.getFirstName()));
                        }
                    } else {
                        mAuth.signOut();
                        redirect(SignInActivity.this, MainActivity.class);
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FIREBASE_SERVER", "Error fetching user data: ", e);
                    redirect(SignInActivity.this, MainActivity.class);
                });
    }

    private void emailVisibility(String message) {
        binding.emailTextField.setError(message);
        binding.emailTextField.setEndIconMode(TextInputLayout.END_ICON_NONE);
    }

    private void passwordVisibility(String message) {
        binding.passwordTextField.setError(message);
        binding.passwordTextField.setEndIconMode(TextInputLayout.END_ICON_NONE);
    }

    private void lockLayout() {
        ViewHandler.lockInputLayout(binding.emailTextField);
        ViewHandler.lockInputLayout(binding.passwordTextField);
        ViewHandler.lockInputLayout(binding.registerTextField);
        ViewHandler.lockInputLayout(binding.resetTextField);
        ViewHandler.lockButton(binding.loginButton);
    }

    private void unlockLayout() {
        ViewHandler.unlockInputLayout(binding.passwordTextField);
        ViewHandler.unlockInputLayout(binding.emailTextField);
        ViewHandler.unlockInputLayout(binding.registerTextField);
        ViewHandler.unlockInputLayout(binding.resetTextField);
        ViewHandler.unlockButton(binding.loginButton);
    }

    private void setAllBoundError() {
        binding.emailTextField.setError(null);
        binding.emailTextField.setErrorEnabled(false);

        binding.passwordTextField.setError(null);
        binding.passwordTextField.setErrorEnabled(false);
    }

    private void detectClick() {
        String text = getString(R.string.register_action_text);
        SpannableString spannableString = new SpannableString(text);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        };

        String clickableText = "Kaydol";
        int startIndex = text.indexOf(clickableText);
        int endIndex = startIndex + clickableText.length();
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.registerTextField.setText(spannableString);
        binding.registerTextField.setMovementMethod(LinkMovementMethod.getInstance());
        binding.registerTextField.setHighlightColor(getResources().getColor(android.R.color.transparent));
    }

    private void redirect(Context packageContext, Class<?> cls) {
        Intent intent = new Intent(packageContext, cls);
        startActivity(intent);
        finish();
    }
}
