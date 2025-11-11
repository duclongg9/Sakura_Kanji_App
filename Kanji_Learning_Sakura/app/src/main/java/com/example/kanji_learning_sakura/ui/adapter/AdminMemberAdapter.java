package com.example.kanji_learning_sakura.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.model.AdminMemberDto;
import com.google.android.material.chip.Chip;
import com.google.android.material.textview.MaterialTextView;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách hội viên trong trang quản trị.
 */
public class AdminMemberAdapter extends RecyclerView.Adapter<AdminMemberAdapter.MemberViewHolder> {

    private final List<AdminMemberDto> items = new ArrayList<>();

    /**
     * Cập nhật dữ liệu hiển thị.
     *
     * @param data danh sách hội viên mới.
     */
    public void submitList(@NonNull List<AdminMemberDto> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_member, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder cho từng hội viên.
     */
    static class MemberViewHolder extends RecyclerView.ViewHolder {

        private final MaterialTextView tvName;
        private final MaterialTextView tvEmail;
        private final MaterialTextView tvVipExpiry;
        private final MaterialTextView tvRequest;
        private final Chip chipTier;

        MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMemberName);
            tvEmail = itemView.findViewById(R.id.tvMemberEmail);
            tvVipExpiry = itemView.findViewById(R.id.tvMemberVipExpiry);
            tvRequest = itemView.findViewById(R.id.tvMemberRequest);
            chipTier = itemView.findViewById(R.id.chipMemberTier);
        }

        void bind(AdminMemberDto item) {
            Context context = itemView.getContext();
            tvName.setText(item.getUserName());
            tvEmail.setText(item.getEmail());

            chipTier.setText(mapTier(context, item.getAccountTier()));

            String vipExpiresAt = item.getVipExpiresAt();
            if (vipExpiresAt != null && !vipExpiresAt.trim().isEmpty()) {
                tvVipExpiry.setVisibility(View.VISIBLE);
                tvVipExpiry.setText(context.getString(R.string.member_vip_expiry, vipExpiresAt));
            } else if ("VIP".equalsIgnoreCase(item.getAccountTier())) {
                tvVipExpiry.setVisibility(View.VISIBLE);
                tvVipExpiry.setText(R.string.member_vip_no_expiry);
            } else {
                tvVipExpiry.setVisibility(View.GONE);
            }

            if (item.isHasPendingRequest()) {
                tvRequest.setVisibility(View.VISIBLE);
                String statusLabel = mapStatus(context, item.getRequestStatus());
                String note = item.getRequestNote();
                if (note == null || note.trim().isEmpty()) {
                    note = context.getString(R.string.member_pending_request_no_note);
                }
                tvRequest.setText(context.getString(R.string.member_pending_request, statusLabel, note));
            } else {
                tvRequest.setVisibility(View.GONE);
            }
        }

        private CharSequence mapTier(Context context, String tier) {
            if (tier == null) {
                return context.getString(R.string.profile_role_free);
            }
            switch (tier.toUpperCase()) {
                case "VIP":
                    chipTier.setChipBackgroundColorResource(R.color.sakuraPinkLight);
                    chipTier.setTextColor(ContextCompat.getColor(context, R.color.sakuraPinkDark));
                    return context.getString(R.string.profile_role_vip);
                case "ADMIN":
                    chipTier.setChipBackgroundColorResource(R.color.hanabiInk);
                    chipTier.setTextColor(ContextCompat.getColor(context, android.R.color.white));
                    return context.getString(R.string.profile_role_admin);
                default:
                    chipTier.setChipBackgroundColorResource(R.color.white);
                    chipTier.setTextColor(ContextCompat.getColor(context, R.color.hanabiInk));
                    return context.getString(R.string.profile_role_free);
            }
        }

        private String mapStatus(Context context, String status) {
            if (status == null) {
                return context.getString(R.string.member_request_status_pending);
            }
            switch (status.toUpperCase()) {
                case "APPROVED":
                    return context.getString(R.string.member_request_status_approved);
                case "REJECTED":
                    return context.getString(R.string.member_request_status_rejected);
                default:
                    return context.getString(R.string.member_request_status_pending);
            }
        }
    }
}
