package com.example.kanji_learning_sakura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.config.AuthPrefs;
import com.example.kanji_learning_sakura.config.ExamResultStore;
import com.example.kanji_learning_sakura.model.ExamChoice;
import com.example.kanji_learning_sakura.model.ExamQuestion;
import com.example.kanji_learning_sakura.ui.adapter.ExamChoiceAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

/**
 * Trình chơi bài thi tự tạo từ Kanji đã chọn.
 */
public class ExamPlayerActivity extends AppCompatActivity {

    public static final String EXTRA_QUESTIONS = "examQuestions";
    public static final String EXTRA_LEVEL_ID = "examLevelId";
    public static final String EXTRA_LEVEL_NAME = "examLevelName";
    public static final String EXTRA_RESULT_SCORE = "examResultScore";
    public static final String EXTRA_RESULT_TOTAL = "examResultTotal";

    private final List<ExamQuestion> questions = new ArrayList<>();
    private ExamChoiceAdapter adapter;
    private TextView tvProgress;
    private TextView tvQuestion;
    private TextView tvFeedback;
    private MaterialButton btnNext;
    private boolean answered = false;
    private int currentIndex = 0;
    private int score = 0;
    private int levelId = -1;

    private ExamResultStore resultStore;
    private AuthPrefs authPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_player);

        resultStore = new ExamResultStore(this);
        authPrefs = new AuthPrefs(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbarExamPlayer);
        toolbar.setNavigationOnClickListener(v -> finish());

        ArrayList<ExamQuestion> data = getIntent().getParcelableArrayListExtra(EXTRA_QUESTIONS);
        if (data == null || data.isEmpty()) {
            toast(getString(R.string.toast_no_question));
            finish();
            return;
        }
        questions.addAll(data);

        levelId = getIntent().getIntExtra(EXTRA_LEVEL_ID, -1);
        String levelName = getIntent().getStringExtra(EXTRA_LEVEL_NAME);
        if (levelName != null && !levelName.isEmpty()) {
            toolbar.setTitle(levelName);
        }

        tvProgress = findViewById(R.id.tvExamProgress);
        tvQuestion = findViewById(R.id.tvExamQuestion);
        tvFeedback = findViewById(R.id.tvExamFeedback);
        btnNext = findViewById(R.id.btnExamNext);
        RecyclerView rvChoices = findViewById(R.id.rvExamChoices);
        rvChoices.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExamChoiceAdapter(this::onChoiceSelected);
        rvChoices.setAdapter(adapter);

        btnNext.setOnClickListener(v -> goNext());

        showCurrentQuestion();
    }

    private void showCurrentQuestion() {
        if (questions.isEmpty()) {
            toast(getString(R.string.toast_no_question));
            finish();
            return;
        }
        ExamQuestion question = questions.get(currentIndex);
        tvProgress.setText(getString(R.string.quiz_progress, currentIndex + 1, questions.size(), score));
        tvQuestion.setText(question.getPrompt());
        adapter.submit(question.getChoices());
        answered = false;
        tvFeedback.setVisibility(android.view.View.GONE);
        btnNext.setText(currentIndex == questions.size() - 1
                ? R.string.exam_finish
                : R.string.quiz_next);
    }

    private void onChoiceSelected(ExamChoice choice) {
        if (answered) {
            return;
        }
        answered = true;
        ExamQuestion question = questions.get(currentIndex);
        int selectedIndex = question.getChoices().indexOf(choice);
        adapter.revealResult(selectedIndex);
        ExamChoice correctChoice = findCorrectChoice(question);
        if (choice.isCorrect()) {
            score++;
            tvFeedback.setText(getString(R.string.exam_feedback_correct,
                    correctChoice != null ? correctChoice.getContent() : ""));
        } else {
            tvFeedback.setText(getString(R.string.exam_feedback_incorrect,
                    correctChoice != null ? correctChoice.getContent() : ""));
        }
        tvFeedback.setVisibility(android.view.View.VISIBLE);
    }

    private void goNext() {
        if (!answered) {
            toast(getString(R.string.toast_select_answer));
            return;
        }
        if (currentIndex < questions.size() - 1) {
            currentIndex++;
            showCurrentQuestion();
        } else {
            finishExam();
        }
    }

    private void finishExam() {
        toast(getString(R.string.exam_result, score, questions.size()));
        if (levelId > 0) {
            resultStore.save(authPrefs.userId(), levelId, score, questions.size());
        }
        Intent data = new Intent();
        data.putExtra(EXTRA_RESULT_SCORE, score);
        data.putExtra(EXTRA_RESULT_TOTAL, questions.size());
        setResult(RESULT_OK, data);
        finish();
    }

    private ExamChoice findCorrectChoice(@NonNull ExamQuestion question) {
        for (ExamChoice choice : question.getChoices()) {
            if (choice.isCorrect()) {
                return choice;
            }
        }
        return null;
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
