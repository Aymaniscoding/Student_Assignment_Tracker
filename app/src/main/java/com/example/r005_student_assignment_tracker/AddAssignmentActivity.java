package com.example.r005_student_assignment_tracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AddAssignmentActivity extends AppCompatActivity {

    public static final String EXTRA_ASSIGNMENT = "extra_assignment";

    private TextView tvHeaderTitle;
    private TextInputLayout tilTitle, tilSubject, tilDueDate, tilReminderDate;
    private TextInputEditText etTitle, etDescription, etDueDate, etReminderDate;
    private MaterialAutoCompleteTextView etSubject;
    private ChipGroup chipGroupPriority;
    private MaterialButton btnSaveAssignment, btnCancel;
    private ImageButton ibBack;

    private long selectedDueDateMillis = 0;
    private long selectedReminderTimeMillis = 0;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
    private final SimpleDateFormat reminderFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.US);

    private AssignmentRepository repository;

    private Assignment existingAssignment;
    private boolean isEditMode = false;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_assignment);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_assignment_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = new AssignmentRepository(this);

        setupPermissionLauncher();
        initViews();
        setupSubjectDropdown();
        setupDatePicker();
        setupReminderPicker();
        setupListeners();
        checkEditMode();
    }

    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (!isGranted) {
                        Toast.makeText(this, "Notification permission denied. Reminders will not trigger notifications.", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void initViews() {
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        ibBack = findViewById(R.id.ibBack);
        tilTitle = findViewById(R.id.tilTitle);
        tilSubject = findViewById(R.id.tilSubject);
        tilDueDate = findViewById(R.id.tilDueDate);
        tilReminderDate = findViewById(R.id.tilReminderDate);

        etTitle = findViewById(R.id.etTitle);
        etSubject = findViewById(R.id.etSubject);
        etDescription = findViewById(R.id.etDescription);
        etDueDate = findViewById(R.id.etDueDate);
        etReminderDate = findViewById(R.id.etReminderDate);

        chipGroupPriority = findViewById(R.id.chipGroupPriority);
        btnSaveAssignment = findViewById(R.id.btnSaveAssignment);
        btnCancel = findViewById(R.id.btnCancel);

        // Pre-select default due date as tomorrow
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        selectedDueDateMillis = cal.getTimeInMillis();
        etDueDate.setText(dateFormat.format(cal.getTime()));
    }

    private void checkEditMode() {
        if (getIntent() != null && getIntent().hasExtra(EXTRA_ASSIGNMENT)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                existingAssignment = getIntent().getSerializableExtra(EXTRA_ASSIGNMENT, Assignment.class);
            } else {
                //noinspection deprecation
                existingAssignment = (Assignment) getIntent().getSerializableExtra(EXTRA_ASSIGNMENT);
            }

            if (existingAssignment != null) {
                isEditMode = true;

                if (tvHeaderTitle != null) {
                    tvHeaderTitle.setText(R.string.title_edit_assignment);
                }
                btnSaveAssignment.setText(R.string.btn_update_assignment);

                etTitle.setText(existingAssignment.getTitle());
                etSubject.setText(existingAssignment.getSubject());
                etDescription.setText(existingAssignment.getDescription());

                selectedDueDateMillis = existingAssignment.getDueDateMillis();
                etDueDate.setText(existingAssignment.getDueDate());

                selectedReminderTimeMillis = existingAssignment.getReminderTimeMillis();
                if (selectedReminderTimeMillis > 0) {
                    etReminderDate.setText(reminderFormat.format(new Date(selectedReminderTimeMillis)));
                }

                String priority = existingAssignment.getPriority();
                if ("High".equalsIgnoreCase(priority)) {
                    chipGroupPriority.check(R.id.chipPriorityHigh);
                } else if ("Low".equalsIgnoreCase(priority)) {
                    chipGroupPriority.check(R.id.chipPriorityLow);
                } else {
                    chipGroupPriority.check(R.id.chipPriorityMedium);
                }
            }
        }
    }

    private void setupSubjectDropdown() {
        String[] subjects = new String[]{
                "Computer Science",
                "Mathematics",
                "Physics",
                "Chemistry",
                "Biology",
                "Software Engineering",
                "Literature",
                "History",
                "Economics",
                "Psychology",
                "Other"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, subjects);
        etSubject.setAdapter(adapter);
    }

    private void setupDatePicker() {
        etDueDate.setOnClickListener(v -> showDatePickerDialog());
        tilDueDate.setEndIconOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Due Date")
                .setSelection(selectedDueDateMillis > 0 ? selectedDueDateMillis : MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            if (selection != null) {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(selection);
                selectedDueDateMillis = selection;
                etDueDate.setText(dateFormat.format(new Date(selection)));
                tilDueDate.setError(null);
            }
        });

        datePicker.show(getSupportFragmentManager(), "DUE_DATE_PICKER");
    }

    private void setupReminderPicker() {
        etReminderDate.setOnClickListener(v -> showReminderDateTimePicker());
        tilReminderDate.setEndIconOnClickListener(v -> showReminderDateTimePicker());
    }

    private void showReminderDateTimePicker() {
        checkNotificationPermission();

        long initialSelection = selectedReminderTimeMillis > System.currentTimeMillis()
                ? selectedReminderTimeMillis
                : MaterialDatePicker.todayInUtcMilliseconds();

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Reminder Date")
                .setSelection(initialSelection)
                .build();

        datePicker.addOnPositiveButtonClickListener(dateSelection -> {
            if (dateSelection != null) {
                Calendar selectedCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                selectedCal.setTimeInMillis(dateSelection);

                int year = selectedCal.get(Calendar.YEAR);
                int month = selectedCal.get(Calendar.MONTH);
                int day = selectedCal.get(Calendar.DAY_OF_MONTH);

                Calendar now = Calendar.getInstance();

                MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .setHour(now.get(Calendar.HOUR_OF_DAY))
                        .setMinute(now.get(Calendar.MINUTE))
                        .setTitleText("Select Reminder Time")
                        .build();

                timePicker.addOnPositiveButtonClickListener(v -> {
                    Calendar finalCal = Calendar.getInstance();
                    finalCal.set(Calendar.YEAR, year);
                    finalCal.set(Calendar.MONTH, month);
                    finalCal.set(Calendar.DAY_OF_MONTH, day);
                    finalCal.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                    finalCal.set(Calendar.MINUTE, timePicker.getMinute());
                    finalCal.set(Calendar.SECOND, 0);
                    finalCal.set(Calendar.MILLISECOND, 0);

                    long reminderTime = finalCal.getTimeInMillis();
                    if (reminderTime <= System.currentTimeMillis()) {
                        tilReminderDate.setError(getString(R.string.error_reminder_past));
                        selectedReminderTimeMillis = 0;
                        etReminderDate.setText("");
                    } else {
                        selectedReminderTimeMillis = reminderTime;
                        tilReminderDate.setError(null);
                        etReminderDate.setText(reminderFormat.format(finalCal.getTime()));
                    }
                });

                timePicker.show(getSupportFragmentManager(), "REMINDER_TIME_PICKER");
            }
        });

        datePicker.show(getSupportFragmentManager(), "REMINDER_DATE_PICKER");
    }

    private void setupListeners() {
        ibBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());

        etTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) tilTitle.setError(null);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        etSubject.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) tilSubject.setError(null);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSaveAssignment.setOnClickListener(v -> saveAssignment());
    }

    private void saveAssignment() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String subject = etSubject.getText() != null ? etSubject.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String dueDateStr = etDueDate.getText() != null ? etDueDate.getText().toString().trim() : "";

        boolean isValid = true;

        if (title.isEmpty()) {
            tilTitle.setError(getString(R.string.error_title_required));
            isValid = false;
        }

        if (subject.isEmpty()) {
            tilSubject.setError(getString(R.string.error_subject_required));
            isValid = false;
        }

        if (dueDateStr.isEmpty()) {
            tilDueDate.setError(getString(R.string.error_date_required));
            isValid = false;
        }

        if (selectedReminderTimeMillis > 0 && selectedReminderTimeMillis <= System.currentTimeMillis()) {
            tilReminderDate.setError(getString(R.string.error_reminder_past));
            isValid = false;
        }

        if (!isValid) return;

        // Determine Priority from chip selection
        int checkedChipId = chipGroupPriority.getCheckedChipId();
        String priority = "Medium";
        if (checkedChipId == R.id.chipPriorityHigh) {
            priority = "High";
        } else if (checkedChipId == R.id.chipPriorityLow) {
            priority = "Low";
        }

        if (isEditMode && existingAssignment != null) {
            existingAssignment.setTitle(title);
            existingAssignment.setSubject(subject);
            existingAssignment.setDescription(description);
            existingAssignment.setDueDate(dueDateStr);
            existingAssignment.setDueDateMillis(selectedDueDateMillis);
            existingAssignment.setPriority(priority);
            existingAssignment.setReminderTimeMillis(selectedReminderTimeMillis);

            boolean success = repository.updateAssignment(existingAssignment);
            if (success) {
                ReminderManager.scheduleReminder(this, existingAssignment);

                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_ASSIGNMENT, existingAssignment);
                setResult(RESULT_OK, resultIntent);

                Toast.makeText(this, R.string.toast_assignment_updated, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, R.string.toast_assignment_save_failed, Toast.LENGTH_SHORT).show();
            }
        } else {
            Assignment newAssignment = new Assignment(title, subject, description, dueDateStr, selectedDueDateMillis, priority);
            newAssignment.setReminderTimeMillis(selectedReminderTimeMillis);

            boolean success = repository.addAssignment(newAssignment);

            if (success) {
                ReminderManager.scheduleReminder(this, newAssignment);

                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_ASSIGNMENT, newAssignment);
                setResult(RESULT_OK, resultIntent);

                Toast.makeText(this, R.string.toast_assignment_saved, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, R.string.toast_assignment_save_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
