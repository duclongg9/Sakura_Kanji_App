package com.example.kanji_learning_sakura.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.model.KanjiDto;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapter lựa chọn Kanji để tạo bài thi.
 */
public class ExamKanjiAdapter extends RecyclerView.Adapter<ExamKanjiAdapter.ExamKanjiHolder> {

    /** Lắng nghe sự thay đổi lựa chọn. */
    public interface SelectionListener {
        /** Được gọi khi số lượng lựa chọn hợp lệ thay đổi. */
        void onSelectionChanged(int count);

        /** Khi người dùng vượt quá giới hạn cho phép. */
        void onSelectionLimitReached(int max);
    }

    private final List<KanjiDto> items = new ArrayList<>();
    private final Set<Long> selectedIds = new HashSet<>();
    private final int maxSelection;
    private final SelectionListener listener;

    public ExamKanjiAdapter(int maxSelection, SelectionListener listener) {
        this.maxSelection = maxSelection;
        this.listener = listener;
    }

    /** Cập nhật dữ liệu danh sách. */
    public void submit(List<KanjiDto> data) {
        items.clear();
        selectedIds.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged(selectedIds.size());
        }
    }

    /** @return danh sách Kanji được chọn. */
    public List<KanjiDto> getSelected() {
        List<KanjiDto> result = new ArrayList<>();
        for (KanjiDto dto : items) {
            if (selectedIds.contains(dto.getId())) {
                result.add(dto);
            }
        }
        return result;
    }

    @NonNull
    @Override
    public ExamKanjiHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exam_kanji, parent, false);
        return new ExamKanjiHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExamKanjiHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ExamKanjiHolder extends RecyclerView.ViewHolder {
        private final TextView tvChar;
        private final TextView tvHanViet;
        private final TextView tvReadings;
        private final TextView tvDescription;
        private final MaterialCheckBox checkBox;
        private final MaterialCardView card;

        ExamKanjiHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            tvChar = itemView.findViewById(R.id.tvExamKanjiChar);
            tvHanViet = itemView.findViewById(R.id.tvExamKanjiHanViet);
            tvReadings = itemView.findViewById(R.id.tvExamKanjiReadings);
            tvDescription = itemView.findViewById(R.id.tvExamKanjiDescription);
            checkBox = itemView.findViewById(R.id.cbExamKanji);
        }

        void bind(KanjiDto item) {
            tvChar.setText(item.getCharacter());
            tvHanViet.setText(item.getHanViet() != null ? item.getHanViet() : itemView.getContext().getString(R.string.kanji_field_missing));
            String readings = itemView.getContext().getString(R.string.kanji_readings_format,
                    item.getOnReading() != null && !item.getOnReading().isEmpty() ? item.getOnReading() : itemView.getContext().getString(R.string.kanji_field_missing),
                    item.getKunReading() != null && !item.getKunReading().isEmpty() ? item.getKunReading() : itemView.getContext().getString(R.string.kanji_field_missing));
            tvReadings.setText(readings);
            if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                tvDescription.setVisibility(View.VISIBLE);
                tvDescription.setText(item.getDescription());
            } else {
                tvDescription.setVisibility(View.GONE);
            }
            boolean checked = selectedIds.contains(item.getId());
            checkBox.setChecked(checked);
            card.setChecked(checked);
            itemView.setOnClickListener(v -> toggle(item));
        }

        private void toggle(KanjiDto item) {
            boolean currentlySelected = selectedIds.contains(item.getId());
            if (currentlySelected) {
                selectedIds.remove(item.getId());
            } else {
                if (selectedIds.size() >= maxSelection) {
                    if (listener != null) {
                        listener.onSelectionLimitReached(maxSelection);
                    }
                    return;
                }
                selectedIds.add(item.getId());
            }
            notifyItemChanged(getBindingAdapterPosition());
            if (listener != null) {
                listener.onSelectionChanged(selectedIds.size());
            }
        }
    }
}
