package com.example.lifefit.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.lifefit.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseUtils {

    private FirebaseFirestore db;

    public FirebaseUtils() {
        db = FirebaseFirestore.getInstance();
    }

    public interface DataCallback {
        void onSuccess(UserModel userModel);
        void onFailure(String errorMessage);
    }

    public void fetchUserData(String uid, final DataCallback callback) {
        db.collection("users").document(uid).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                UserModel userModel = document.toObject(UserModel.class);
                                callback.onSuccess(userModel);
                            } else {
                                callback.onFailure("Document not found");
                            }
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    }
                });
    }
}
