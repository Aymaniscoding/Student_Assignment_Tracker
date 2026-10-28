package com.example.r005_student_assignment_tracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AssignmentAdapter.OnAssignmentActionListener {

    private TextView tvStatTotal, tvStatPending, tvStatCompleted, tvStatHighPriority;
    private TextView tvCountBadge;
    private TextInputEditText etSearch;
    private ChipGroup chipGroupFilter;
    private RecyclerView rvAssignments;
    private LinearLayout layoutEmptyState;
    private ExtendedFloatingActionButton fabAddAssignment;

    private AssignmentRepository repository;
    private AssignmentAdapter adapter;
    private ActivityResultLauncher<Intent> addAssignmentLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private String currentFilter = "All";
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = new AssignmentRepository(this);

        setupPermissionLauncher();
        checkNotificationPermission();

        initViews();
        setupSearch();
        setupRecyclerView();
        setupFilterChips();
        setupActivityLauncher();
        setupFab();
    }

    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    // Permission result
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

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void initViews() {
        tvStatTotal = findViewById(R.id.tvStatTotal);
        tvStatPending = findViewById(R.id.tvStatPending);
        tvStatCompleted = findViewById(R.id.tvStatCompleted);
        tvStatHighPriority = findViewById(R.id.tvStatHighPriority);

        tvCountBadge = findViewById(R.id.tvCountBadge);
        etSearch = findViewById(R.id.etSearch);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        rvAssignments = findViewById(R.id.rvAssignments);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        fabAddAssignment = findViewById(R.id.fabAddAssignment);
    }

    private void setupSearch() {
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s != null ? s.toString().trim().toLowerCase() : "";
                    loadDashboardData();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupRecyclerView() {
        adapter = new AssignmentAdapter(this, this);
        rvAssignments.setLayoutManager(new LinearLayoutManager(this));
        rvAssignments.setAdapter(adapter);
    }

    private void setupFilterChips() {
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipFilterPending) {
                currentFilter = "Pending";
            } else if (checkedId == R.id.chipFilterCompleted) {
                currentFilter = "Completed";
            } else if (checkedId == R.id.chipFilterHigh) {
                currentFilter = "High";
            } else {
                currentFilter = "All";
            }
            loadDashboardData();
        });
    }

    private void setupActivityLauncher() {
        addAssignmentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadDashboardData();
                    }
                }
        );
    }

    private void setupFab() {
        fabAddAssignment.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddAssignmentActivity.class);
            addAssignmentLauncher.launch(intent);
        });
    }

    private void loadDashboardData() {
        List<Assignment> allAssignments = repository.getAllAssignments();

        int totalCount = allAssignments.size();
        int pendingCount = 0;
        int completedCount = 0;
        int highPriorityCount = 0;

        List<Assignment> filteredList = new ArrayList<>();

        for (Assignment assignment : allAssignments) {
            if (assignment.isCompleted()) {
                completedCount++;
            } else {
                pendingCount++;
            }

            if ("High".equalsIgnoreCase(assignment.getPriority())) {
                highPriorityCount++;
            }

            // Check Search Query (Title or Subject)
            boolean matchesSearch = currentSearchQuery.isEmpty()
                    || (assignment.getTitle() != null && assignment.getTitle().toLowerCase().contains(currentSearchQuery))
                    || (assignment.getSubject() != null && assignment.getSubject().toLowerCase().contains(currentSearchQuery));

            // Check Filter Choice
            boolean matchesFilter = false;
            switch (currentFilter) {
                case "Pending":
                    matchesFilter = !assignment.isCompleted();
                    break;
                case "Completed":
                    matchesFilter = assignment.isCompleted();
                    break;
                case "High":
                    matchesFilter = "High".equalsIgnoreCase(assignment.getPriority());
                    break;
                case "All":
                default:
                    matchesFilter = true;
                    break;
            }

            if (matchesSearch && matchesFilter) {
                filteredList.add(assignment);
            }
        }

        // Update Stat Cards with actual database counts
        tvStatTotal.setText(String.valueOf(totalCount));
        tvStatPending.setText(String.valueOf(pendingCount));
        tvStatCompleted.setText(String.valueOf(completedCount));
        tvStatHighPriority.setText(String.valueOf(highPriorityCount));

        // Update count badge & list visibility
        tvCountBadge.setText(String.valueOf(filteredList.size()));

        if (filteredList.isEmpty()) {
            rvAssignments.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvAssignments.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
            adapter.setAssignments(filteredList);
        }
    }

    @Override
    public void onStatusToggle(Assignment assignment) {
        repository.toggleCompletion(assignment.getId());
        Assignment updated = repository.getAssignmentById(assignment.getId());
        if (updated != null) {
            if (updated.isCompleted()) {
                ReminderManager.cancelReminder(this, updated.getId());
            } else {
                ReminderManager.scheduleReminder(this, updated);
            }
        }
        loadDashboardData();
        Toast.makeText(this, R.string.toast_status_updated, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(Assignment assignment) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, assignment.getTitle()))
                .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                    ReminderManager.cancelReminder(this, assignment.getId());
                    repository.deleteAssignment(assignment.getId());
                    loadDashboardData();
                    Toast.makeText(this, R.string.toast_assignment_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void onEditClick(Assignment assignment) {
        Intent intent = new Intent(MainActivity.this, AddAssignmentActivity.class);
        intent.putExtra(AddAssignmentActivity.EXTRA_ASSIGNMENT, assignment);
        addAssignmentLauncher.launch(intent);
    }
}
