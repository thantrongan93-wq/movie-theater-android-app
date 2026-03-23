package com.example.lab10.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.R;
import com.example.lab10.models.LoyaltyProgram;

import java.util.List;

public class LoyaltyProgramAdapter extends RecyclerView.Adapter<LoyaltyProgramAdapter.ViewHolder> {

    private List<LoyaltyProgram> programs;

    public LoyaltyProgramAdapter(List<LoyaltyProgram> programs) {
        this.programs = programs;
    }

    public void updateData(List<LoyaltyProgram> newList) {
        this.programs = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loyalty_program, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LoyaltyProgram program = programs.get(position);
        holder.tvName.setText(program.getName());
        holder.tvDescription.setText(program.getDescription());
        holder.tvStatus.setText(program.getStatus());
        holder.tvDateRange.setText(program.getStartDate() + " - " + program.getEndDate());
    }

    @Override
    public int getItemCount() {
        return programs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvStatus, tvDateRange;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_program_name);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvDateRange = itemView.findViewById(R.id.tv_date_range);
        }
    }
}
