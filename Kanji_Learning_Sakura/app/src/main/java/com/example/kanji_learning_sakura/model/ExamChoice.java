package com.example.kanji_learning_sakura.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

/**
 * Lựa chọn trong một câu hỏi bài thi tự tạo.
 */
public class ExamChoice implements Parcelable {
    private String content;
    private boolean correct;

    public ExamChoice() {
    }

    public ExamChoice(String content, boolean correct) {
        this.content = content;
        this.correct = correct;
    }

    protected ExamChoice(Parcel in) {
        content = in.readString();
        correct = in.readByte() != 0;
    }

    public static final Creator<ExamChoice> CREATOR = new Creator<ExamChoice>() {
        @Override
        public ExamChoice createFromParcel(Parcel in) {
            return new ExamChoice(in);
        }

        @Override
        public ExamChoice[] newArray(int size) {
            return new ExamChoice[size];
        }
    };

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(content);
        dest.writeByte((byte) (correct ? 1 : 0));
    }
}
