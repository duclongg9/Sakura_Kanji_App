package com.example.kanji_learning_sakura.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.core.KanjiService;
import com.example.kanji_learning_sakura.model.QuizChoiceDto;
import com.example.kanji_learning_sakura.model.QuizQuestionDto;
import com.example.kanji_learning_sakura.ui.adapter.QuizChoiceAdapter;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

/**
 * Trình phát quiz từng câu.
 */
public class QuizPlayerActivity extends AppCompatActivity {

    public static final String EXTRA_LESSON_ID = "lessonId";
    public static final String EXTRA_LESSON_TITLE = "lessonTitle";

    private KanjiService service;
    private final List<QuizQuestionDto> questions = new ArrayList<>();
    private int currentIndex = 0;
    private int score = 0;
    private QuizChoiceAdapter adapter;
    private TextView tvProgress;
    private TextView tvQuestion;
    private TextView tvExplanation;
    private MaterialButton btnNext;
    private boolean answered = false;
    private long lessonId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_player);
        service = new KanjiService(this);

        lessonId = getIntent().getLongExtra(EXTRA_LESSON_ID, -1);
        String title = getIntent().getStringExtra(EXTRA_LESSON_TITLE);
        if (title != null) {
            setTitle(title);
        }

        tvProgress = findViewById(R.id.tvQuizProgress);
        tvQuestion = findViewById(R.id.tvQuizQuestion);
        tvExplanation = findViewById(R.id.tvQuizExplanation);
        btnNext = findViewById(R.id.btnNext);
        RecyclerView rv = findViewById(R.id.rvChoices);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuizChoiceAdapter(this::onSelectChoice);
        rv.setAdapter(adapter);

        btnNext.setOnClickListener(v -> goNext());

        loadQuestions();
    }

    private void loadQuestions() {
        new Thread(() -> {
            try {
                List<QuizQuestionDto> data = service.getQuizByLesson(lessonId);
                runOnUiThread(() -> {
                    questions.clear();
                    questions.addAll(data);
                    currentIndex = 0;
                    score = 0;
                    showCurrentQuestion();
                });
            } catch (Exception ex) {
                runOnUiThread(() -> toast(ex.getMessage()));
            }
        }).start();
    }

    private void showCurrentQuestion() {
        if (questions.isEmpty()) {
            toast(getString(R.string.toast_no_question));
            finish();
            return;
        }
        QuizQuestionDto question = questions.get(currentIndex);
        tvProgress.setText(getString(R.string.quiz_progress, currentIndex + 1, questions.size(), score));
        tvQuestion.setText(question.getPrompt());
        adapter.submit(question.getChoices());
        answered = false;
        tvExplanation.setVisibility(android.view.View.GONE);
        btnNext.setText(currentIndex == questions.size() - 1 ? R.string.quiz_finish : R.string.quiz_next);
    }

    private void onSelectChoice(QuizChoiceDto choice) {
        if (answered) return;
        answered = true;
        QuizQuestionDto question = questions.get(currentIndex);
        int selectedIndex = question.getChoices().indexOf(choice);
        adapter.revealResult(selectedIndex);
        if (choice.isCorrect()) {
            score++;
            toast(getString(R.string.toast_correct));
        } else {
            toast(getString(R.string.toast_incorrect));
        }
        tvExplanation.setText(question.getExplanation());
        tvExplanation.setVisibility(android.view.View.VISIBLE);
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
            toast(getString(R.string.quiz_result, score, questions.size()));
            finish();
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
