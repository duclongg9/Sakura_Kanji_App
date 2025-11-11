package com.example.kanji_learning_sakura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.config.AuthPrefs;
import com.example.kanji_learning_sakura.config.ExamResultStore;
import com.example.kanji_learning_sakura.core.KanjiService;
import com.example.kanji_learning_sakura.model.ExamChoice;
import com.example.kanji_learning_sakura.model.ExamQuestion;
import com.example.kanji_learning_sakura.model.JlptLevelDto;
import com.example.kanji_learning_sakura.model.KanjiDto;
import com.example.kanji_learning_sakura.model.LevelDto;
import com.example.kanji_learning_sakura.ui.adapter.ExamKanjiAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Màn hình chọn Kanji và tạo bài thi ghép nghĩa.
 */
public class ExamSetupActivity extends AppCompatActivity implements ExamKanjiAdapter.SelectionListener {

    private static final int MAX_SELECTION = 10;

    private KanjiService service;
    private AuthPrefs authPrefs;
    private ExamResultStore resultStore;

    private final List<JlptLevelDto> jlptLevels = new ArrayList<>();
    private final List<LevelDto> levels = new ArrayList<>();

    private MaterialAutoCompleteTextView spJlpt;
    private MaterialAutoCompleteTextView spLevel;
    private TextView tvLastResult;
    private TextView tvSelection;
    private TextView tvEmpty;
    private CircularProgressIndicator progress;
    private ExamKanjiAdapter adapter;
    private MaterialButton btnStart;
    private LevelDto currentLevel;
    private RecyclerView rvKanji;

    private final ActivityResultLauncher<Intent> examLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    updateLastResult();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_setup);
        service = new KanjiService(this);
        authPrefs = new AuthPrefs(this);
        resultStore = new ExamResultStore(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbarExam);
        toolbar.setNavigationOnClickListener(v -> finish());

        spJlpt = findViewById(R.id.spExamJlpt);
        spLevel = findViewById(R.id.spExamLevel);
        tvLastResult = findViewById(R.id.tvExamLastResult);
        tvSelection = findViewById(R.id.tvExamSelection);
        tvEmpty = findViewById(R.id.tvExamEmpty);
        progress = findViewById(R.id.progressExam);
        btnStart = findViewById(R.id.btnStartExam);
        rvKanji = findViewById(R.id.rvExamKanji);

        rvKanji.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExamKanjiAdapter(MAX_SELECTION, this);
        rvKanji.setAdapter(adapter);

        tvSelection.setText(getString(R.string.exam_selection_counter, 0, MAX_SELECTION));
        btnStart.setEnabled(false);
        btnStart.setOnClickListener(v -> startExam());

        spJlpt.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < jlptLevels.size()) {
                loadLevels(jlptLevels.get(position).getId());
            }
        });

        spLevel.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < levels.size()) {
                currentLevel = levels.get(position);
                loadKanji(currentLevel.getId());
            }
        });

        loadJlptLevels();
    }

    @Override
    public void onSelectionChanged(int count) {
        tvSelection.setText(getString(R.string.exam_selection_counter, count, MAX_SELECTION));
        btnStart.setEnabled(count >= 2);
    }

    @Override
    public void onSelectionLimitReached(int max) {
        toast(getString(R.string.toast_exam_limit, max));
    }

    private void loadJlptLevels() {
        setLoading(true);
        new Thread(() -> {
            try {
                List<JlptLevelDto> data = service.getJlptLevels();
                runOnUiThread(() -> {
                    jlptLevels.clear();
                    jlptLevels.addAll(data);
                    spJlpt.setAdapter(new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1,
                            toJlptNames(data)));
                    if (!data.isEmpty()) {
                        spJlpt.setText(data.get(0).getNameLevel(), false);
                        loadLevels(data.get(0).getId());
                    } else {
                        setLoading(false);
                    }
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    setLoading(false);
                    toast(ex.getMessage());
                });
            }
        }).start();
    }

    private void loadLevels(int jlptId) {
        setLoading(true);
        new Thread(() -> {
            try {
                List<LevelDto> data = service.getLevels(jlptId);
                runOnUiThread(() -> {
                    levels.clear();
                    levels.addAll(data);
                    spLevel.setAdapter(new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1,
                            toLevelNames(data)));
                    if (!data.isEmpty()) {
                        currentLevel = data.get(0);
                        spLevel.setText(currentLevel.getName(), false);
                        loadKanji(currentLevel.getId());
                    } else {
                        currentLevel = null;
                        adapter.submit(new ArrayList<>());
                        showEmptyState(true);
                        setLoading(false);
                        updateLastResult();
                    }
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    setLoading(false);
                    toast(ex.getMessage());
                });
            }
        }).start();
    }

    private void loadKanji(int levelId) {
        setLoading(true);
        new Thread(() -> {
            try {
                List<KanjiDto> data = service.getKanjiByLevel(levelId);
                runOnUiThread(() -> {
                    adapter.submit(data);
                    showEmptyState(data.isEmpty());
                    setLoading(false);
                    updateLastResult();
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    setLoading(false);
                    toast(ex.getMessage());
                });
            }
        }).start();
    }

    private void startExam() {
        LevelDto level = currentLevel;
        if (level == null) {
            toast(getString(R.string.hint_select_level_optional));
            return;
        }
        List<KanjiDto> selected = adapter.getSelected();
        if (selected.size() < 2) {
            toast(getString(R.string.toast_select_kanji_for_exam));
            return;
        }
        ArrayList<ExamQuestion> questions = buildQuestions(selected);
        Intent intent = new Intent(this, ExamPlayerActivity.class);
        intent.putParcelableArrayListExtra(ExamPlayerActivity.EXTRA_QUESTIONS, questions);
        intent.putExtra(ExamPlayerActivity.EXTRA_LEVEL_ID, level.getId());
        intent.putExtra(ExamPlayerActivity.EXTRA_LEVEL_NAME, level.getName());
        examLauncher.launch(intent);
    }

    private ArrayList<ExamQuestion> buildQuestions(List<KanjiDto> selected) {
        ArrayList<ExamQuestion> questions = new ArrayList<>();
        List<KanjiDto> pool = new ArrayList<>(selected);
        Collections.shuffle(pool);
        Set<String> answerCache = new HashSet<>();
        for (KanjiDto item : pool) {
            ExamQuestion question = new ExamQuestion();
            question.setPrompt(getString(R.string.exam_question_prompt, item.getCharacter()));
            List<ExamChoice> choices = new ArrayList<>();
            String correctAnswer = resolveAnswer(item);
            choices.add(new ExamChoice(correctAnswer, true));
            answerCache.clear();
            answerCache.add(correctAnswer.toLowerCase(Locale.ROOT));

            List<KanjiDto> distractors = new ArrayList<>(pool);
            Collections.shuffle(distractors);
            int targetChoices = Math.min(4, pool.size());
            for (KanjiDto distractor : distractors) {
                if (distractor.getId() == item.getId()) {
                    continue;
                }
                String candidate = resolveAnswer(distractor);
                String key = candidate.toLowerCase(Locale.ROOT);
                if (answerCache.contains(key)) {
                    candidate = candidate + " (" + distractor.getCharacter() + ")";
                    key = candidate.toLowerCase(Locale.ROOT);
                    if (answerCache.contains(key)) {
                        continue;
                    }
                }
                choices.add(new ExamChoice(candidate, false));
                answerCache.add(key);
                if (choices.size() >= targetChoices) {
                    break;
                }
            }

            if (choices.size() < 2) {
                choices.add(new ExamChoice(correctAnswer + " ✕", false));
            }
            while (choices.size() < targetChoices) {
                String filler = correctAnswer + " " + (choices.size() + 1);
                answerCache.add(filler.toLowerCase(Locale.ROOT));
                choices.add(new ExamChoice(filler, false));
            }
            Collections.shuffle(choices);
            question.getChoices().addAll(choices);
            questions.add(question);
        }
        return questions;
    }

    private String resolveAnswer(@NonNull KanjiDto dto) {
        if (!TextUtils.isEmpty(dto.getHanViet())) {
            return dto.getHanViet();
        }
        if (!TextUtils.isEmpty(dto.getOnReading())) {
            return dto.getOnReading();
        }
        if (!TextUtils.isEmpty(dto.getKunReading())) {
            return dto.getKunReading();
        }
        if (!TextUtils.isEmpty(dto.getDescription())) {
            return dto.getDescription();
        }
        return dto.getCharacter();
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showEmptyState(boolean empty) {
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (rvKanji != null) {
            rvKanji.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
    }

    private void updateLastResult() {
        if (currentLevel == null) {
            tvLastResult.setText(R.string.exam_last_result_none);
            return;
        }
        ExamResultStore.ExamResult result = resultStore.load(authPrefs.userId(), currentLevel.getId());
        if (result == null) {
            tvLastResult.setText(R.string.exam_last_result_none);
        } else {
            String time = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                    .format(new Date(result.getTimestamp()));
            tvLastResult.setText(getString(R.string.exam_last_result, result.getScore(), result.getTotal(), time));
        }
    }

    private List<String> toJlptNames(List<JlptLevelDto> data) {
        List<String> result = new ArrayList<>();
        for (JlptLevelDto dto : data) {
            result.add(dto.getNameLevel());
        }
        return result;
    }

    private List<String> toLevelNames(List<LevelDto> data) {
        List<String> result = new ArrayList<>();
        for (LevelDto dto : data) {
            result.add(dto.getName());
        }
        return result;
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
