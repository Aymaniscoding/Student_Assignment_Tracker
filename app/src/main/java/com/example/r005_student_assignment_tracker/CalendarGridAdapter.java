package com.example.r005_student_assignment_tracker;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarGridAdapter extends RecyclerView.Adapter<CalendarGridAdapter.ViewHolder> {

    public interface OnDateClickListener {
        void onDateClick(Calendar date);
    }

    public static class DayItem {
        Calendar date;
        boolean isCurrentMonth;
        boolean isSelected;

        public DayItem(Calendar date, boolean isCurrentMonth, boolean isSelected) {
            this.date = date;
            this.isCurrentMonth = isCurrentMonth;
            this.isSelected = isSelected;
        }
    }

    private final Context context;
    private final List<DayItem> days = new ArrayList<>();
    private final List<Assignment> assignments = new ArrayList<>();
    private final OnDateClickListener listener;

    public CalendarGridAdapter(Context context, OnDateClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setDays(List<DayItem> days, List<Assignment> assignments) {
        this.days.clear();
        if (days != null) this.days.addAll(days);

        this.assignments.clear();
        if (assignments != null) this.assignments.addAll(assignments);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DayItem dayItem = days.get(position);
        Calendar dayCal = dayItem.date;

        int dayOfMonth = dayCal.get(Calendar.DAY_OF_MONTH);
        holder.tvDayNumber.setText(String.valueOf(dayOfMonth));

        if (!dayItem.isCurrentMonth) {
            holder.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.text_muted));
            holder.cardCalendarDay.setCardBackgroundColor(ColorStateList.valueOf(Color.TRANSPARENT));
            holder.cardCalendarDay.setStrokeColor(ColorStateList.valueOf(Color.TRANSPARENT));
            holder.cardCalendarDay.setOnClickListener(null);
            return;
        }

        // Evaluate uncompleted assignments for this day
        long now = System.currentTimeMillis();
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);

        boolean hasMissed = false;   // Overdue (past date/time) -> Red
        boolean hasDueSoon = false;  // Upcoming in 0-2 days -> Yellow
        boolean hasDueLater = false; // Due in 3+ days -> Green

        for (Assignment a : assignments) {
            if (a.isCompleted()) continue;
            if (a.getDueDateMillis() <= 0) continue;

            if (isSameDay(a.getDueDateMillis(), dayCal)) {
                if (a.getDueDateMillis() < todayStart.getTimeInMillis() || a.getDueDateMillis() < now) {
                    hasMissed = true; // Overdue / Missed -> RED
                } else {
                    long diffMillis = a.getDueDateMillis() - now;
                    long diffDays = diffMillis / (1000 * 60 * 60 * 24);

                    if (diffDays <= 2) {
                        hasDueSoon = true; // Due in 0-2 days -> YELLOW
                    } else {
                        hasDueLater = true; // Due in 3+ days -> GREEN
                    }
                }
            }
        }

        // Apply Colors: Red = Missed (Overdue), Yellow = Due Soon (1-2 days), Green = Due Later (3+ days)
        if (hasMissed) {
            holder.cardCalendarDay.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.priority_high)));
            holder.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else if (hasDueSoon) {
            holder.cardCalendarDay.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.priority_medium)));
            holder.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else if (hasDueLater) {
            holder.cardCalendarDay.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_completed)));
            holder.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            holder.cardCalendarDay.setCardBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.surface_dark)));
            holder.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
        }

        // Selection stroke highlight
        if (dayItem.isSelected) {
            holder.cardCalendarDay.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)));
            holder.cardCalendarDay.setStrokeWidth(4);
        } else {
            holder.cardCalendarDay.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.card_border)));
            holder.cardCalendarDay.setStrokeWidth(1);
        }

        holder.cardCalendarDay.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDateClick(dayCal);
            }
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    private boolean isSameDay(long millis1, Calendar cal2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(millis1);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardCalendarDay;
        TextView tvDayNumber;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardCalendarDay = itemView.findViewById(R.id.cardCalendarDay);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
        }
    }
}
