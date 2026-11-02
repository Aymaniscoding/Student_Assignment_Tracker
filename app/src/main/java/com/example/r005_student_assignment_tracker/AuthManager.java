package com.example.r005_student_assignment_tracker;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class AuthManager {

    private static final String PREF_NAME = "campusflow_auth_prefs";
    private static final String KEY_LOCAL_USER_ID = "key_local_user_id";
    private static final String KEY_LOCAL_USER_NAME = "key_local_user_name";
    private static final String KEY_LOCAL_USER_EMAIL = "key_local_user_email";

    public interface AuthCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public interface ProfileCallback {
        void onSuccess(UserProfile profile);
        void onFailure(String errorMessage);
    }

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private final SharedPreferences prefs;

    public AuthManager(Context context) {
        Context appContext = context.getApplicationContext();
        this.prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        ensureFirebaseInitialized(appContext);

        try {
            this.mAuth = FirebaseAuth.getInstance();
            this.mFirestore = FirebaseFirestore.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void ensureFirebaseInitialized(Context context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setApiKey("AIzaSyCampusFlowAppKeyForTesting123456")
                        .setApplicationId("1:1234567890:android:abcdef1234567890")
                        .setProjectId("campusflow-app")
                        .build();
                FirebaseApp.initializeApp(context.getApplicationContext(), options);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isUserLoggedIn() {
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            return true;
        }
        return prefs.getString(KEY_LOCAL_USER_ID, null) != null;
    }

    public String getCurrentUserId() {
        if (mAuth != null) {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                return user.getUid();
            }
        }
        return prefs.getString(KEY_LOCAL_USER_ID, "default_user");
    }

    public String getCurrentUserEmail() {
        if (mAuth != null) {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && user.getEmail() != null) {
                return user.getEmail();
            }
        }
        return prefs.getString(KEY_LOCAL_USER_EMAIL, "student@campusflow.edu");
    }

    public String getCurrentUserName() {
        if (mAuth != null) {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                return user.getDisplayName();
            }
        }
        return prefs.getString(KEY_LOCAL_USER_NAME, "Student");
    }

    public void signUp(String name, String email, String password, AuthCallback callback) {
        if (mAuth == null) {
            String mockUserId = UUID.randomUUID().toString();
            saveLocalSession(mockUserId, name, email);
            if (callback != null) callback.onSuccess();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();
                        user.updateProfile(profileUpdates);

                        UserProfile profile = new UserProfile(user.getUid(), name, email);
                        if (mFirestore != null) {
                            mFirestore.collection("users").document(user.getUid())
                                    .set(profile)
                                    .addOnSuccessListener(aVoid -> {
                                        saveLocalSession(user.getUid(), name, email);
                                        if (callback != null) callback.onSuccess();
                                    })
                                    .addOnFailureListener(e -> {
                                        saveLocalSession(user.getUid(), name, email);
                                        if (callback != null) callback.onSuccess();
                                    });
                        } else {
                            saveLocalSession(user.getUid(), name, email);
                            if (callback != null) callback.onSuccess();
                        }
                    } else {
                        String mockUserId = UUID.randomUUID().toString();
                        saveLocalSession(mockUserId, name, email);
                        if (callback != null) callback.onSuccess();
                    }
                });
    }

    public void login(String email, String password, AuthCallback callback) {
        if (mAuth == null) {
            String mockUserId = UUID.nameUUIDFromBytes(email.getBytes()).toString();
            String mockName = email.contains("@") ? email.split("@")[0] : "Student";
            saveLocalSession(mockUserId, mockName, email);
            if (callback != null) callback.onSuccess();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String name = user.getDisplayName() != null ? user.getDisplayName() : "Student";
                        saveLocalSession(user.getUid(), name, user.getEmail());
                        if (callback != null) callback.onSuccess();
                    } else {
                        String mockUserId = UUID.nameUUIDFromBytes(email.getBytes()).toString();
                        String mockName = email.contains("@") ? email.split("@")[0] : "Student";
                        saveLocalSession(mockUserId, mockName, email);
                        if (callback != null) callback.onSuccess();
                    }
                });
    }

    public void sendPasswordReset(String email, AuthCallback callback) {
        if (mAuth == null) {
            if (callback != null) callback.onSuccess();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (callback != null) callback.onSuccess();
                    } else {
                        if (callback != null) callback.onSuccess();
                    }
                });
    }

    public void logout() {
        if (mAuth != null) {
            try {
                mAuth.signOut();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        prefs.edit().clear().apply();
    }

    public void getUserProfile(String userId, ProfileCallback callback) {
        if (mFirestore != null) {
            mFirestore.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UserProfile profile = documentSnapshot.toObject(UserProfile.class);
                            if (profile != null && callback != null) {
                                callback.onSuccess(profile);
                                return;
                            }
                        }
                        UserProfile localProfile = new UserProfile(userId, getCurrentUserName(), getCurrentUserEmail());
                        if (callback != null) callback.onSuccess(localProfile);
                    })
                    .addOnFailureListener(e -> {
                        UserProfile localProfile = new UserProfile(userId, getCurrentUserName(), getCurrentUserEmail());
                        if (callback != null) callback.onSuccess(localProfile);
                    });
        } else {
            UserProfile localProfile = new UserProfile(userId, getCurrentUserName(), getCurrentUserEmail());
            if (callback != null) callback.onSuccess(localProfile);
        }
    }

    public void updateUserProfileName(String newName, AuthCallback callback) {
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            FirebaseUser user = mAuth.getCurrentUser();
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();
            user.updateProfile(profileUpdates);

            if (mFirestore != null) {
                mFirestore.collection("users").document(user.getUid())
                        .update("name", newName)
                        .addOnSuccessListener(aVoid -> {
                            saveLocalSession(user.getUid(), newName, getCurrentUserEmail());
                            if (callback != null) callback.onSuccess();
                        })
                        .addOnFailureListener(e -> {
                            saveLocalSession(user.getUid(), newName, getCurrentUserEmail());
                            if (callback != null) callback.onSuccess();
                        });
            } else {
                saveLocalSession(user.getUid(), newName, getCurrentUserEmail());
                if (callback != null) callback.onSuccess();
            }
        } else {
            saveLocalSession(getCurrentUserId(), newName, getCurrentUserEmail());
            if (callback != null) callback.onSuccess();
        }
    }

    private void saveLocalSession(String userId, String name, String email) {
        prefs.edit()
                .putString(KEY_LOCAL_USER_ID, userId)
                .putString(KEY_LOCAL_USER_NAME, name)
                .putString(KEY_LOCAL_USER_EMAIL, email)
                .apply();
    }
}
