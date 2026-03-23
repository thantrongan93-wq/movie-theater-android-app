package com.example.lab10.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.R;
import com.example.lab10.models.ShowtimeGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class BookingShowtimeAdapter extends RecyclerView.Adapter<BookingShowtimeAdapter.GroupViewHolder> {
    
    private List<ShowtimeGroup> groups;
    private OnShowtimeIdClickListener listener;
    private Long selectedShowtimeId = null;
    
    public interface OnShowtimeIdClickListener {
        void onShowtimeClick(Long showtimeId);
    }

    public BookingShowtimeAdapter(List<ShowtimeGroup> groups, OnShowtimeIdClickListener listener) {
        this.groups = groups != null ? groups : new ArrayList<>();
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_showtime_group, parent, false);
        return new GroupViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        holder.bind(groups.get(position), listener, selectedShowtimeId);
    }
    
    @Override
    public int getItemCount() {
        return groups.size();
    }
    
    public void updateGroupData(List<ShowtimeGroup> newGroups) {
        this.groups = newGroups != null ? newGroups : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setSelectedShowtimeId(Long showtimeId) {
        this.selectedShowtimeId = showtimeId;
        notifyDataSetChanged();
    }
    
    static class GroupViewHolder extends RecyclerView.ViewHolder {
        private TextView tvGroupDate;
        private ChipGroup layoutTimesContainer;
        
        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupDate = itemView.findViewById(R.id.tv_group_date);
            layoutTimesContainer = itemView.findViewById(R.id.layout_times_container);
        }

        public void bind(ShowtimeGroup group, OnShowtimeIdClickListener listener, Long selectedShowtimeId) {
            tvGroupDate.setText(group.getDate());
            layoutTimesContainer.removeAllViews();

            if (group.getShowtimes() != null) {
                for (ShowtimeGroup.ShowtimeInfo info : group.getShowtimes()) {
                    Chip chip = new Chip(itemView.getContext());
                    
                    // Format time to HH:mm
                    String rawTime = info.getTime();
                    String formattedTime = rawTime.length() >= 5 ? rawTime.substring(0, 5) : rawTime;
                    chip.setText(formattedTime);
                    
                    chip.setCheckable(true);
                    chip.setClickable(true);

                    boolean isSelected = info.getShowtimeId().equals(selectedShowtimeId);
                    chip.setChecked(isSelected);

                    chip.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onShowtimeClick(info.getShowtimeId());
                        }
                    });
                    layoutTimesContainer.addView(chip);
                }
            }
        }
    }
}
