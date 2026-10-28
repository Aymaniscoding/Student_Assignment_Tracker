package com.example.r005_student_assignment_tracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CampusFlow.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_ASSIGNMENTS = "assignments";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_SUBJECT = "subject";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DUE_DATE = "due_date";
    public static final String COLUMN_DUE_DATE_MILLIS = "due_date_millis";
    public static final String COLUMN_PRIORITY = "priority";
    public static final String COLUMN_IS_COMPLETED = "is_completed";
    public static final String COLUMN_REMINDER_TIME_MILLIS = "reminder_time_millis";

    private static final String CREATE_TABLE_ASSIGNMENTS = "CREATE TABLE " + TABLE_ASSIGNMENTS + " ("
            + COLUMN_ID + " TEXT PRIMARY KEY, "
            + COLUMN_TITLE + " TEXT NOT NULL, "
            + COLUMN_SUBJECT + " TEXT NOT NULL, "
            + COLUMN_DESCRIPTION + " TEXT, "
            + COLUMN_DUE_DATE + " TEXT NOT NULL, "
            + COLUMN_DUE_DATE_MILLIS + " INTEGER, "
            + COLUMN_PRIORITY + " TEXT NOT NULL, "
            + COLUMN_IS_COMPLETED + " INTEGER DEFAULT 0, "
            + COLUMN_REMINDER_TIME_MILLIS + " INTEGER DEFAULT 0"
            + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ASSIGNMENTS);
        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_ASSIGNMENTS + " ADD COLUMN " + COLUMN_REMINDER_TIME_MILLIS + " INTEGER DEFAULT 0;");
        }
    }

    public boolean insertAssignment(Assignment assignment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, assignment.getId());
        values.put(COLUMN_TITLE, assignment.getTitle());
        values.put(COLUMN_SUBJECT, assignment.getSubject());
        values.put(COLUMN_DESCRIPTION, assignment.getDescription());
        values.put(COLUMN_DUE_DATE, assignment.getDueDate());
        values.put(COLUMN_DUE_DATE_MILLIS, assignment.getDueDateMillis());
        values.put(COLUMN_PRIORITY, assignment.getPriority());
        values.put(COLUMN_IS_COMPLETED, assignment.isCompleted() ? 1 : 0);
        values.put(COLUMN_REMINDER_TIME_MILLIS, assignment.getReminderTimeMillis());

        long result = db.insert(TABLE_ASSIGNMENTS, null, values);
        return result != -1;
    }

    public List<Assignment> getAllAssignments() {
        List<Assignment> assignmentList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_ASSIGNMENTS, null, null, null, null, null, COLUMN_DUE_DATE_MILLIS + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Assignment assignment = cursorToAssignment(cursor);
                assignmentList.add(assignment);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return assignmentList;
    }

    public Assignment getAssignmentById(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ASSIGNMENTS, null, COLUMN_ID + " = ?", new String[]{id}, null, null, null);
        Assignment assignment = null;
        if (cursor != null && cursor.moveToFirst()) {
            assignment = cursorToAssignment(cursor);
            cursor.close();
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return assignment;
    }

    public boolean updateAssignment(Assignment assignment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, assignment.getTitle());
        values.put(COLUMN_SUBJECT, assignment.getSubject());
        values.put(COLUMN_DESCRIPTION, assignment.getDescription());
        values.put(COLUMN_DUE_DATE, assignment.getDueDate());
        values.put(COLUMN_DUE_DATE_MILLIS, assignment.getDueDateMillis());
        values.put(COLUMN_PRIORITY, assignment.getPriority());
        values.put(COLUMN_IS_COMPLETED, assignment.isCompleted() ? 1 : 0);
        values.put(COLUMN_REMINDER_TIME_MILLIS, assignment.getReminderTimeMillis());

        int rows = db.update(TABLE_ASSIGNMENTS, values, COLUMN_ID + " = ?", new String[]{assignment.getId()});
        return rows > 0;
    }

    public boolean deleteAssignment(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_ASSIGNMENTS, COLUMN_ID + " = ?", new String[]{id});
        return rows > 0;
    }

    public boolean toggleCompletion(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_ASSIGNMENTS, new String[]{COLUMN_IS_COMPLETED}, COLUMN_ID + " = ?", new String[]{id}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int currentStatus = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_COMPLETED));
            int newStatus = (currentStatus == 1) ? 0 : 1;
            cursor.close();

            ContentValues values = new ContentValues();
            values.put(COLUMN_IS_COMPLETED, newStatus);
            int rows = db.update(TABLE_ASSIGNMENTS, values, COLUMN_ID + " = ?", new String[]{id});
            return rows > 0;
        }
        if (cursor != null) cursor.close();
        return false;
    }

    private Assignment cursorToAssignment(Cursor cursor) {
        Assignment assignment = new Assignment();
        assignment.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        assignment.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
        assignment.setSubject(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SUBJECT)));
        assignment.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
        assignment.setDueDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_DATE)));
        assignment.setDueDateMillis(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DUE_DATE_MILLIS)));
        assignment.setPriority(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PRIORITY)));
        assignment.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)) == 1);
        int reminderIndex = cursor.getColumnIndex(COLUMN_REMINDER_TIME_MILLIS);
        if (reminderIndex != -1) {
            assignment.setReminderTimeMillis(cursor.getLong(reminderIndex));
        }
        return assignment;
    }

    private void insertSampleData(SQLiteDatabase db) {
        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.DAY_OF_MONTH, 2);
        insertAssignmentInternal(db, new Assignment(
                "Data Structures Lab Project",
                "Computer Science",
                "Implement Binary Search Tree and AVL tree balancing functions with unit tests.",
                "Oct 28, 2026",
                cal.getTimeInMillis(),
                "High"
        ));

        cal.add(Calendar.DAY_OF_MONTH, 3);
        insertAssignmentInternal(db, new Assignment(
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
        insertAssignmentInternal(db, done);

        cal.add(Calendar.DAY_OF_MONTH, 7);
        insertAssignmentInternal(db, new Assignment(
                "Software Engineering Design Specs",
                "Computer Science",
                "Submit UML Class Diagram and System Architecture Document for team project.",
                "Nov 05, 2026",
                cal.getTimeInMillis(),
                "Low"
        ));
    }

    private void insertAssignmentInternal(SQLiteDatabase db, Assignment assignment) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, assignment.getId());
        values.put(COLUMN_TITLE, assignment.getTitle());
        values.put(COLUMN_SUBJECT, assignment.getSubject());
        values.put(COLUMN_DESCRIPTION, assignment.getDescription());
        values.put(COLUMN_DUE_DATE, assignment.getDueDate());
        values.put(COLUMN_DUE_DATE_MILLIS, assignment.getDueDateMillis());
        values.put(COLUMN_PRIORITY, assignment.getPriority());
        values.put(COLUMN_IS_COMPLETED, assignment.isCompleted() ? 1 : 0);
        values.put(COLUMN_REMINDER_TIME_MILLIS, assignment.getReminderTimeMillis());
        db.insert(TABLE_ASSIGNMENTS, null, values);
    }
}
