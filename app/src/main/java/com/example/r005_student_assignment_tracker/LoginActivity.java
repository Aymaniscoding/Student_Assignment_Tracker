package com.example.r005_student_assignment_tracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView btnForgotPassword, btnRegisterLink;
    private ProgressBar progressBar;

    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authManager = new AuthManager(this);

        if (authManager.isUserLoggedIn()) {
            openMainActivity();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        btnRegisterLink = findViewById(R.id.btnRegisterLink);

        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> performLogin());

        btnRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void performLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        boolean isValid = true;

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_email_invalid));
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        if (password.isEmpty() || password.length() < 6) {
            tilPassword.setError(getString(R.string.error_password_short));
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        if (!isValid) return;

        showLoading(true);

        authManager.login(email, password, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess() {
                showLoading(false);
                Toast.makeText(LoginActivity.this, R.string.toast_login_success, Toast.LENGTH_SHORT).show();
                openMainActivity();
            }

            @Override
            public void onFailure(String errorMessage) {
                showLoading(false);
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showForgotPasswordDialog() {
        final TextInputLayout dialogTil = new TextInputLayout(this);
        dialogTil.setHint("Email Address");
        dialogTil.setPadding(32, 16, 32, 0);

        final TextInputEditText input = new TextInputEditText(this);
        if (etEmail.getText() != null) {
            input.setText(etEmail.getText().toString().trim());
        }
        dialogTil.addView(input);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_forgot_title)
                .setMessage(R.string.dialog_forgot_message)
                .setView(dialogTil)
                .setPositiveButton(R.string.btn_send_reset, (dialog, which) -> {
                    String resetEmail = input.getText() != null ? input.getText().toString().trim() : "";
                    if (resetEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(resetEmail).matches()) {
                        Toast.makeText(this, R.string.error_email_invalid, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    showLoading(true);
                    authManager.sendPasswordReset(resetEmail, new AuthManager.AuthCallback() {
                        @Override
                        public void onSuccess() {
                            showLoading(false);
                            Toast.makeText(LoginActivity.this, R.string.toast_reset_sent, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            showLoading(false);
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void openMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnLogin != null) {
            btnLogin.setEnabled(!isLoading);
        }
    }
}
