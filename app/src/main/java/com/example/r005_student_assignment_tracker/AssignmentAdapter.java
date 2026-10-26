package com.example.r005_student_assignment_tracker;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.ViewHolder> {

    public interface OnAssignmentActionListener {
        void onStatusToggle(Assignment assignment);
        void onDeleteClick(Assignment assignment);
    }

    private final Context context;
    private List<Assignment> assignments = new ArrayList<>();
    private final OnAssignmentActionListener listener;

    public AssignmentAdapter(Context context, OnAssignmentActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_assignment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Assignment assignment = assignments.get(position);

        holder.tvTitle.setText(assignment.getTitle());
        holder.tvSubjectTag.setText(assignment.getSubject());

        if (assignment.getDescription() == null || assignment.getDescription().trim().isEmpty()) {
            holder.tvDescription.setVisibility(View.GONE);
        } else {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(assignment.getDescription());
        }

        holder.tvDueDate.setText(context.getString(R.string.label_due_date_format, assignment.getDueDate()));

        // Priority styling
        String priority = assignment.getPriority();
        if ("High".equalsIgnoreCase(priority)) {
            holder.tvPriorityBadge.setText(R.string.priority_high);
            holder.tvPriorityBadge.setBackgroundResource(R.drawable.bg_priority_high);
            holder.tvPriorityBadge.setTextColor(ContextCompat.getColor(context, R.color.priority_high));
        } else if ("Low".equalsIgnoreCase(priority)) {
            holder.tvPriorityBadge.setText(R.string.priority_low);
            holder.tvPriorityBadge.setBackgroundResource(R.drawable.bg_priority_low);
            holder.tvPriorityBadge.setTextColor(ContextCompat.getColor(context, R.color.priority_low));
        } else {
            holder.tvPriorityBadge.setText(R.string.priority_medium);
            holder.tvPriorityBadge.setBackgroundResource(R.drawable.bg_priority_medium);
            holder.tvPriorityBadge.setTextColor(ContextCompat.getColor(context, R.color.priority_medium));
        }

        // Completion status chip & strikethrough styling
        if (assignment.isCompleted()) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text_muted));
            holder.chipStatus.setText(R.string.stat_completed);
            holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_completed_light)));
            holder.chipStatus.setTextColor(ContextCompat.getColor(context, R.color.status_completed));
            holder.chipStatus.setChipIconResource(R.drawable.ic_check_circle);
            holder.chipStatus.setChipIconTint(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_completed)));
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            holder.chipStatus.setText(R.string.stat_pending);
            holder.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_pending_light)));
            holder.chipStatus.setTextColor(ContextCompat.getColor(context, R.color.status_pending));
            holder.chipStatus.setChipIconResource(R.drawable.ic_clock);
            holder.chipStatus.setChipIconTint(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_pending)));
        }

        holder.chipStatus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStatusToggle(assignment);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(assignment);
            }
        });
    }

    @Override
    public int getItemCount() {
        return assignments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardAssignment;
        TextView tvSubjectTag, tvPriorityBadge, tvTitle, tvDescription, tvDueDate;
        Chip chipStatus;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardAssignment = itemView.findViewById(R.id.cardAssignment);
            tvSubjectTag = itemView.findViewById(R.id.tvSubjectTag);
            tvPriorityBadge = itemView.findViewById(R.id.tvPriorityBadge);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
