package com.example.r005_student_assignment_tracker;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String assignmentId = intent.getStringExtra(ReminderManager.EXTRA_ASSIGNMENT_ID);
        if (assignmentId == null || assignmentId.isEmpty()) return;

        // Query SQLite to verify assignment exists and is not completed
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        Assignment assignment = dbHelper.getAssignmentById(assignmentId);

        if (assignment == null || assignment.isCompleted()) {
            // Assignment deleted or completed -> cancel notification
            return;
        }

        // Check POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        // Tap notification intent -> opens MainActivity
        Intent contentIntent = new Intent(context, MainActivity.class);
        contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                assignmentId.hashCode(),
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = "Assignment Reminder ⏰";
        String message = assignment.getTitle() + " (" + assignment.getSubject() + ") is due on " + assignment.getDueDate() + "!";

        ReminderManager.createNotificationChannel(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, ReminderManager.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_clock)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(assignmentId.hashCode(), builder.build());
        }
    }
}
