package com.example.kanji_learning_sakura.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.core.KanjiService;
import com.example.kanji_learning_sakura.model.JlptLevelDto;
import com.example.kanji_learning_sakura.model.KanjiDto;
import com.example.kanji_learning_sakura.model.LevelDto;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình quản trị thêm Kanji.
 */
public class AdminKanjiActivity extends AppCompatActivity {

    private KanjiService service;
    private MaterialAutoCompleteTextView spJlpt;
    private MaterialAutoCompleteTextView spLevel;
    private final List<JlptLevelDto> jlptLevels = new ArrayList<>();
    private final List<LevelDto> levels = new ArrayList<>();
    private TextInputEditText edtKanji;
    private TextInputEditText edtHanViet;
    private TextInputEditText edtOn;
    private TextInputEditText edtKun;
    private TextInputEditText edtDescription;

    private final ActivityResultLauncher<Intent> refreshLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> reloadLevels());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_kanji);
        service = new KanjiService(this);

        spJlpt = findViewById(R.id.spAdminJlpt);
        spLevel = findViewById(R.id.spAdminLevel);
        edtKanji = findViewById(R.id.edtKanji);
        edtHanViet = findViewById(R.id.edtHanViet);
        edtOn = findViewById(R.id.edtOn);
        edtKun = findViewById(R.id.edtKun);
        edtDescription = findViewById(R.id.edtDescription);
        MaterialButton btnSave = findViewById(R.id.btnSaveKanji);
        MaterialButton btnCreateLevel = findViewById(R.id.btnCreateLevel);
        MaterialButton btnCreateLesson = findViewById(R.id.btnCreateLesson);

        loadJlpt();

        spJlpt.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < jlptLevels.size()) {
                loadLevels(jlptLevels.get(position).getId());
            }
        });

        btnSave.setOnClickListener(v -> saveKanji());
        btnCreateLevel.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateLevelActivity.class);
            refreshLauncher.launch(intent);
        });
        btnCreateLesson.setOnClickListener(v -> {
            if (levels.isEmpty()) {
                toast(getString(R.string.toast_choose_lesson));
                return;
            }
            LevelDto selected = getSelectedLevel();
            if (selected == null) {
                toast(getString(R.string.toast_choose_lesson));
                return;
            }
            Intent intent = new Intent(this, CreateLessonActivity.class);
            intent.putExtra(CreateLessonActivity.EXTRA_LEVEL_ID, selected.getId());
            refreshLauncher.launch(intent);
        });
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
                    if (!data.isEmpty()) {
                        loadLevels(data.get(0).getId());
                        spJlpt.setText(data.get(0).getNameLevel(), false);
                    }
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
                });
            } catch (Exception ex) {
                runOnUiThread(() -> toast(ex.getMessage()));
            }
        }).start();
    }

    private void reloadLevels() {
        LevelDto selected = getSelectedLevel();
        if (!jlptLevels.isEmpty()) {
            int selectedJlptId = selected != null ? selected.getJlptLevelId() : jlptLevels.get(0).getId();
            loadLevels(selectedJlptId);
        }
    }

    private void saveKanji() {
        LevelDto level = getSelectedLevel();
        if (level == null) {
            toast(getString(R.string.hint_select_level));
            return;
        }
        String character = textOf(edtKanji);
        if (character.isEmpty()) {
            edtKanji.setError(getString(R.string.hint_kanji_char));
            edtKanji.requestFocus();
            return;
        }
        String hanViet = textOf(edtHanViet);
        String on = textOf(edtOn);
        String kun = textOf(edtKun);
        String desc = textOf(edtDescription);

        new Thread(() -> {
            try {
                KanjiDto dto = service.createKanji(character, hanViet, on, kun, desc, level.getId());
                runOnUiThread(() -> {
                    toast(getString(R.string.toast_saved_kanji, dto.getCharacter()));
                    edtKanji.setText("");
                    edtHanViet.setText("");
                    edtOn.setText("");
                    edtKun.setText("");
                    edtDescription.setText("");
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
            if (index < 0) {
                index = 0;
            }
        }
        if (index >= 0 && index < levels.size()) {
            return levels.get(index);
        }
        return null;
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

    private String textOf(TextInputEditText editText) {
        CharSequence text = editText.getText();
        return text == null ? "" : text.toString().trim();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
