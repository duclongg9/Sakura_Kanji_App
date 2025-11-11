package com.example.kanji_learning_sakura.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.model.ExamChoice;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Adapter hiển thị đáp án cho từng câu hỏi bài thi tự tạo.
 */
public class ExamChoiceAdapter extends RecyclerView.Adapter<ExamChoiceAdapter.ChoiceHolder> {

    private final List<ExamChoice> items = new ArrayList<>();
    private final Consumer<ExamChoice> onSelect;
    private int selectedPosition = -1;
    private boolean showResult = false;

    public ExamChoiceAdapter(Consumer<ExamChoice> onSelect) {
        this.onSelect = onSelect;
    }

    public void submit(List<ExamChoice> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        selectedPosition = -1;
        showResult = false;
        notifyDataSetChanged();
    }

    public void revealResult(int selectedIndex) {
        selectedPosition = selectedIndex;
        showResult = true;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChoiceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_choice, parent, false);
        return new ChoiceHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChoiceHolder holder, int position) {
        holder.bind(items.get(position), position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ChoiceHolder extends RecyclerView.ViewHolder {
        private final TextView tvContent;

        ChoiceHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvChoiceContent);
        }

        void bind(ExamChoice choice, int position) {
            tvContent.setText(choice.getContent());
            Context context = itemView.getContext();
            int stroke = R.color.hanabiInk;
            int background = android.R.color.transparent;
            int textColor = R.color.hanabiInk;

            if (showResult) {
                if (choice.isCorrect()) {
                    stroke = R.color.hanabiSuccess;
                    background = R.color.hanabiSuccess;
                    textColor = android.R.color.white;
                } else if (position == selectedPosition) {
                    stroke = R.color.hanabiError;
                    background = R.color.hanabiError;
                    textColor = android.R.color.white;
                }
            } else if (position == selectedPosition) {
                stroke = R.color.hanabiSuccess;
            }

            com.google.android.material.card.MaterialCardView card =
                    (com.google.android.material.card.MaterialCardView) itemView;
            card.setStrokeColor(ContextCompat.getColor(context, stroke));
            card.setCardBackgroundColor(ContextCompat.getColor(context, background));
            tvContent.setTextColor(ContextCompat.getColor(context, textColor));

            itemView.setOnClickListener(v -> {
                if (showResult) return;
                selectedPosition = position;
                notifyDataSetChanged();
                onSelect.accept(choice);
            });
        }
    }
}
