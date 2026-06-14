package com.example.fitnesstracker;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements FitnessAdapter.OnActivityActionListener {

    private static final String TAG = "MainActivity";
    private static final int DAILY_GOAL = 2000;

    private TextInputEditText etWorkout, etDuration, etCalories, etSearch, etWeight, etHeight;
    private Spinner spinnerCategory;
    private Button btnSave;
    private TextView txtBMIResult, txtGoalStatus, txtBMIResultShort, txtGoalPercent;
    private LinearProgressIndicator progressDailyGoal;
    private RecyclerView recyclerView;
    private BarChart barChart;
    private ImageButton btnThemeToggle;

    private DatabaseHelper db;
    private ArrayList<FitnessModel> list;
    private FitnessAdapter adapter;

    private int totalCalories = 0;
    private int currentEditId = -1;

    private final String[] categories = {"Cardio", "Strength", "Yoga", "Running", "Cycling", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadThemePreference();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        initViews();
        setupRecyclerView();
        setupSpinner();
        loadData("");
        setupListeners();
    }

    private void initViews() {
        etWorkout = findViewById(R.id.etWorkout);
        etDuration = findViewById(R.id.etDuration);
        etCalories = findViewById(R.id.etCalories);
        etSearch = findViewById(R.id.etSearch);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.btnSave);
        
        Button btnCalculateBMI = findViewById(R.id.btnCalculateBMI);
        Button btnExport = findViewById(R.id.btnExport);
        
        txtBMIResult = findViewById(R.id.txtBMIResult);
        txtGoalStatus = findViewById(R.id.txtGoalStatus);
        txtBMIResultShort = findViewById(R.id.txtBMIResultShort);
        txtGoalPercent = findViewById(R.id.txtGoalPercent);
        
        progressDailyGoal = findViewById(R.id.progressDailyGoal);
        recyclerView = findViewById(R.id.recyclerView);
        barChart = findViewById(R.id.barChart);
        btnThemeToggle = findViewById(R.id.btnThemeToggle);

        btnCalculateBMI.setOnClickListener(v -> calculateBMI());
        btnExport.setOnClickListener(v -> exportToCSV());
        btnThemeToggle.setOnClickListener(v -> toggleTheme());
        updateThemeIcon();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        adapter = new FitnessAdapter(list, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(spinnerAdapter);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> {
            if (validateInput()) {
                saveActivity();
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadData(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private boolean validateInput() {
        boolean isValid = true;
        if (etWorkout.getText() == null || TextUtils.isEmpty(etWorkout.getText().toString())) {
            etWorkout.setError(getString(R.string.required));
            isValid = false;
        }
        if (etDuration.getText() == null || TextUtils.isEmpty(etDuration.getText().toString())) {
            etDuration.setError(getString(R.string.required));
            isValid = false;
        }
        if (etCalories.getText() == null || TextUtils.isEmpty(etCalories.getText().toString())) {
            etCalories.setError(getString(R.string.required));
            isValid = false;
        }
        return isValid;
    }

    private void saveActivity() {
        if (etWorkout.getText() == null || etDuration.getText() == null || etCalories.getText() == null) return;

        String workout = etWorkout.getText().toString();
        int duration = Integer.parseInt(etDuration.getText().toString());
        int calories = Integer.parseInt(etCalories.getText().toString());
        String category = spinnerCategory.getSelectedItem().toString();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (currentEditId == -1) {
            db.insertActivity(workout, duration, calories, category, date);
            Toast.makeText(this, R.string.activity_added, Toast.LENGTH_SHORT).show();
        } else {
            db.updateActivity(currentEditId, workout, duration, calories, category, date);
            Toast.makeText(this, R.string.activity_updated, Toast.LENGTH_SHORT).show();
            currentEditId = -1;
            btnSave.setText(R.string.add_activity);
        }

        clearFields();
        loadData("");
    }

    private void clearFields() {
        etWorkout.setText("");
        etDuration.setText("");
        etCalories.setText("");
        etWorkout.clearFocus();
    }

    private void loadData(String query) {
        list.clear();
        totalCalories = 0;
        Cursor cursor;
        if (query.isEmpty()) {
            cursor = db.getActivities();
        } else {
            cursor = db.searchActivities(query);
        }

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String workout = cursor.getString(1);
                int duration = cursor.getInt(2);
                int calories = cursor.getInt(3);
                String category = cursor.getString(4);
                String date = cursor.getString(5);

                if (date.equals(today)) {
                    totalCalories += calories;
                }
                list.add(new FitnessModel(id, workout, duration, calories, category, date));
            }
            cursor.close();
        }
        
        // Refresh the list efficiently
        adapter.notifyDataSetChanged();

        updateProgress();
        updateChart();
    }

    private void updateProgress() {
        txtGoalStatus.setText(getString(R.string.goal_status, totalCalories, DAILY_GOAL));
        int progress = (int) (((float) totalCalories / DAILY_GOAL) * 100);
        progressDailyGoal.setProgress(Math.min(progress, 100));
        txtGoalPercent.setText(getString(R.string.percent_format, Math.min(progress, 100)));
    }

    private void updateChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        Cursor cursor = db.getWeeklyStats();
        if (cursor != null) {
            int i = 0;
            while (cursor.moveToNext() && i < 7) {
                entries.add(new BarEntry(i, cursor.getFloat(1)));
                i++;
            }
            cursor.close();
        }

        Collections.reverse(entries);

        BarDataSet dataSet = new BarDataSet(entries, getString(R.string.calories));
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(10f);

        int grayColor = ContextCompat.getColor(this, android.R.color.darker_gray);
        dataSet.setValueTextColor(grayColor);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setTextColor(grayColor);
        barChart.getAxisLeft().setTextColor(grayColor);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setTextColor(grayColor);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private void calculateBMI() {
        if (etWeight.getText() == null || etHeight.getText() == null) return;
        String wStr = etWeight.getText().toString();
        String hStr = etHeight.getText().toString();
        if (!wStr.isEmpty() && !hStr.isEmpty()) {
            float weight = Float.parseFloat(wStr);
            float height = Float.parseFloat(hStr) / 100;
            if (height > 0) {
                float bmi = weight / (height * height);
                String result = String.format(Locale.getDefault(), "%.1f", bmi);
                txtBMIResult.setText(getString(R.string.bmi_result, result));
                txtBMIResultShort.setText(result);
            }
        } else {
            Toast.makeText(this, R.string.enter_weight_height, Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleTheme() {
        SharedPreferences pref = getSharedPreferences("ThemePref", MODE_PRIVATE);
        boolean isDarkMode = pref.getBoolean("isDarkMode", false);
        saveThemePreference(!isDarkMode);

        if (!isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        recreate();
    }

    private void saveThemePreference(boolean isDarkMode) {
        SharedPreferences pref = getSharedPreferences("ThemePref", MODE_PRIVATE);
        pref.edit().putBoolean("isDarkMode", isDarkMode).apply();
    }

    private void loadThemePreference() {
        SharedPreferences pref = getSharedPreferences("ThemePref", MODE_PRIVATE);
        boolean isDarkMode = pref.getBoolean("isDarkMode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void updateThemeIcon() {
        SharedPreferences pref = getSharedPreferences("ThemePref", MODE_PRIVATE);
        boolean isDarkMode = pref.getBoolean("isDarkMode", false);
        if (isDarkMode) {
            btnThemeToggle.setImageResource(android.R.drawable.ic_menu_day);
        } else {
            btnThemeToggle.setImageResource(android.R.drawable.ic_menu_recent_history);
        }
    }

    @Override
    public void onEdit(FitnessModel model) {
        currentEditId = model.getId();
        if (etWorkout != null) etWorkout.setText(model.getWorkout());
        if (etDuration != null) etDuration.setText(String.valueOf(model.getDuration()));
        if (etCalories != null) etCalories.setText(String.valueOf(model.getCalories()));
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(model.getCategory())) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
        btnSave.setText(R.string.update_activity);
        if (etWorkout != null) etWorkout.requestFocus();
    }

    @Override
    public void onDelete(int id) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_activity_title)
                .setMessage(R.string.delete_activity_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    db.deleteActivity(id);
                    loadData("");
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void exportToCSV() {
        File folder = getExternalFilesDir("FitnessData");
        if (folder != null && !folder.exists()) {
            if (!folder.mkdirs()) {
                Log.e(TAG, "Failed to create directory");
            }
        }

        if (folder == null) {
            Toast.makeText(this, R.string.export_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        String filename = folder.getAbsolutePath() + "/FitnessTracker_" + System.currentTimeMillis() + ".csv";

        try (FileWriter writer = new FileWriter(filename)) {
            writer.append("ID,Workout,Duration,Calories,Category,Date\n");
            for (FitnessModel m : list) {
                writer.append(String.valueOf(m.getId())).append(",")
                        .append(m.getWorkout()).append(",")
                        .append(String.valueOf(m.getDuration())).append(",")
                        .append(String.valueOf(m.getCalories())).append(",")
                        .append(m.getCategory()).append(",")
                        .append(m.getDate()).append("\n");
            }
            Toast.makeText(this, getString(R.string.exported_to, filename), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e(TAG, "Export failed", e);
            Toast.makeText(this, R.string.export_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
