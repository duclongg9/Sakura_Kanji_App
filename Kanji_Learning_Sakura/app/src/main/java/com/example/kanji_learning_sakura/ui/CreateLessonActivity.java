package com.example.kanji_learning_sakura.ui;

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
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

/**
 * Tạo bài học mới.
 */
public class CreateLessonActivity extends AppCompatActivity {

    public static final String EXTRA_LEVEL_ID = "levelId";

    private KanjiService service;
    private MaterialAutoCompleteTextView spLevel;
    private final List<LevelDto> levels = new ArrayList<>();
    private TextInputEditText edtTitle;
    private TextInputEditText edtOverview;
    private TextInputEditText edtOrder;
    private int preselectLevelId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_lesson);
        service = new KanjiService(this);
        preselectLevelId = getIntent().getIntExtra(EXTRA_LEVEL_ID, -1);

        spLevel = findViewById(R.id.spLessonLevel);
        edtTitle = findViewById(R.id.edtLessonTitle);
        edtOverview = findViewById(R.id.edtLessonOverview);
        edtOrder = findViewById(R.id.edtLessonOrder);
        MaterialButton btnSave = findViewById(R.id.btnSaveLesson);

        loadLevels();
        btnSave.setOnClickListener(v -> saveLesson());
    }

    private void loadLevels() {
        new Thread(() -> {
            try {
                List<JlptLevelDto> jlpt = service.getJlptLevels();
                List<LevelDto> all = new ArrayList<>();
                for (JlptLevelDto j : jlpt) {
                    all.addAll(service.getLevels(j.getId(), true));
                }
                runOnUiThread(() -> {
                    levels.clear();
                    levels.addAll(all);
                    spLevel.setAdapter(new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1,
                            toLevelNames(all)));
                    if (preselectLevelId > 0) {
                        for (int i = 0; i < levels.size(); i++) {
                            if (levels.get(i).getId() == preselectLevelId) {
                                spLevel.setText(levels.get(i).getName(), false);
                                return;
                            }
                        }
                    }
                    if (!levels.isEmpty()) {
                        spLevel.setText(levels.get(0).getName(), false);
                    }
                });
            } catch (Exception ex) {
                runOnUiThread(() -> toast(ex.getMessage()));
            }
        }).start();
    }

    private void saveLesson() {
        LevelDto selected = getSelectedLevel();
        if (selected == null) {
            toast(getString(R.string.hint_select_level));
            return;
        }
        String title = textOf(edtTitle);
        if (title.isEmpty()) {
            edtTitle.setError(getString(R.string.hint_lesson_title));
            edtTitle.requestFocus();
            return;
        }
        String overview = textOf(edtOverview);
        int order = 0;
        try {
            order = Integer.parseInt(textOf(edtOrder));
        } catch (NumberFormatException ignored) {
        }
        int finalOrder = order;
        new Thread(() -> {
            try {
                LessonDto dto = service.createLesson(selected.getId(), title, overview, finalOrder);
                runOnUiThread(() -> {
                    toast(getString(R.string.toast_created_lesson, dto.getTitle()));
                    finish();
                });
            } catch (Exception ex) {
                runOnUiThread(() -> toast(ex.getMessage()));
            }
        }).start();
    }

    private LevelDto getSelectedLevel() {
        int index = spLevel.getListSelection();
        if (index < 0 && !levels.isEmpty()) {
            String text = spLevel.getText() != null ? spLevel.getText().toString() : null;
            if (text != null) {
                for (int i = 0; i < levels.size(); i++) {
                    if (text.equals(levels.get(i).getName())) {
                        index = i;
                        break;
                    }
                }
            }
            if (index < 0) index = 0;
        }
        if (index >= 0 && index < levels.size()) {
            return levels.get(index);
        }
        return null;
    }

    private List<String> toLevelNames(List<LevelDto> items) {
        List<String> result = new ArrayList<>();
        for (LevelDto dto : items) {
            result.add(dto.isActive()
                    ? dto.getName()
                    : getString(R.string.label_level_hidden, dto.getName()));
        }
        return result;
    }

    private String textOf(TextInputEditText editText) {
        CharSequence text = editText.getText();
        return text == null ? "" : text.toString().trim();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
