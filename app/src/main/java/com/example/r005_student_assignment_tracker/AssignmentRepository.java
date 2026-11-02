package com.example.r005_student_assignment_tracker;

import android.content.Context;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class AssignmentRepository {
    private final DatabaseHelper dbHelper;
    private FirebaseFirestore mFirestore;

    public interface FirestoreSyncCallback {
        void onSyncComplete();
    }

    public AssignmentRepository(Context context) {
        Context appContext = context.getApplicationContext();
        this.dbHelper = new DatabaseHelper(appContext);
        ensureFirebaseInitialized(appContext);

        try {
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

    public List<Assignment> getAllAssignments(String userId) {
        return dbHelper.getAllAssignments(userId);
    }

    public Assignment getAssignmentById(String id) {
        return dbHelper.getAssignmentById(id);
    }

    public boolean isDuplicateTitle(String title, String userId, String excludeId) {
        return dbHelper.isDuplicateTitle(title, userId, excludeId);
    }

    public boolean addAssignment(Assignment assignment) {
        boolean success = dbHelper.insertAssignment(assignment);
        if (success && assignment.getUserId() != null && !assignment.getUserId().isEmpty()) {
            syncToFirestore(assignment);
        }
        return success;
    }

    public boolean updateAssignment(Assignment assignment) {
        boolean success = dbHelper.updateAssignment(assignment);
        if (success && assignment.getUserId() != null && !assignment.getUserId().isEmpty()) {
            syncToFirestore(assignment);
        }
        return success;
    }

    public boolean deleteAssignment(String userId, String id) {
        boolean success = dbHelper.deleteAssignment(id);
        if (mFirestore != null && userId != null && !userId.isEmpty()) {
            try {
                mFirestore.collection("users").document(userId)
                        .collection("assignments").document(id)
                        .delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    public boolean toggleCompletion(String id) {
        boolean success = dbHelper.toggleCompletion(id);
        Assignment updated = dbHelper.getAssignmentById(id);
        if (success && updated != null && updated.getUserId() != null && !updated.getUserId().isEmpty()) {
            syncToFirestore(updated);
        }
        return success;
    }

    public void syncFromFirestore(String userId, FirestoreSyncCallback callback) {
        if (mFirestore == null || userId == null || userId.isEmpty()) {
            if (callback != null) callback.onSyncComplete();
            return;
        }

        try {
            mFirestore.collection("users").document(userId)
                    .collection("assignments")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                Assignment remote = doc.toObject(Assignment.class);
                                if (remote != null) {
                                    remote.setUserId(userId);
                                    if (dbHelper.getAssignmentById(remote.getId()) != null) {
                                        dbHelper.updateAssignment(remote);
                                    } else {
                                        dbHelper.insertAssignment(remote);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (callback != null) callback.onSyncComplete();
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onSyncComplete();
                    });
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) callback.onSyncComplete();
        }
    }

    private void syncToFirestore(Assignment assignment) {
        if (mFirestore == null || assignment == null || assignment.getUserId() == null || assignment.getUserId().isEmpty()) return;

        try {
            mFirestore.collection("users").document(assignment.getUserId())
                    .collection("assignments").document(assignment.getId())
                    .set(assignment);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
