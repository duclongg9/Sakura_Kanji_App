package com.example.kanji_learning_sakura.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.model.KanjiDto;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách Kanji trong màn quản trị kèm thao tác chỉnh sửa/xóa.
 */
public class AdminKanjiAdapter extends RecyclerView.Adapter<AdminKanjiAdapter.KanjiViewHolder> {

    /**
     * Lắng nghe sự kiện người dùng tương tác với từng item.
     */
    public interface Listener {
        /**
         * Được gọi khi cần chỉnh sửa một Kanji.
         *
         * @param item Kanji được chọn.
         */
        void onEdit(@NonNull KanjiDto item);

        /**
         * Được gọi khi cần xóa một Kanji khỏi danh sách.
         *
         * @param item Kanji được chọn.
         */
        void onDelete(@NonNull KanjiDto item);
    }

    private final Listener listener;
    private final List<KanjiDto> items = new ArrayList<>();

    public AdminKanjiAdapter(@NonNull Listener listener) {
        this.listener = listener;
    }

    /**
     * Cập nhật danh sách Kanji hiển thị.
     *
     * @param data danh sách mới.
     */
    public void submitList(@NonNull List<KanjiDto> data) {
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public KanjiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_admin_kanji, parent, false);
        return new KanjiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KanjiViewHolder holder, int position) {
        KanjiDto item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder lưu trữ view cho từng dòng Kanji.
     */
    static class KanjiViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvCharacter;
        private final TextView tvHanViet;
        private final TextView tvReadings;
        private final TextView tvDescription;
        private final MaterialButton btnEdit;
        private final MaterialButton btnDelete;

        KanjiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCharacter = itemView.findViewById(R.id.tvKanjiCharacter);
            tvHanViet = itemView.findViewById(R.id.tvHanViet);
            tvReadings = itemView.findViewById(R.id.tvReadings);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(KanjiDto item, Listener listener) {
            Context context = itemView.getContext();
            String missing = context.getString(R.string.kanji_field_missing);
            tvCharacter.setText(item.getCharacter());
            tvHanViet.setText(context.getString(R.string.kanji_hanviet_format,
                    valueOrFallback(item.getHanViet(), missing)));
            tvReadings.setText(context.getString(R.string.kanji_readings_format,
                    valueOrFallback(item.getOnReading(), missing),
                    valueOrFallback(item.getKunReading(), missing)));
            String description = item.getDescription();
            if (description != null && !description.trim().isEmpty()) {
                tvDescription.setVisibility(View.VISIBLE);
                tvDescription.setText(context.getString(R.string.kanji_description_label, description));
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            btnEdit.setOnClickListener(v -> listener.onEdit(item));
            btnDelete.setOnClickListener(v -> listener.onDelete(item));
        }

        private String valueOrFallback(String value, String fallback) {
            return value == null || value.trim().isEmpty() ? fallback : value;
        }
    }
}
