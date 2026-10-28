package com.example.r005_student_assignment_tracker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.UUID;

public class Assignment implements Serializable {
    private String id;
    private String title;
    private String subject;
    private String description;
    private String dueDate;
    private long dueDateMillis;
    private String priority; // High, Medium, Low
    private boolean isCompleted;
    private long reminderTimeMillis;

    public Assignment() {
        this.id = UUID.randomUUID().toString();
        this.isCompleted = false;
        this.priority = "Medium";
        this.reminderTimeMillis = 0;
    }

    public Assignment(String title, String subject, String description, String dueDate, long dueDateMillis, String priority) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.subject = subject;
        this.description = description;
        this.dueDate = dueDate;
        this.dueDateMillis = dueDateMillis;
        this.priority = priority;
        this.isCompleted = false;
        this.reminderTimeMillis = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public long getDueDateMillis() {
        return dueDateMillis;
    }

    public void setDueDateMillis(long dueDateMillis) {
        this.dueDateMillis = dueDateMillis;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public long getReminderTimeMillis() {
        return reminderTimeMillis;
    }

    public void setReminderTimeMillis(long reminderTimeMillis) {
        this.reminderTimeMillis = reminderTimeMillis;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id", id);
        obj.put("title", title);
        obj.put("subject", subject);
        obj.put("description", description);
        obj.put("dueDate", dueDate);
        obj.put("dueDateMillis", dueDateMillis);
        obj.put("priority", priority);
        obj.put("isCompleted", isCompleted);
        obj.put("reminderTimeMillis", reminderTimeMillis);
        return obj;
    }

    public static Assignment fromJSONObject(JSONObject obj) throws JSONException {
        Assignment assignment = new Assignment();
        assignment.setId(obj.optString("id", UUID.randomUUID().toString()));
        assignment.setTitle(obj.optString("title", ""));
        assignment.setSubject(obj.optString("subject", ""));
        assignment.setDescription(obj.optString("description", ""));
        assignment.setDueDate(obj.optString("dueDate", ""));
        assignment.setDueDateMillis(obj.optLong("dueDateMillis", 0));
        assignment.setPriority(obj.optString("priority", "Medium"));
        assignment.setCompleted(obj.optBoolean("isCompleted", false));
        assignment.setReminderTimeMillis(obj.optLong("reminderTimeMillis", 0));
        return assignment;
    }
}
