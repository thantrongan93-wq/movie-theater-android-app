package com.example.lab10.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab10.R;
import com.example.lab10.models.Seat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.RowViewHolder> {

    public interface OnSeatSelectionChangedListener {
        void onSeatSelectionChanged();
    }

    // Màu sắc
    private static final int COLOR_REGULAR  = Color.parseColor("#E8E0F0"); // tím nhạt – thường
    private static final int COLOR_VIP      = Color.parseColor("#FFD6D6"); // hồng nhạt – VIP
    private static final int COLOR_SELECTED = Color.parseColor("#E91E63"); // hồng đậm – đã chọn
    private static final int COLOR_BOOKED   = Color.parseColor("#BBBBBB"); // xám – đã đặt
    private static final int COLOR_TEXT     = Color.parseColor("#5B2D8E"); // tím đậm
    private static final int COLOR_TEXT_W   = Color.WHITE;

    private final List<String>            rowOrder = new ArrayList<>();
    private final Map<String, List<Seat>> rowMap   = new LinkedHashMap<>();
    private List<Seat> allSeats = new ArrayList<>();
    private final OnSeatSelectionChangedListener listener;
    private float seatSizeDp = 38f; // kích thước ghế, thay đổi khi zoom

    public SeatAdapter(List<Seat> seats, OnSeatSelectionChangedListener listener) {
        this.listener = listener;
        buildRows(seats);
    }

    /** Gọi từ Activity khi user zoom để resize tất cả ghế */
    public void setSeatSize(float sizeDp) {
        this.seatSizeDp = sizeDp;
        notifyDataSetChanged();
    }

    // -------------------------------------------------------------------------
    // Extract row + number từ seatNumber nếu rowNumber null
    // Ví dụ: "E4" → row="E", num="4"   |   "E13" → row="E", num="13"
    // -------------------------------------------------------------------------
    private static String extractRow(Seat seat) {
        if (seat.getRowNumber() != null && !seat.getRowNumber().isEmpty()) {
            return seat.getRowNumber();
        }
        String raw = seat.getSeatNumber();
        if (raw == null || raw.isEmpty()) return "?";
        // Tách phần chữ đầu (row) và phần số sau
        int i = 0;
        while (i < raw.length() && Character.isLetter(raw.charAt(i))) i++;
        return i > 0 ? raw.substring(0, i) : "?";
    }

    private static String extractNum(Seat seat) {
        // Nếu rowNumber đã có sẵn thì seatNumber là số thuần
        if (seat.getRowNumber() != null && !seat.getRowNumber().isEmpty()) {
            return seat.getSeatNumber() != null ? seat.getSeatNumber() : "";
        }
        String raw = seat.getSeatNumber();
        if (raw == null) return "";
        int i = 0;
        while (i < raw.length() && Character.isLetter(raw.charAt(i))) i++;
        return raw.substring(i);
    }

    private void buildRows(List<Seat> seats) {
        rowOrder.clear();
        rowMap.clear();
        allSeats = seats != null ? seats : new ArrayList<>();
        for (Seat seat : allSeats) {
            String row = extractRow(seat);
            if (!rowMap.containsKey(row)) {
                rowOrder.add(row);
                rowMap.put(row, new ArrayList<>());
            }
            rowMap.get(row).add(seat);
        }
        
        // Sort rows alphabetically (A, B, C, ...)
        rowOrder.sort(String::compareTo);
        
        // Sort seats within each row by seat number
        for (List<Seat> rowSeats : rowMap.values()) {
            rowSeats.sort((s1, s2) -> {
                String num1 = extractNum(s1);
                String num2 = extractNum(s2);
                try {
                    return Integer.compare(Integer.parseInt(num1), Integer.parseInt(num2));
                } catch (NumberFormatException e) {
                    return num1.compareTo(num2); // fallback to string comparison
                }
            });
        }
    }

    public void updateSeats(List<Seat> newSeats) {
        buildRows(newSeats);
        notifyDataSetChanged();
    }

    public List<Seat> getSelectedSeats() {
        List<Seat> sel = new ArrayList<>();
        for (Seat s : allSeats) if (s.isSelected()) sel.add(s);
        return sel;
    }

    @Override public int getItemCount() { return rowOrder.size(); }

    @NonNull
    @Override
    public RowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_seat_row, parent, false);
        return new RowViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RowViewHolder holder, int position) {
        String row = rowOrder.get(position);
        holder.bind(row, rowMap.get(row));
    }

    // -------------------------------------------------------------------------

    class RowViewHolder extends RecyclerView.ViewHolder {
        private final TextView     tvRowLabel;
        private final TextView     tvRowLabelRight;
        private final LinearLayout llSeats;

        RowViewHolder(@NonNull View v) {
            super(v);
            tvRowLabel      = v.findViewById(R.id.tv_row_label);
            tvRowLabelRight = v.findViewById(R.id.tv_row_label_right);
            llSeats         = v.findViewById(R.id.ll_seats_container);
        }

        void bind(String row, List<Seat> seats) {
            tvRowLabel.setText(row);
            tvRowLabelRight.setText(row);
            llSeats.removeAllViews();
            if (seats == null) return;

            Context ctx  = itemView.getContext();
            int sz       = dp(ctx, seatSizeDp);
            int margin   = dp(ctx, 2);

            for (Seat seat : seats) {
                llSeats.addView(buildCard(ctx, seat, sz, margin));
            }
        }

        private CardView buildCard(Context ctx, Seat seat, int sz, int mg) {
            CardView card = new CardView(ctx);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sz, sz);
            lp.setMargins(mg, mg, mg, mg);
            card.setLayoutParams(lp);
            card.setRadius(dp(ctx, 6));
            card.setCardElevation(dp(ctx, 1));

            TextView tv = new TextView(ctx);
            tv.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, Math.max(8f, seatSizeDp * 0.27f));
            tv.setText(extractNum(seat));
            card.addView(tv);

            applyColor(card, tv, seat);

            boolean avail = Boolean.TRUE.equals(seat.getIsAvailable());
            card.setClickable(avail);
            card.setFocusable(avail);
            if (avail) {
                card.setOnClickListener(v -> {
                    seat.setSelected(!seat.isSelected());
                    applyColor(card, tv, seat);
                    if (listener != null) listener.onSeatSelectionChanged();
                });
            }
            return card;
        }

        private void applyColor(CardView card, TextView tv, Seat seat) {
            boolean avail = Boolean.TRUE.equals(seat.getIsAvailable());
            if (!avail) {
                card.setCardBackgroundColor(COLOR_BOOKED);
                tv.setTextColor(COLOR_TEXT_W);
            } else if (seat.isSelected()) {
                card.setCardBackgroundColor(COLOR_SELECTED);
                tv.setTextColor(COLOR_TEXT_W);
            } else {
                boolean vip = "VIP".equalsIgnoreCase(seat.getSeatType())
                        || "VIP".equalsIgnoreCase(seat.getStatus());
                card.setCardBackgroundColor(vip ? COLOR_VIP : COLOR_REGULAR);
                tv.setTextColor(COLOR_TEXT);
            }
        }

        private int dp(Context ctx, float v) {
            return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    v, ctx.getResources().getDisplayMetrics()));
        }
    }
}