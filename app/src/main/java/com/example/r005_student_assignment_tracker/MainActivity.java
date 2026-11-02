package com.example.r005_student_assignment_tracker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements AssignmentAdapter.OnAssignmentActionListener {

    // Views: Home Tab
    private LinearLayout layoutHome, layoutAssignments, layoutCalendar, layoutProfile;
    private TextView tvHomeGreeting, tvProgressPercent, tvProgressSummary;
    private LinearProgressIndicator progressProductivity;
    private TextView tvStatTotal, tvStatPending, tvStatCompleted, tvStatMissed, tvStatHighPriority;

    // Views: Assignments Tab
    private TextView tvCountBadge;
    private TextInputEditText etSearch;
    private ChipGroup chipGroupFilter;
    private RecyclerView rvAssignments;
    private LinearLayout layoutEmptyState;

    // Views: Calendar Tab
    private ImageButton btnPrevMonth, btnNextMonth;
    private TextView tvCalendarMonthTitle, tvCalendarSelectedDate;
    private RecyclerView rvCalendarGrid, rvCalendarAssignments;

    // Views: Profile Tab
    private TextView tvProfileName, tvProfileEmail, tvProfileTotal, tvProfileCompleted, tvProfileRate;
    private MaterialButton btnEditProfile, btnLogout;

    // Common Navigation
    private BottomNavigationView bottomNav;
    private ExtendedFloatingActionButton fabAddAssignment;

    // Data & Helpers
    private AssignmentRepository repository;
    private AuthManager authManager;
    private AssignmentAdapter adapter;
    private AssignmentAdapter calendarAdapter;
    private CalendarGridAdapter calendarGridAdapter;

    private ActivityResultLauncher<Intent> addAssignmentLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    private String currentFilter = "All";
    private String currentSearchQuery = "";

    private Calendar currentDisplayMonth = Calendar.getInstance();
    private Calendar selectedCalendarDate = Calendar.getInstance();
    private final SimpleDateFormat calendarDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

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

        authManager = new AuthManager(this);

        if (!authManager.isUserLoggedIn()) {
            redirectToLogin();
            return;
        }

        repository = new AssignmentRepository(this);

        setupPermissionLauncher();
        checkNotificationPermission();

        initViews();
        setupBottomNavigation();
        setupSearch();
        setupRecyclerViews();
        setupFilterChips();
        setupCalendarView();
        setupProfileActions();
        setupActivityLauncher();
        setupFab();

        // Sync Firestore data for user
        repository.syncFromFirestore(authManager.getCurrentUserId(), this::loadDashboardData);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!authManager.isUserLoggedIn()) {
            redirectToLogin();
            return;
        }
        loadDashboardData();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
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

    private void initViews() {
        // Content Layout Containers
        layoutHome = findViewById(R.id.layoutHome);
        layoutAssignments = findViewById(R.id.layoutAssignments);
        layoutCalendar = findViewById(R.id.layoutCalendar);
        layoutProfile = findViewById(R.id.layoutProfile);

        // Home Tab
        tvHomeGreeting = findViewById(R.id.tvHomeGreeting);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tvProgressSummary = findViewById(R.id.tvProgressSummary);
        progressProductivity = findViewById(R.id.progressProductivity);

        tvStatTotal = findViewById(R.id.tvStatTotal);
        tvStatPending = findViewById(R.id.tvStatPending);
        tvStatCompleted = findViewById(R.id.tvStatCompleted);
        tvStatMissed = findViewById(R.id.tvStatMissed);
        tvStatHighPriority = findViewById(R.id.tvStatHighPriority);

        // Assignments Tab
        tvCountBadge = findViewById(R.id.tvCountBadge);
        etSearch = findViewById(R.id.etSearch);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        rvAssignments = findViewById(R.id.rvAssignments);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        // Calendar Tab
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        tvCalendarMonthTitle = findViewById(R.id.tvCalendarMonthTitle);
        rvCalendarGrid = findViewById(R.id.rvCalendarGrid);
        tvCalendarSelectedDate = findViewById(R.id.tvCalendarSelectedDate);
        rvCalendarAssignments = findViewById(R.id.rvCalendarAssignments);

        // Profile Tab
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileTotal = findViewById(R.id.tvProfileTotal);
        tvProfileCompleted = findViewById(R.id.tvProfileCompleted);
        tvProfileRate = findViewById(R.id.tvProfileRate);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);

        // Bottom Nav & FAB
        bottomNav = findViewById(R.id.bottomNav);
        fabAddAssignment = findViewById(R.id.fabAddAssignment);
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                showTab(layoutHome);
                fabAddAssignment.setVisibility(View.VISIBLE);
                return true;
            } else if (itemId == R.id.nav_assignments) {
                showTab(layoutAssignments);
                fabAddAssignment.setVisibility(View.VISIBLE);
                return true;
            } else if (itemId == R.id.nav_calendar) {
                showTab(layoutCalendar);
                fabAddAssignment.setVisibility(View.VISIBLE);
                return true;
            } else if (itemId == R.id.nav_profile) {
                showTab(layoutProfile);
                fabAddAssignment.setVisibility(View.GONE);
                return true;
            }
            return false;
        });
    }

    private void showTab(View activeTab) {
        layoutHome.setVisibility(View.GONE);
        layoutAssignments.setVisibility(View.GONE);
        layoutCalendar.setVisibility(View.GONE);
        layoutProfile.setVisibility(View.GONE);

        activeTab.setVisibility(View.VISIBLE);
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

    private void setupRecyclerViews() {
        adapter = new AssignmentAdapter(this, this);
        adapter.setTeacherMode(true);
        rvAssignments.setLayoutManager(new LinearLayoutManager(this));
        rvAssignments.setAdapter(adapter);

        calendarAdapter = new AssignmentAdapter(this, this);
        calendarAdapter.setTeacherMode(true);
        rvCalendarAssignments.setLayoutManager(new LinearLayoutManager(this));
        rvCalendarAssignments.setAdapter(calendarAdapter);
    }

    private void setupFilterChips() {
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipFilterPending) {
                currentFilter = "Pending";
            } else if (checkedId == R.id.chipFilterCompleted) {
                currentFilter = "Completed";
            } else if (checkedId == R.id.chipFilterMissed) {
                currentFilter = "Missed";
            } else if (checkedId == R.id.chipFilterHigh) {
                currentFilter = "High";
            } else {
                currentFilter = "All";
            }
            loadDashboardData();
        });
    }

    private void setupCalendarView() {
        if (rvCalendarGrid != null) {
            rvCalendarGrid.setLayoutManager(new GridLayoutManager(this, 7));
            calendarGridAdapter = new CalendarGridAdapter(this, date -> {
                selectedCalendarDate = (Calendar) date.clone();
                loadCalendarData();
            });
            rvCalendarGrid.setAdapter(calendarGridAdapter);
        }

        if (btnPrevMonth != null) {
            btnPrevMonth.setOnClickListener(v -> {
                currentDisplayMonth.add(Calendar.MONTH, -1);
                loadCalendarData();
            });
        }

        if (btnNextMonth != null) {
            btnNextMonth.setOnClickListener(v -> {
                currentDisplayMonth.add(Calendar.MONTH, 1);
                loadCalendarData();
            });
        }
    }

    private void setupProfileActions() {
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        btnLogout.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_logout_title)
                .setMessage(R.string.dialog_logout_message)
                .setPositiveButton(R.string.btn_logout, (dialog, which) -> {
                    authManager.logout();
                    redirectToLogin();
                })
                .setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.dismiss())
                .show());
    }

    private void showEditProfileDialog() {
        final TextInputLayout dialogTil = new TextInputLayout(this);
        dialogTil.setHint("Full Name");
        dialogTil.setPadding(32, 16, 32, 0);

        final TextInputEditText input = new TextInputEditText(this);
        input.setText(authManager.getCurrentUserName());
        dialogTil.addView(input);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.btn_edit_profile)
                .setView(dialogTil)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = input.getText() != null ? input.getText().toString().trim() : "";
                    if (!newName.isEmpty()) {
                        authManager.updateUserProfileName(newName, new AuthManager.AuthCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(MainActivity.this, R.string.toast_profile_updated, Toast.LENGTH_SHORT).show();
                                loadDashboardData();
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.dismiss())
                .show();
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
        String currentUserId = authManager.getCurrentUserId();
        List<Assignment> allAssignments = repository.getAllAssignments(currentUserId);

        int totalCount = allAssignments.size();
        int pendingCount = 0;
        int completedCount = 0;
        int missedCount = 0;
        int highPriorityCount = 0;

        List<Assignment> filteredList = new ArrayList<>();
        long now = System.currentTimeMillis();

        for (Assignment assignment : allAssignments) {
            boolean isMissed = !assignment.isCompleted() && assignment.getDueDateMillis() > 0 && assignment.getDueDateMillis() < now;

            if (assignment.isCompleted()) {
                completedCount++;
            } else {
                pendingCount++;
            }

            if (isMissed) {
                missedCount++;
            }

            if ("High".equalsIgnoreCase(assignment.getPriority())) {
                highPriorityCount++;
            }

            // Search Query
            boolean matchesSearch = currentSearchQuery.isEmpty()
                    || (assignment.getTitle() != null && assignment.getTitle().toLowerCase().contains(currentSearchQuery))
                    || (assignment.getSubject() != null && assignment.getSubject().toLowerCase().contains(currentSearchQuery));

            // Filter Choice
            boolean matchesFilter = false;
            switch (currentFilter) {
                case "Pending":
                    matchesFilter = !assignment.isCompleted();
                    break;
                case "Completed":
                    matchesFilter = assignment.isCompleted();
                    break;
                case "Missed":
                    matchesFilter = isMissed;
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

        // Update Greetings & User Profile Info
        String userName = authManager.getCurrentUserName();
        tvHomeGreeting.setText("Welcome Back, " + userName + "!");

        tvProfileName.setText(userName);
        tvProfileEmail.setText(authManager.getCurrentUserEmail());

        // Update Progress Bar Card
        int progressPercent = totalCount > 0 ? (completedCount * 100) / totalCount : 0;
        progressProductivity.setProgress(progressPercent);
        tvProgressPercent.setText(progressPercent + "%");
        tvProgressSummary.setText(completedCount + " of " + totalCount + " tasks completed");

        // Update Overview Stat Cards
        tvStatTotal.setText(String.valueOf(totalCount));
        tvStatPending.setText(String.valueOf(pendingCount));
        tvStatCompleted.setText(String.valueOf(completedCount));
        if (tvStatMissed != null) tvStatMissed.setText(String.valueOf(missedCount));
        tvStatHighPriority.setText(String.valueOf(highPriorityCount));

        // Update Profile Tab Stats
        tvProfileTotal.setText(String.valueOf(totalCount));
        tvProfileCompleted.setText(String.valueOf(completedCount));
        tvProfileRate.setText(progressPercent + "%");

        // Update Assignments Tab Badge & List Visibility
        tvCountBadge.setText(String.valueOf(filteredList.size()));

        if (filteredList.isEmpty()) {
            rvAssignments.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);

            TextView tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
            TextView tvEmptySubtitle = findViewById(R.id.tvEmptySubtitle);

            if (tvEmptyTitle != null && tvEmptySubtitle != null) {
                if (!currentSearchQuery.isEmpty() || !"All".equalsIgnoreCase(currentFilter)) {
                    tvEmptyTitle.setText(R.string.empty_search_title);
                    tvEmptySubtitle.setText(R.string.empty_search_subtitle);
                } else {
                    tvEmptyTitle.setText(R.string.empty_state_title);
                    tvEmptySubtitle.setText(R.string.empty_state_subtitle);
                }
            }
        } else {
            rvAssignments.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
            adapter.setAssignments(filteredList);
        }

        loadCalendarData();
    }

    private void loadCalendarData() {
        String currentUserId = authManager.getCurrentUserId();
        List<Assignment> allAssignments = repository.getAllAssignments(currentUserId);

        // Update Month Header Title
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.US);
        if (tvCalendarMonthTitle != null) {
            tvCalendarMonthTitle.setText(monthFormat.format(currentDisplayMonth.getTime()));
        }

        // Generate Day Items for Calendar Grid
        List<CalendarGridAdapter.DayItem> daysList = new ArrayList<>();
        Calendar monthCal = (Calendar) currentDisplayMonth.clone();
        monthCal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = monthCal.get(Calendar.DAY_OF_WEEK) - 1; // 0 for Sunday
        monthCal.add(Calendar.DAY_OF_MONTH, -firstDayOfWeek);

        for (int i = 0; i < 35; i++) {
            boolean isCurrentMonth = monthCal.get(Calendar.MONTH) == currentDisplayMonth.get(Calendar.MONTH);
            boolean isSelected = isSameDay(monthCal, selectedCalendarDate);

            daysList.add(new CalendarGridAdapter.DayItem((Calendar) monthCal.clone(), isCurrentMonth, isSelected));
            monthCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (calendarGridAdapter != null) {
            calendarGridAdapter.setDays(daysList, allAssignments);
        }

        String selectedDateFormatted = calendarDateFormat.format(selectedCalendarDate.getTime());
        if (tvCalendarSelectedDate != null) {
            tvCalendarSelectedDate.setText("Tasks Due on " + selectedDateFormatted);
        }

        List<Assignment> calendarAssignments = new ArrayList<>();
        for (Assignment assignment : allAssignments) {
            if (assignment.getDueDate() != null && assignment.getDueDate().equalsIgnoreCase(selectedDateFormatted)) {
                calendarAssignments.add(assignment);
            }
        }

        if (calendarAdapter != null) {
            calendarAdapter.setAssignments(calendarAssignments);
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
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
                    repository.deleteAssignment(authManager.getCurrentUserId(), assignment.getId());
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
