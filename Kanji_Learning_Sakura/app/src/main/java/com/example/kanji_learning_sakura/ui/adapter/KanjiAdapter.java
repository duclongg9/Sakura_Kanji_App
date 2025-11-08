package com.example.kanji_learning_sakura.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.model.KanjiDto;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách Kanji.
 */
public class KanjiAdapter extends RecyclerView.Adapter<KanjiAdapter.KanjiViewHolder> {

    private final List<KanjiDto> items = new ArrayList<>();

    public void submit(List<KanjiDto> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public KanjiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kanji, parent, false);
        return new KanjiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KanjiViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class KanjiViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvKanji;
        private final TextView tvHanViet;
        private final TextView tvOnKun;
        private final TextView tvDescription;

        KanjiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKanji = itemView.findViewById(R.id.tvKanji);
            tvHanViet = itemView.findViewById(R.id.tvHanViet);
            tvOnKun = itemView.findViewById(R.id.tvOnKun);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }

        void bind(KanjiDto dto) {
            tvKanji.setText(dto.getCharacter());
            tvHanViet.setText(dto.getHanViet() != null ? dto.getHanViet() : "");
            String onKun = "On: " + safe(dto.getOnReading()) + "  |  Kun: " + safe(dto.getKunReading());
            tvOnKun.setText(onKun);
            tvDescription.setText(dto.getDescription() != null ? dto.getDescription() : "");
        }

        private String safe(String value) {
            return value == null ? "-" : value;
        }
    }
}
