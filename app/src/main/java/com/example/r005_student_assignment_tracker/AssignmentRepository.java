package com.example.r005_student_assignment_tracker;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AssignmentRepository {
    private static final String PREF_NAME = "campusflow_prefs";
    private static final String KEY_ASSIGNMENTS = "key_assignments_list";
    private static final String KEY_FIRST_RUN = "key_first_run_done";

    private final SharedPreferences prefs;

    public AssignmentRepository(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (!prefs.getBoolean(KEY_FIRST_RUN, false)) {
            initSampleData();
            prefs.edit().putBoolean(KEY_FIRST_RUN, true).apply();
        }
    }

    public List<Assignment> getAllAssignments() {
        List<Assignment> list = new ArrayList<>();
        String jsonString = prefs.getString(KEY_ASSIGNMENTS, null);
        if (jsonString != null) {
            try {
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    list.add(Assignment.fromJSONObject(jsonArray.getJSONObject(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public void saveAll(List<Assignment> assignments) {
        JSONArray jsonArray = new JSONArray();
        for (Assignment assignment : assignments) {
            try {
                jsonArray.put(assignment.toJSONObject());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        prefs.edit().putString(KEY_ASSIGNMENTS, jsonArray.toString()).apply();
    }

    public void addAssignment(Assignment assignment) {
        List<Assignment> assignments = getAllAssignments();
        assignments.add(0, assignment); // add to top
        saveAll(assignments);
    }

    public void updateAssignment(Assignment updated) {
        List<Assignment> assignments = getAllAssignments();
        for (int i = 0; i < assignments.size(); i++) {
            if (assignments.get(i).getId().equals(updated.getId())) {
                assignments.set(i, updated);
                break;
            }
        }
        saveAll(assignments);
    }

    public void deleteAssignment(String id) {
        List<Assignment> assignments = getAllAssignments();
        for (int i = 0; i < assignments.size(); i++) {
            if (assignments.get(i).getId().equals(id)) {
                assignments.remove(i);
                break;
            }
        }
        saveAll(assignments);
    }

    public void toggleCompletion(String id) {
        List<Assignment> assignments = getAllAssignments();
        for (Assignment a : assignments) {
            if (a.getId().equals(id)) {
                a.setCompleted(!a.isCompleted());
                break;
            }
        }
        saveAll(assignments);
    }

    private void initSampleData() {
        List<Assignment> sampleList = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.DAY_OF_MONTH, 2);
        sampleList.add(new Assignment(
                "Data Structures Lab Project",
                "Computer Science",
                "Implement Binary Search Tree and AVL tree balancing functions with unit tests.",
                "Oct 28, 2026",
                cal.getTimeInMillis(),
                "High"
        ));

        cal.add(Calendar.DAY_OF_MONTH, 3);
        sampleList.add(new Assignment(
                "Calculus III Problem Set",
                "Mathematics",
                "Complete problems 12 to 28 from Chapter 4 on multivariable integration.",
                "Oct 31, 2026",
                cal.getTimeInMillis(),
                "Medium"
        ));

        cal.add(Calendar.DAY_OF_MONTH, 5);
        Assignment done = new Assignment(
                "Physics Quantum Mechanics Essay",
                "Physics",
                "Write a 1500-word analysis on Wave-Particle Duality and Quantum Tunneling.",
                "Nov 03, 2026",
                cal.getTimeInMillis(),
                "High"
        );
        done.setCompleted(true);
        sampleList.add(done);

        cal.add(Calendar.DAY_OF_MONTH, 7);
        sampleList.add(new Assignment(
                "Software Engineering Design Specs",
                "Computer Science",
                "Submit UML Class Diagram and System Architecture Document for team project.",
                "Nov 05, 2026",
                cal.getTimeInMillis(),
                "Low"
        ));

        saveAll(sampleList);
    }
}
