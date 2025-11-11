package com.example.kanji_learning_sakura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.config.AuthPrefs;
import com.example.kanji_learning_sakura.core.KanjiService;
import com.example.kanji_learning_sakura.model.JlptLevelDto;
import com.example.kanji_learning_sakura.model.LessonDto;
import com.example.kanji_learning_sakura.model.LevelDto;
import com.example.kanji_learning_sakura.ui.adapter.LevelDropdownAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Chọn JLPT -> Level -> Lesson rồi chuyển sang màn quiz.
 */
public class QuizSetupActivity extends AppCompatActivity {

    private KanjiService service;
    private AuthPrefs authPrefs;
    private MaterialAutoCompleteTextView spJlpt;
    private MaterialAutoCompleteTextView spLevel;
    private MaterialAutoCompleteTextView spLesson;
    private MaterialButton btnStart;
    private final List<JlptLevelDto> jlptLevels = new ArrayList<>();
    private final List<LevelDto> levels = new ArrayList<>();
    private final List<LessonDto> lessons = new ArrayList<>();
    private LevelDropdownAdapter levelAdapter;
    private LevelDto currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_setup);
        service = new KanjiService(this);
        authPrefs = new AuthPrefs(this);

        spJlpt = findViewById(R.id.spQuizJlpt);
        spLevel = findViewById(R.id.spQuizLevel);
        spLesson = findViewById(R.id.spLesson);
        btnStart = findViewById(R.id.btnStartQuiz);
        levelAdapter = new LevelDropdownAdapter(this);
        spLevel.setAdapter(levelAdapter);

        loadJlpt();

        spJlpt.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < jlptLevels.size()) {
                loadLevels(jlptLevels.get(position).getId());
            }
        });
        spLevel.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < levels.size()) {
                handleLevelSelection(levels.get(position), true);
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
                    levelAdapter.submit(data, authPrefs.isVipUser());
                    spLevel.showDropDown();
                    selectDefaultLevel();
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
                    spLesson.setEnabled(true);
                    spLesson.setAlpha(1f);
                    btnStart.setEnabled(!data.isEmpty());
                    spLesson.showDropDown();
                    if (!data.isEmpty()) {
                        spLesson.setText(data.get(0).getTitle(), false);
                    } else {
                        spLesson.setText("", false);
                    }
                });
            } catch (Exception ex) {
                runOnUiThread(() -> toast(ex.getMessage()));
            }
        }).start();
    }

    /**
     * Chọn level mặc định phù hợp với quyền truy cập.
     */
    private void selectDefaultLevel() {
        if (levels.isEmpty()) {
            currentLevel = null;
            lessons.clear();
            showLockedLessons(true);
            return;
        }
        LevelDto preferred = null;
        for (LevelDto level : levels) {
            if (!levelAdapter.isLocked(level)) {
                preferred = level;
                break;
            }
        }
        if (preferred == null) {
            preferred = levels.get(0);
        }
        spLevel.setText(levelAdapter.displayName(preferred), false);
        handleLevelSelection(preferred, false);
    }

    /**
     * Xử lý khi người dùng chọn level.
     *
     * @param level    level đã chọn.
     * @param fromUser {@code true} nếu hành động đến từ người dùng.
     */
    private void handleLevelSelection(LevelDto level, boolean fromUser) {
        currentLevel = level;
        if (levelAdapter.isLocked(level)) {
            showLockedLessons(true);
            if (fromUser) {
                toast(getString(R.string.msg_paid_content_requires_vip));
            }
        } else {
            showLockedLessons(true);
            loadLessons(level.getId());
        }
    }

    /**
     * Hiển thị trạng thái khóa bài học đối với level yêu cầu VIP.
     *
     * @param clearSelection {@code true} nếu cần xóa lựa chọn bài học hiện tại.
     */
    private void showLockedLessons(boolean clearSelection) {
        if (clearSelection) {
            lessons.clear();
        }
        if (currentLevel == null || levelAdapter.isLocked(currentLevel)) {
            String placeholder = getString(R.string.label_locked_lesson_placeholder);
            spLesson.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1,
                    Collections.singletonList(placeholder)));
            spLesson.setText(placeholder, false);
            spLesson.setEnabled(false);
            spLesson.setAlpha(0.35f);
            btnStart.setEnabled(false);
        } else if (clearSelection) {
            spLesson.setAdapter(null);
            spLesson.setText("", false);
            spLesson.setEnabled(true);
            spLesson.setAlpha(1f);
            btnStart.setEnabled(false);
        }
    }

    private void startQuiz() {
        if (currentLevel != null && levelAdapter.isLocked(currentLevel)) {
            toast(getString(R.string.msg_paid_content_requires_vip));
            return;
        }
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
