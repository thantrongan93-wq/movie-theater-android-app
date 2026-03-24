package com.example.lab10.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.lab10.R;
import com.example.lab10.models.ChatMessage;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 0;
    private static final int VIEW_TYPE_BOT = 1;
    private static final int VIEW_TYPE_QR = 2;

    private final List<ChatMessage> messages = new ArrayList<>();
    private OnViewSeatsClickListener viewSeatsClickListener;

    public interface OnViewSeatsClickListener {
        void onViewSeatsClick(Long movieId, Long showtimeDetailId);
    }

    public void setOnViewSeatsClickListener(OnViewSeatsClickListener listener) {
        this.viewSeatsClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        if (msg.isUser()) return VIEW_TYPE_USER;
        if (msg.isQrMessage()) return VIEW_TYPE_QR;
        return VIEW_TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_USER:
                return new UserMessageViewHolder(
                        inflater.inflate(R.layout.item_chat_message_user, parent, false));
            case VIEW_TYPE_QR:
                return new QrViewHolder(
                        inflater.inflate(R.layout.item_chat_qr, parent, false));
            default:
                return new BotMessageViewHolder(
                        inflater.inflate(R.layout.item_chat_message_bot, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof QrViewHolder) {
            ((QrViewHolder) holder).bind(message);
        } else if (holder instanceof BotMessageViewHolder) {
            ((BotMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    /** Xóa loading indicator (tin nhắn cuối nếu nó là loading) */
    public void removeLoading() {
        if (!messages.isEmpty()) {
            int lastIndex = messages.size() - 1;
            if (messages.get(lastIndex).isLoading()) {
                messages.remove(lastIndex);
                notifyItemRemoved(lastIndex);
            }
        }
    }

    // ======================== ViewHolders ========================

    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessage;

        UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_user_message);
        }

        void bind(ChatMessage message) {
            tvMessage.setText(message.getMessage());
        }
    }

    class BotMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessage;
        private final MaterialButton btnViewSeats;

        BotMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_bot_message);
            btnViewSeats = itemView.findViewById(R.id.btn_view_seats);
        }

        void bind(ChatMessage message) {
            if (message.isLoading()) {
                tvMessage.setText("Đang suy nghĩ... 🤔");
                btnViewSeats.setVisibility(View.GONE);
                return;
            }

            tvMessage.setText(message.getMessage());

            if (message.hasAction()) {
                btnViewSeats.setVisibility(View.VISIBLE);
                btnViewSeats.setOnClickListener(v -> {
                    if (viewSeatsClickListener != null) {
                        viewSeatsClickListener.onViewSeatsClick(
                                message.getMovieId(), message.getShowtimeDetailId());
                    }
                });
            } else {
                btnViewSeats.setVisibility(View.GONE);
            }
        }
    }

    static class QrViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivQrCode;
        private final TextView tvBankName, tvBankAccount, tvAmount, tvContent, tvStatus;

        QrViewHolder(@NonNull View itemView) {
            super(itemView);
            ivQrCode = itemView.findViewById(R.id.iv_qr_code);
            tvBankName = itemView.findViewById(R.id.tv_bank_name);
            tvBankAccount = itemView.findViewById(R.id.tv_bank_account);
            tvAmount = itemView.findViewById(R.id.tv_payment_amount);
            tvContent = itemView.findViewById(R.id.tv_payment_content);
            tvStatus = itemView.findViewById(R.id.tv_payment_status);
        }

        void bind(ChatMessage message) {
            // Load QR image từ URL
            if (message.getQrImageUrl() != null && !message.getQrImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(message.getQrImageUrl())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(ivQrCode);
            }

            tvBankName.setText("🏦 " + (message.getBankName() != null ? message.getBankName() : ""));
            tvBankAccount.setText("💳 STK: " + (message.getBankAccount() != null ? message.getBankAccount() : ""));
            tvAmount.setText("💰 " + (message.getPaymentAmount() != null ? message.getPaymentAmount() + " VNĐ" : ""));
            tvContent.setText("📝 ND: " + (message.getPaymentContent() != null ? message.getPaymentContent() : ""));
            tvStatus.setText("⏳ Đang chờ thanh toán...");
        }
    }
}
