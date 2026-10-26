package com.example.r005_student_assignment_tracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AddAssignmentActivity extends AppCompatActivity {

    public static final String EXTRA_ASSIGNMENT = "extra_assignment";

    private TextInputLayout tilTitle, tilSubject, tilDueDate;
    private TextInputEditText etTitle, etDescription, etDueDate;
    private MaterialAutoCompleteTextView etSubject;
    private ChipGroup chipGroupPriority;
    private MaterialButton btnSaveAssignment, btnCancel;
    private ImageButton ibBack;

    private long selectedDueDateMillis = 0;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    private AssignmentRepository repository;

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

        initViews();
        setupSubjectDropdown();
        setupDatePicker();
        setupListeners();
    }

    private void initViews() {
        ibBack = findViewById(R.id.ibBack);
        tilTitle = findViewById(R.id.tilTitle);
        tilSubject = findViewById(R.id.tilSubject);
        tilDueDate = findViewById(R.id.tilDueDate);

        etTitle = findViewById(R.id.etTitle);
        etSubject = findViewById(R.id.etSubject);
        etDescription = findViewById(R.id.etDescription);
        etDueDate = findViewById(R.id.etDueDate);

        chipGroupPriority = findViewById(R.id.chipGroupPriority);
        btnSaveAssignment = findViewById(R.id.btnSaveAssignment);
        btnCancel = findViewById(R.id.btnCancel);

        // Pre-select default due date as tomorrow
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        selectedDueDateMillis = cal.getTimeInMillis();
        etDueDate.setText(dateFormat.format(cal.getTime()));
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
                // Adjust UTC selection to local date timezone
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(selection);
                selectedDueDateMillis = selection;
                etDueDate.setText(dateFormat.format(new Date(selection)));
                tilDueDate.setError(null);
            }
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
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

        if (!isValid) return;

        // Determine Priority from chip selection
        int checkedChipId = chipGroupPriority.getCheckedChipId();
        String priority = "Medium";
        if (checkedChipId == R.id.chipPriorityHigh) {
            priority = "High";
        } else if (checkedChipId == R.id.chipPriorityLow) {
            priority = "Low";
        }

        Assignment newAssignment = new Assignment(title, subject, description, dueDateStr, selectedDueDateMillis, priority);
        repository.addAssignment(newAssignment);

        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_ASSIGNMENT, newAssignment);
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, R.string.toast_assignment_saved, Toast.LENGTH_SHORT).show();
        finish();
    }
}
