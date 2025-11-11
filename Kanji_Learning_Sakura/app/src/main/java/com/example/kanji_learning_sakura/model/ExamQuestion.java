package com.example.kanji_learning_sakura.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Câu hỏi trong bài thi gồm đề bài và danh sách lựa chọn.
 */
public class ExamQuestion implements Parcelable {
    private String prompt;
    private final List<ExamChoice> choices = new ArrayList<>();

    public ExamQuestion() {
    }

    protected ExamQuestion(Parcel in) {
        prompt = in.readString();
        in.readTypedList(choices, ExamChoice.CREATOR);
    }

    public static final Creator<ExamQuestion> CREATOR = new Creator<ExamQuestion>() {
        @Override
        public ExamQuestion createFromParcel(Parcel in) {
            return new ExamQuestion(in);
        }

        @Override
        public ExamQuestion[] newArray(int size) {
            return new ExamQuestion[size];
        }
    };

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public List<ExamChoice> getChoices() {
        return choices;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(prompt);
        dest.writeTypedList(choices);
    }
}
