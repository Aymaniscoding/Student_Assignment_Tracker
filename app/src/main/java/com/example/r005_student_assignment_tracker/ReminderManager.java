package com.example.r005_student_assignment_tracker;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ReminderManager {

    public static final String CHANNEL_ID = "campusflow_reminders_channel";
    public static final String CHANNEL_NAME = "Assignment Reminders";

    public static final String EXTRA_ASSIGNMENT_ID = "extra_assignment_id";
    public static final String EXTRA_ASSIGNMENT_TITLE = "extra_assignment_title";
    public static final String EXTRA_ASSIGNMENT_DUE_DATE = "extra_assignment_due_date";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for scheduled assignment reminders");
            channel.enableVibration(true);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static void scheduleReminder(Context context, Assignment assignment) {
        if (assignment == null || assignment.getId() == null) return;

        // If reminder time is invalid, in the past, or assignment is completed -> cancel any existing reminder
        if (assignment.isCompleted()
                || assignment.getReminderTimeMillis() <= 0
                || assignment.getReminderTimeMillis() <= System.currentTimeMillis()) {
            cancelReminder(context, assignment.getId());
            return;
        }

        createNotificationChannel(context);

        int requestCode = assignment.getId().hashCode();
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction("com.example.r005_student_assignment_tracker.ACTION_REMINDER_TRIGGER");
        intent.putExtra(EXTRA_ASSIGNMENT_ID, assignment.getId());
        intent.putExtra(EXTRA_ASSIGNMENT_TITLE, assignment.getTitle());
        intent.putExtra(EXTRA_ASSIGNMENT_DUE_DATE, assignment.getDueDate());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, assignment.getReminderTimeMillis(), pendingIntent);
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, assignment.getReminderTimeMillis(), pendingIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, assignment.getReminderTimeMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, assignment.getReminderTimeMillis(), pendingIntent);
            }
        }
    }

    public static void cancelReminder(Context context, String assignmentId) {
        if (assignmentId == null) return;

        int requestCode = assignmentId.hashCode();
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction("com.example.r005_student_assignment_tracker.ACTION_REMINDER_TRIGGER");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );

        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
            pendingIntent.cancel();
        }
    }
}
