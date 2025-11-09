package com.example.kanji_learning_sakura.model;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO hiển thị kết quả import Kanji qua CSV.
 */
public class BulkImportReportDto {
    private int totalRows;
    private int kanjiInserted;
    private int kanjiUpdated;
    private int questionsCreated;
    private int choicesCreated;
    private final List<RowError> errors = new ArrayList<>();

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getKanjiInserted() {
        return kanjiInserted;
    }

    public void setKanjiInserted(int kanjiInserted) {
        this.kanjiInserted = kanjiInserted;
    }

    public int getKanjiUpdated() {
        return kanjiUpdated;
    }

    public void setKanjiUpdated(int kanjiUpdated) {
        this.kanjiUpdated = kanjiUpdated;
    }

    public int getQuestionsCreated() {
        return questionsCreated;
    }

    public void setQuestionsCreated(int questionsCreated) {
        this.questionsCreated = questionsCreated;
    }

    public int getChoicesCreated() {
        return choicesCreated;
    }

    public void setChoicesCreated(int choicesCreated) {
        this.choicesCreated = choicesCreated;
    }

    public List<RowError> getErrors() {
        return errors;
    }

    public static class RowError {
        private int rowNumber;
        private String message;

        public int getRowNumber() {
            return rowNumber;
        }

        public void setRowNumber(int rowNumber) {
            this.rowNumber = rowNumber;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
