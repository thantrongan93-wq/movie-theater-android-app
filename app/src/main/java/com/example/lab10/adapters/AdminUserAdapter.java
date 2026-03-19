package com.example.lab10.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lab10.R;
import com.example.lab10.models.User;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    private List<User> userList = new ArrayList<>();
    private List<User> filteredList = new ArrayList<>();
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(User user);
        void onUserLongClick(User user);
    }

    public AdminUserAdapter(OnUserClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = filteredList.get(position);
        
        // Display Name & Email
        String fullName = user.getFullName() != null ? user.getFullName() : (user.getUsername() != null ? user.getUsername() : "N/A");
        holder.tvFullName.setText(fullName);
        holder.tvEmail.setText(user.getEmail());
        
        // Initial for Avatar
        if (!fullName.isEmpty()) {
            holder.tvInitial.setText(fullName.substring(0, 1).toUpperCase());
        } else {
            holder.tvInitial.setText("U");
        }
        
        // Points Badge
        int points = user.getPoints() != null ? user.getPoints() : 0;
        holder.tvPoints.setText(points + " pts");
        
        // Role Badge Styling
        String role = (user.getRoles() != null && !user.getRoles().isEmpty()) 
                      ? user.getRoles().get(0) : "USER";
        holder.tvRole.setText(role.toUpperCase());
        
        GradientDrawable roleBg = new GradientDrawable();
        roleBg.setCornerRadius(20);
        
        if (role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("ROLE_ADMIN")) {
            roleBg.setColor(Color.parseColor("#FEE2E2")); // Red-100
            holder.tvRole.setTextColor(Color.parseColor("#EF4444")); // Red-500
        } else {
            roleBg.setColor(Color.parseColor("#DBEAFE")); // Blue-100
            holder.tvRole.setTextColor(Color.parseColor("#3B82F6")); // Blue-500
        }
        holder.tvRole.setBackground(roleBg);

        // Listeners
        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onUserLongClick(user);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void setData(List<User> newData) {
        this.userList = newData != null ? newData : new ArrayList<>();
        this.filteredList = new ArrayList<>(this.userList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query.isEmpty()) {
            filteredList = new ArrayList<>(userList);
        } else {
            filteredList = userList.stream()
                    .filter(u -> (u.getFullName() != null && u.getFullName().toLowerCase().contains(query.toLowerCase()))
                            || (u.getEmail() != null && u.getEmail().toLowerCase().contains(query.toLowerCase())))
                    .collect(Collectors.toList());
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFullName, tvEmail, tvRole, tvInitial, tvPoints;

        ViewHolder(View itemView) {
            super(itemView);
            tvFullName = itemView.findViewById(R.id.tvUserFullName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvRole = itemView.findViewById(R.id.tvUserRole);
            tvInitial = itemView.findViewById(R.id.tvUserInitial);
            tvPoints = itemView.findViewById(R.id.tvUserPointsBadge);
        }
    }
}