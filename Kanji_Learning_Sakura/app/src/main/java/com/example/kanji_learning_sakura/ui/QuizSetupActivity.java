package com.example.kanji_learning_sakura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.core.KanjiService;
import com.example.kanji_learning_sakura.model.JlptLevelDto;
import com.example.kanji_learning_sakura.model.LessonDto;
import com.example.kanji_learning_sakura.model.LevelDto;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import java.util.ArrayList;
import java.util.List;

/**
 * Chọn JLPT -> Level -> Lesson rồi chuyển sang màn quiz.
 */
public class QuizSetupActivity extends AppCompatActivity {

    private KanjiService service;
    private MaterialAutoCompleteTextView spJlpt;
    private MaterialAutoCompleteTextView spLevel;
    private MaterialAutoCompleteTextView spLesson;
    private final List<JlptLevelDto> jlptLevels = new ArrayList<>();
    private final List<LevelDto> levels = new ArrayList<>();
    private final List<LessonDto> lessons = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_setup);
        service = new KanjiService(this);

        spJlpt = findViewById(R.id.spQuizJlpt);
        spLevel = findViewById(R.id.spQuizLevel);
        spLesson = findViewById(R.id.spLesson);
        MaterialButton btnStart = findViewById(R.id.btnStartQuiz);

        loadJlpt();

        spJlpt.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < jlptLevels.size()) {
                loadLevels(jlptLevels.get(position).getId());
            }
        });
        spLevel.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < levels.size()) {
                loadLessons(levels.get(position).getId());
            }
        });

        btnStart.setOnClickListener(v -> startQuiz());
    }

    private void loadJlpt() {
        new Thread(() -> {
            try {
                List<JlptLevelDto> data = service.getJlptLevels();
                runOnUiThread(() -> {
                    jlptLevels.clear();
                    jlptLevels.addAll(data);
                    spJlpt.setAdapter(new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1,
                            toJlptNames(data)));
                });
            } catch (Exception ex) {
                runOnUiThread(() -> toast(ex.getMessage()));
            }
        }).start();
    }

    private void loadLevels(int jlptId) {
        new Thread(() -> {
            try {
                List<LevelDto> data = service.getLevels(jlptId);
                runOnUiThread(() -> {
                    levels.clear();
                    levels.addAll(data);
                    spLevel.setAdapter(new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1,
                            toLevelNames(data)));
                    spLevel.showDropDown();
                    if (!data.isEmpty()) {
                        spLevel.setText(data.get(0).getName(), false);
                        loadLessons(data.get(0).getId());
                    }
                });
            } catch (Exception ex) {
                runOnUiThread(() -> toast(ex.getMessage()));
            }
        }).start();
    }

    private void loadLessons(int levelId) {
        new Thread(() -> {
            try {
                List<LessonDto> data = service.getLessons(levelId);
                runOnUiThread(() -> {
                    lessons.clear();
                    lessons.addAll(data);
                    spLesson.setAdapter(new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1,
                            toLessonNames(data)));
                    spLesson.showDropDown();
                    if (!data.isEmpty()) {
                        spLesson.setText(data.get(0).getTitle(), false);
                    }
                });
            } catch (Exception ex) {
                runOnUiThread(() -> toast(ex.getMessage()));
            }
        }).start();
    }

    private void startQuiz() {
        int levelIndex = spLevel.getListSelection();
        int lessonIndex = spLesson.getListSelection();
        if (lessonIndex < 0 && !lessons.isEmpty()) {
            String text = spLesson.getText() != null ? spLesson.getText().toString() : null;
            if (text != null) {
                for (int i = 0; i < lessons.size(); i++) {
                    if (text.equals(lessons.get(i).getTitle())) {
                        lessonIndex = i;
                        break;
                    }
                }
            }
            if (lessonIndex < 0) {
                lessonIndex = 0;
            }
        }
        if (lessonIndex < 0 || lessonIndex >= lessons.size()) {
            toast(getString(R.string.toast_choose_lesson));
            return;
        }
        LessonDto lesson = lessons.get(lessonIndex);
        Intent intent = new Intent(this, QuizPlayerActivity.class);
        intent.putExtra(QuizPlayerActivity.EXTRA_LESSON_ID, lesson.getLessonId());
        intent.putExtra(QuizPlayerActivity.EXTRA_LESSON_TITLE, lesson.getTitle());
        startActivity(intent);
    }

    private List<String> toJlptNames(List<JlptLevelDto> items) {
        List<String> result = new ArrayList<>();
        for (JlptLevelDto dto : items) {
            result.add(dto.getNameLevel());
        }
        return result;
    }

    private List<String> toLevelNames(List<LevelDto> items) {
        List<String> result = new ArrayList<>();
        for (LevelDto dto : items) {
            result.add(dto.getName());
        }
        return result;
    }

    private List<String> toLessonNames(List<LessonDto> items) {
        List<String> result = new ArrayList<>();
        for (LessonDto dto : items) {
            result.add(dto.getTitle());
        }
        return result;
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
