package com.example.fitnesstracker;

public class FitnessModel {
    private int id;
    private String workout;
    private int duration;
    private int calories;
    private String category;
    private String date;

    public FitnessModel(int id, String workout, int duration, int calories, String category, String date) {
        this.id = id;
        this.workout = workout;
        this.duration = duration;
        this.calories = calories;
        this.category = category;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getWorkout() {
        return workout;
    }

    public int getDuration() {
        return duration;
    }

    public int getCalories() {
        return calories;
    }

    public String getCategory() {
        return category;
    }

    public String getDate() {
        return date;
    }
}