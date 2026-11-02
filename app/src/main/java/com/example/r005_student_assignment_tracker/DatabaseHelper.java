package com.example.r005_student_assignment_tracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "CampusFlow.db";
    private static final int DATABASE_VERSION = 5;

    public static final String TABLE_ASSIGNMENTS = "assignments";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_SUBJECT = "subject";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DUE_DATE = "due_date";
    public static final String COLUMN_DUE_DATE_MILLIS = "due_date_millis";
    public static final String COLUMN_PRIORITY = "priority";
    public static final String COLUMN_IS_COMPLETED = "is_completed";
    public static final String COLUMN_REMINDER_TIME_MILLIS = "reminder_time_millis";
    public static final String COLUMN_SUBMISSION_NOTES = "submission_notes";
    public static final String COLUMN_SUBMISSION_TIME_MILLIS = "submission_time_millis";

    private static final String CREATE_TABLE_ASSIGNMENTS = "CREATE TABLE " + TABLE_ASSIGNMENTS + " ("
            + COLUMN_ID + " TEXT PRIMARY KEY, "
            + COLUMN_USER_ID + " TEXT DEFAULT '', "
            + COLUMN_TITLE + " TEXT NOT NULL, "
            + COLUMN_SUBJECT + " TEXT NOT NULL, "
            + COLUMN_DESCRIPTION + " TEXT, "
            + COLUMN_DUE_DATE + " TEXT NOT NULL, "
            + COLUMN_DUE_DATE_MILLIS + " INTEGER, "
            + COLUMN_PRIORITY + " TEXT NOT NULL, "
            + COLUMN_IS_COMPLETED + " INTEGER DEFAULT 0, "
            + COLUMN_REMINDER_TIME_MILLIS + " INTEGER DEFAULT 0, "
            + COLUMN_SUBMISSION_NOTES + " TEXT DEFAULT '', "
            + COLUMN_SUBMISSION_TIME_MILLIS + " INTEGER DEFAULT 0"
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
        if (oldVersion < 5) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSIGNMENTS + ";");
            onCreate(db);
            return;
        }
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_ASSIGNMENTS + " ADD COLUMN " + COLUMN_REMINDER_TIME_MILLIS + " INTEGER DEFAULT 0;");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_ASSIGNMENTS + " ADD COLUMN " + COLUMN_USER_ID + " TEXT DEFAULT '';");
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_ASSIGNMENTS + " ADD COLUMN " + COLUMN_SUBMISSION_NOTES + " TEXT DEFAULT '';");
            db.execSQL("ALTER TABLE " + TABLE_ASSIGNMENTS + " ADD COLUMN " + COLUMN_SUBMISSION_TIME_MILLIS + " INTEGER DEFAULT 0;");
        }
    }

    public boolean insertAssignment(Assignment assignment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, assignment.getId());
        values.put(COLUMN_USER_ID, assignment.getUserId() != null ? assignment.getUserId() : "");
        values.put(COLUMN_TITLE, assignment.getTitle());
        values.put(COLUMN_SUBJECT, assignment.getSubject());
        values.put(COLUMN_DESCRIPTION, assignment.getDescription());
        values.put(COLUMN_DUE_DATE, assignment.getDueDate());
        values.put(COLUMN_DUE_DATE_MILLIS, assignment.getDueDateMillis());
        values.put(COLUMN_PRIORITY, assignment.getPriority());
        values.put(COLUMN_IS_COMPLETED, assignment.isCompleted() ? 1 : 0);
        values.put(COLUMN_REMINDER_TIME_MILLIS, assignment.getReminderTimeMillis());
        values.put(COLUMN_SUBMISSION_NOTES, assignment.getSubmissionNotes() != null ? assignment.getSubmissionNotes() : "");
        values.put(COLUMN_SUBMISSION_TIME_MILLIS, assignment.getSubmissionTimeMillis());

        long result = db.insert(TABLE_ASSIGNMENTS, null, values);
        return result != -1;
    }

    public List<Assignment> getAllAssignments(String userId) {
        List<Assignment> assignmentList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = null;
        String[] selectionArgs = null;

        if (userId != null && !userId.isEmpty()) {
            selection = COLUMN_USER_ID + " = ? OR " + COLUMN_USER_ID + " = ''";
            selectionArgs = new String[]{userId};
        }

        try (Cursor cursor = db.query(TABLE_ASSIGNMENTS, null, selection, selectionArgs, null, null, COLUMN_DUE_DATE_MILLIS + " ASC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    assignmentList.add(cursorToAssignment(cursor));
                } while (cursor.moveToNext());
            }
        }
        return assignmentList;
    }

    public Assignment getAssignmentById(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_ASSIGNMENTS, null, COLUMN_ID + " = ?", new String[]{id}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursorToAssignment(cursor);
            }
        }
        return null;
    }

    public boolean isDuplicateTitle(String title, String userId, String excludeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection;
        List<String> argsList = new ArrayList<>();

        selection = "LOWER(" + COLUMN_TITLE + ") = LOWER(?)";
        argsList.add(title.trim());

        if (userId != null && !userId.isEmpty()) {
            selection += " AND (" + COLUMN_USER_ID + " = ? OR " + COLUMN_USER_ID + " = '')";
            argsList.add(userId);
        }

        if (excludeId != null && !excludeId.isEmpty()) {
            selection += " AND " + COLUMN_ID + " != ?";
            argsList.add(excludeId);
        }

        try (Cursor cursor = db.query(TABLE_ASSIGNMENTS, new String[]{COLUMN_ID}, selection, argsList.toArray(new String[0]), null, null, null)) {
            return cursor != null && cursor.getCount() > 0;
        }
    }

    public boolean updateAssignment(Assignment assignment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, assignment.getUserId() != null ? assignment.getUserId() : "");
        values.put(COLUMN_TITLE, assignment.getTitle());
        values.put(COLUMN_SUBJECT, assignment.getSubject());
        values.put(COLUMN_DESCRIPTION, assignment.getDescription());
        values.put(COLUMN_DUE_DATE, assignment.getDueDate());
        values.put(COLUMN_DUE_DATE_MILLIS, assignment.getDueDateMillis());
        values.put(COLUMN_PRIORITY, assignment.getPriority());
        values.put(COLUMN_IS_COMPLETED, assignment.isCompleted() ? 1 : 0);
        values.put(COLUMN_REMINDER_TIME_MILLIS, assignment.getReminderTimeMillis());
        values.put(COLUMN_SUBMISSION_NOTES, assignment.getSubmissionNotes() != null ? assignment.getSubmissionNotes() : "");
        values.put(COLUMN_SUBMISSION_TIME_MILLIS, assignment.getSubmissionTimeMillis());

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
        try (Cursor cursor = db.query(TABLE_ASSIGNMENTS, new String[]{COLUMN_IS_COMPLETED}, COLUMN_ID + " = ?", new String[]{id}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int currentStatus = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_COMPLETED));
                int newStatus = (currentStatus == 1) ? 0 : 1;

                ContentValues values = new ContentValues();
                values.put(COLUMN_IS_COMPLETED, newStatus);
                int rows = db.update(TABLE_ASSIGNMENTS, values, COLUMN_ID + " = ?", new String[]{id});
                return rows > 0;
            }
        }
        return false;
    }

    private Assignment cursorToAssignment(Cursor cursor) {
        Assignment assignment = new Assignment();
        assignment.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));

        int userIdx = cursor.getColumnIndex(COLUMN_USER_ID);
        if (userIdx != -1) {
            assignment.setUserId(cursor.getString(userIdx));
        }

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

        int subNotesIdx = cursor.getColumnIndex(COLUMN_SUBMISSION_NOTES);
        if (subNotesIdx != -1) {
            assignment.setSubmissionNotes(cursor.getString(subNotesIdx));
        }

        int subTimeIdx = cursor.getColumnIndex(COLUMN_SUBMISSION_TIME_MILLIS);
        if (subTimeIdx != -1) {
            assignment.setSubmissionTimeMillis(cursor.getLong(subTimeIdx));
        }

        return assignment;
    }

    private void insertSampleData(SQLiteDatabase db) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

        // 1. Missed / Overdue Assignment (Due 3 days ago)
        Calendar missedCal = Calendar.getInstance();
        missedCal.add(Calendar.DAY_OF_MONTH, -3);
        insertAssignmentInternal(db, new Assignment(
                "Data Structures Lab Project",
                "Computer Science",
                "Implement Binary Search Tree and AVL tree balancing functions with unit tests.",
                sdf.format(missedCal.getTime()),
                missedCal.getTimeInMillis(),
                "High"
        ));

        // 2. Upcoming Soon Assignment (Due in 1 day)
        Calendar soonCal = Calendar.getInstance();
        soonCal.add(Calendar.DAY_OF_MONTH, 1);
        insertAssignmentInternal(db, new Assignment(
                "Calculus III Problem Set",
                "Mathematics",
                "Complete problems 12 to 28 from Chapter 4 on multivariable integration.",
                sdf.format(soonCal.getTime()),
                soonCal.getTimeInMillis(),
                "Medium"
        ));

        // 3. Completed Assignment (Due in 3 days)
        Calendar doneCal = Calendar.getInstance();
        doneCal.add(Calendar.DAY_OF_MONTH, 3);
        Assignment done = new Assignment(
                "Physics Quantum Mechanics Essay",
                "Physics",
                "Write a 1500-word analysis on Wave-Particle Duality and Quantum Tunneling.",
                sdf.format(doneCal.getTime()),
                doneCal.getTimeInMillis(),
                "High"
        );
        done.setCompleted(true);
        insertAssignmentInternal(db, done);

        // 4. Due Later Assignment (Due in 7 days)
        Calendar laterCal = Calendar.getInstance();
        laterCal.add(Calendar.DAY_OF_MONTH, 7);
        insertAssignmentInternal(db, new Assignment(
                "Software Engineering Design Specs",
                "Computer Science",
                "Submit UML Class Diagram and System Architecture Document for team project.",
                sdf.format(laterCal.getTime()),
                laterCal.getTimeInMillis(),
                "Low"
        ));
    }

    private void insertAssignmentInternal(SQLiteDatabase db, Assignment assignment) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, assignment.getId());
        values.put(COLUMN_USER_ID, assignment.getUserId() != null ? assignment.getUserId() : "");
        values.put(COLUMN_TITLE, assignment.getTitle());
        values.put(COLUMN_SUBJECT, assignment.getSubject());
        values.put(COLUMN_DESCRIPTION, assignment.getDescription());
        values.put(COLUMN_DUE_DATE, assignment.getDueDate());
        values.put(COLUMN_DUE_DATE_MILLIS, assignment.getDueDateMillis());
        values.put(COLUMN_PRIORITY, assignment.getPriority());
        values.put(COLUMN_IS_COMPLETED, assignment.isCompleted() ? 1 : 0);
        values.put(COLUMN_REMINDER_TIME_MILLIS, assignment.getReminderTimeMillis());
        values.put(COLUMN_SUBMISSION_NOTES, assignment.getSubmissionNotes() != null ? assignment.getSubmissionNotes() : "");
        values.put(COLUMN_SUBMISSION_TIME_MILLIS, assignment.getSubmissionTimeMillis());
        db.insert(TABLE_ASSIGNMENTS, null, values);
    }
}
