package com.example.fitnesstracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FitnessAdapter extends RecyclerView.Adapter<FitnessAdapter.ViewHolder> {

    private final ArrayList<FitnessModel> list;
    private final OnActivityActionListener listener;

    public interface OnActivityActionListener {
        void onEdit(FitnessModel model);
        void onDelete(int id);
    }

    public FitnessAdapter(ArrayList<FitnessModel> list, OnActivityActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView workout, duration, calories, category, date;
        ImageButton btnEdit, btnDelete;
        View colorTag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            workout = itemView.findViewById(R.id.txtWorkout);
            duration = itemView.findViewById(R.id.txtDuration);
            calories = itemView.findViewById(R.id.txtCalories);
            category = itemView.findViewById(R.id.txtCategory);
            date = itemView.findViewById(R.id.txtDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            colorTag = itemView.findViewById(R.id.viewColorTag);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fitness, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FitnessModel model = list.get(position);
        holder.workout.setText(model.getWorkout());
        
        holder.duration.setText(holder.itemView.getContext().getString(R.string.duration_label, model.getDuration()));
        holder.calories.setText(holder.itemView.getContext().getString(R.string.calories_label, model.getCalories()));
        
        holder.category.setText(model.getCategory());
        holder.date.setText(model.getDate());

        // Set color based on category
        int colorRes = R.color.color_other;
        String cat = model.getCategory().toLowerCase();
        if (cat.contains("cardio") || cat.contains("run") || cat.contains("cycl")) {
            colorRes = R.color.color_cardio;
        } else if (cat.contains("strength")) {
            colorRes = R.color.color_strength;
        } else if (cat.contains("yoga")) {
            colorRes = R.color.color_yoga;
        }
        
        if (holder.colorTag != null) {
            holder.colorTag.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), colorRes));
        }
        holder.category.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), colorRes));

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(model));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(model.getId()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}