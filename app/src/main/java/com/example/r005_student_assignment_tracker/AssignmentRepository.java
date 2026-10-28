package com.example.r005_student_assignment_tracker;

import android.content.Context;

import java.util.List;

public class AssignmentRepository {
    private final DatabaseHelper dbHelper;

    public AssignmentRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public List<Assignment> getAllAssignments() {
        return dbHelper.getAllAssignments();
    }

    public Assignment getAssignmentById(String id) {
        return dbHelper.getAssignmentById(id);
    }

    public boolean addAssignment(Assignment assignment) {
        return dbHelper.insertAssignment(assignment);
    }

    public boolean updateAssignment(Assignment assignment) {
        return dbHelper.updateAssignment(assignment);
    }

    public boolean deleteAssignment(String id) {
        return dbHelper.deleteAssignment(id);
    }

    public boolean toggleCompletion(String id) {
        return dbHelper.toggleCompletion(id);
    }
}
