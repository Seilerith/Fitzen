package com.example.lifefit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lifefit.utils.Validator;
import com.example.lifefit.utils.ViewHandler;
import com.example.lifefit.utils.AdvancedDate;
import com.example.lifefit.models.UserModel;
import com.example.lifefit.databinding.ActivitySignUpBinding;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        if (user == null) {
            binding = ActivitySignUpBinding.inflate(getLayoutInflater());
            View view = binding.getRoot();
            setContentView(view);

            detectClick();
            setAllListeners();
            handleDatePicker();

            EdgeToEdge.enable(this);
            ViewCompat.setOnApplyWindowInsetsListener(binding.signup, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        } else {
            redirect(SignUpActivity.this, MainActivity.class);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
           redirect(this, MainActivity.class);
        }
    }

    public void handleDatePicker() {
        // Get current date
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        int minimumAge = 12;
        c.set(Calendar.YEAR, mYear - minimumAge);

        binding.datePicker.setMinDate(0); // from Jan 1 1970
        binding.datePicker.setMaxDate(c.getTimeInMillis());
        binding.datePicker.updateDate(c.get(Calendar.YEAR) - 1, mMonth, mDay );
    }

    private void setAllListeners() {
        binding.passwordTextField.setEndIconOnClickListener(view -> {
            int inputType = binding.passwordInput.getInputType();

            if (inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                binding.passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.confirmPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                binding.passwordInput.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                binding.confirmPasswordInput.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
        });
        binding.passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // pass
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                binding.passwordTextField.setError(null);
                binding.confirmPasswordTextField.setErrorEnabled(false);
                binding.passwordTextField.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // pass
            }
        });

        binding.confirmPasswordTextField.setEndIconOnClickListener(view -> {
            int inputType = binding.confirmPasswordInput.getInputType();

            if (inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                binding.passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                binding.confirmPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                binding.passwordInput.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                binding.confirmPasswordInput.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            }
        });
        binding.confirmPasswordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // pass
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                binding.confirmPasswordTextField.setError(null);
                binding.confirmPasswordTextField.setErrorEnabled(false);
                binding.confirmPasswordTextField.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // pass
            }
        });

        binding.registerButton.setOnClickListener(v -> {
            signUpNewUser();
        });
    }

    private void signUpNewUser() {
        String firstName = binding.firstNameInput.getText().toString().trim();
        String lastName = binding.lastNameInput.getText().toString().trim();
        String emailAddress = binding.emailInput.getText().toString().trim().toLowerCase(Locale.ROOT);
        String password = binding.passwordInput.getText().toString();
        String confirmPassword = binding.confirmPasswordInput.getText().toString();

        setAllBoundError();

        boolean isEmpty = false;
        boolean isPasswordOk = false;
        boolean matchPasswordError = false;

        boolean nameValidatorErrorExist = false;
        final boolean usernameValidatorErrorExist = false;
        boolean emailValidatorErrorExist = false;
        boolean passwordValidatorErrorExist = false;
        boolean confirmPasswordValidatorErrorExist = false;

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

        // Handle password
        if (password.isEmpty()) {
            isEmpty = true;
            binding.passwordTextField.setError(getString(R.string.password_required));
        } else {
            // Handle password
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

            if (!passwordValidatorErrorExist) {
                isPasswordOk = true;
            }
        }

        // Handle confirm password
        if (confirmPassword.isEmpty()) {
            isEmpty = true;
            binding.confirmPasswordTextField.setError(getString(R.string.confirm_password_required));
        } else {
            if (!isPasswordOk) {
                if (!Validator.isPasswordLengthValid(confirmPassword)) {
                    confirmPasswordValidatorErrorExist = true;
                    binding.confirmPasswordTextField.setError(getString(R.string.password_length));
                } else if (!Validator.isPasswordCharacterSetValid(confirmPassword) || !Validator.doesPasswordContainSpecialCharacter(confirmPassword)) {
                    confirmPasswordValidatorErrorExist = true;
                    binding.confirmPasswordTextField.setError(getString(R.string.password_complexity));
                } else if (Validator.doesPasswordContainSpaces(confirmPassword)) {
                    passwordValidatorErrorExist = true;
                    binding.confirmPasswordTextField.setError(getString(R.string.password_no_spaces));
                }
            }
        }

        // Match password
        if (!passwordValidatorErrorExist && !confirmPasswordValidatorErrorExist && !password.equals(confirmPassword)) {
            matchPasswordError = true;
            String passwordShouldMatchMessage = getString(R.string.password_match);

            binding.passwordTextField.setError(passwordShouldMatchMessage);
            binding.confirmPasswordTextField.setError(passwordShouldMatchMessage);
        }

        if (isEmpty || nameValidatorErrorExist || usernameValidatorErrorExist || emailValidatorErrorExist || passwordValidatorErrorExist || matchPasswordError) {
            return;
        }

        lockLayout();
        binding.registerProgressIndicator.show();
        mAuth.createUserWithEmailAndPassword(emailAddress, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.w("FIREBASE_SERVER", "createUserWithEmail:success");
                            binding.signUpResult.setText(getString(R.string.sign_up_success, firstName));

                            FirebaseUser user = mAuth.getCurrentUser();

                            // Store user data
                            int pickYear = binding.datePicker.getYear();
                            int pickMonth = binding.datePicker.getMonth();
                            int pickDay = binding.datePicker.getDayOfMonth();

                            UserModel.calendar.set(pickYear, pickMonth, pickDay);
                            Date date = UserModel.calendar.getTime();

                            String formattedDate = AdvancedDate.serialize(date);
                            Date parsedDate = AdvancedDate.parse(formattedDate);

                            UUID uuid = UUID.randomUUID();
                            String username = firstName.toLowerCase().substring(0, 1) + lastName.toLowerCase().substring(0,1) + uuid.toString().substring(0, 5);

                            Log.d("register-info", username);
                            Log.d("register-info", firstName);
                            Log.d("register-info", lastName);
                            Log.d("register-info", parsedDate.toString());
                            Log.d("register-info", emailAddress);

                            UserModel userModel = new UserModel(username, firstName, lastName, parsedDate, emailAddress);

                            firebaseFirestore
                                    .collection("users")
                                    .document(user.getUid())
                                    .set(userModel);

                            // Redirect to main activity after 4 seconds
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    unlockLayout();
                                    binding.registerProgressIndicator.hide();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.putExtra("name", username);
                                    intent.putExtra("firstName", firstName);
                                    intent.putExtra("lastName", lastName);
                                    intent.putExtra("birthDate", parsedDate.toString());
                                    intent.putExtra("emailAddress", emailAddress);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 4000);
                        } else {
                            String err = task.getException().getMessage();
                            Log.w("FIREBASE_SERVER", "createUserWithEmail:failure:" + err);

                            if (err.startsWith("The email address is already")) {
                                binding.emailTextField.setError(getString(R.string.email_already_exists));
                            } else if (err.startsWith("The email address is badly formatted")) {
                                binding.emailTextField.setError(getString(R.string.email_invalid));
                            } else {
                                binding.emailTextField.setError(getString(R.string.internal_error));
                                Toast.makeText(SignUpActivity.this, err, Toast.LENGTH_SHORT).show();
                            }

                            unlockLayout();
                            binding.registerProgressIndicator.hide();
                        }
                    }
                });
    }

    private void validateAllInput() {
    }

    private void lockLayout() {
         // ViewHandler.lockInputLayout(binding.usernameTextField);
         ViewHandler.lockInputLayout(binding.firstNameTextField);
         ViewHandler.lockInputLayout(binding.lastNameTextField);
         ViewHandler.lockInputLayout(binding.emailTextField);
         ViewHandler.lockInputLayout(binding.passwordTextField);
         ViewHandler.lockInputLayout(binding.confirmPasswordTextField);
         ViewHandler.lockInputLayout(binding.datePicker);
         ViewHandler.lockInputLayout(binding.loginTextField);
         ViewHandler.lockButton(binding.registerButton);
     }

    private void unlockLayout() {
        // ViewHandler.unlockInputLayout(binding.usernameTextField);
        ViewHandler.unlockInputLayout(binding.firstNameTextField);
        ViewHandler.unlockInputLayout(binding.lastNameTextField);
        ViewHandler.unlockInputLayout(binding.passwordTextField);
        ViewHandler.unlockInputLayout(binding.confirmPasswordTextField);
        ViewHandler.unlockInputLayout(binding.emailTextField);
        ViewHandler.unlockInputLayout(binding.datePicker);
        ViewHandler.unlockInputLayout(binding.loginTextField);
        ViewHandler.unlockButton(binding.registerButton);
    }

    private void setAllBoundError() {
        binding.firstNameTextField.setError(null);
        binding.firstNameTextField.setErrorEnabled(false);

        binding.lastNameTextField.setError(null);
        binding.lastNameTextField.setErrorEnabled(false);

        binding.emailTextField.setError(null);
        binding.emailTextField.setErrorEnabled(false);

        binding.passwordTextField.setError(null);
        binding.passwordTextField.setErrorEnabled(false);

        binding.confirmPasswordTextField.setError(null);
        binding.confirmPasswordTextField.setErrorEnabled(false);
    }

    private void detectClick() {
        String text = getString(R.string.login_action_text);
        SpannableString spannableString = new SpannableString(text);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
        };

        String clickableText = "Giriş yap";
        int startIndex = text.indexOf(clickableText);
        int endIndex = startIndex + clickableText.length();
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.loginTextField.setText(spannableString);
        binding.loginTextField.setMovementMethod(LinkMovementMethod.getInstance());
        binding.loginTextField.setHighlightColor(getResources().getColor(android.R.color.transparent));
    }

    private void redirect(Context packageContext, Class<?> cls) {
        Intent intent = new Intent(packageContext, cls);
        startActivity(intent);
        finish();
    }
}