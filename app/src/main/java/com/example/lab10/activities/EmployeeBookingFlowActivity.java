package com.example.lab10.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.example.lab10.R;

public class EmployeeBookingFlowActivity extends AppCompatActivity {

    private static final int TOTAL_STEPS = 5;

    private int currentStep = 0;
    private TextView[] stepIndicators;
    private LinearLayout[] stepContainers;
    private Button btnPrevious;
    private Button btnNext;
    private Button btnRestartFlow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_booking_flow);

        setupToolbar();
        initViews();
        setupActions();
        renderStep();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_booking_flow);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Employee Booking Flow");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        stepIndicators = new TextView[] {
                findViewById(R.id.step_movie),
                findViewById(R.id.step_time),
                findViewById(R.id.step_seat),
                findViewById(R.id.step_confirm),
                findViewById(R.id.step_done)
        };

        stepContainers = new LinearLayout[] {
                findViewById(R.id.layout_step_movie),
                findViewById(R.id.layout_step_time),
                findViewById(R.id.layout_step_seat),
                findViewById(R.id.layout_step_confirm),
                findViewById(R.id.layout_step_done)
        };

        btnPrevious = findViewById(R.id.btn_previous_step);
        btnNext = findViewById(R.id.btn_next_step);
        btnRestartFlow = findViewById(R.id.btn_restart_flow);
    }

    private void setupActions() {
        btnPrevious.setOnClickListener(v -> {
            if (currentStep > 0) {
                currentStep--;
                renderStep();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentStep < TOTAL_STEPS - 1) {
                currentStep++;
                renderStep();
            } else {
                finish();
            }
        });

        btnRestartFlow.setOnClickListener(v -> {
            currentStep = 0;
            renderStep();
        });
    }

    private void renderStep() {
        for (int i = 0; i < stepContainers.length; i++) {
            stepContainers[i].setVisibility(i == currentStep ? LinearLayout.VISIBLE : LinearLayout.GONE);
        }

        for (int i = 0; i < stepIndicators.length; i++) {
            if (i < currentStep) {
                applyIndicatorStyle(stepIndicators[i], R.drawable.bg_step_done, android.R.color.white);
            } else if (i == currentStep) {
                applyIndicatorStyle(stepIndicators[i], R.drawable.bg_step_active, android.R.color.white);
            } else {
                applyIndicatorStyle(stepIndicators[i], R.drawable.bg_step_idle, android.R.color.darker_gray);
            }
        }

        btnPrevious.setEnabled(currentStep > 0);
        btnPrevious.setAlpha(currentStep > 0 ? 1f : 0.5f);

        if (currentStep == TOTAL_STEPS - 1) {
            btnNext.setText("Đóng");
        } else {
            btnNext.setText("Tiếp tục");
        }
    }

    private void applyIndicatorStyle(TextView indicator, int backgroundRes, int textColorRes) {
        indicator.setBackgroundResource(backgroundRes);
        indicator.setTextColor(ContextCompat.getColor(this, textColorRes));
    }
}
