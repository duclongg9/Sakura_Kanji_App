package com.example.kanji_learning_sakura.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.core.KanjiService;
import com.example.kanji_learning_sakura.model.JlptLevelDto;
import com.example.kanji_learning_sakura.model.LevelDto;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

/**
 * Thêm Level mới cho JLPT tương ứng.
 */
public class CreateLevelActivity extends AppCompatActivity {

    private KanjiService service;
    private MaterialAutoCompleteTextView spJlpt;
    private MaterialAutoCompleteTextView spAccessTier;
    private final List<JlptLevelDto> jlptLevels = new ArrayList<>();
    private TextInputEditText edtName;
    private TextInputEditText edtDescription;
    private MaterialSwitch switchActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_level);
        service = new KanjiService(this);

        spJlpt = findViewById(R.id.spLevelJlpt);
        spAccessTier = findViewById(R.id.spAccessTier);
        edtName = findViewById(R.id.edtLevelName);
        edtDescription = findViewById(R.id.edtLevelDescription);
        switchActive = findViewById(R.id.switchActive);
        MaterialButton btnSave = findViewById(R.id.btnSaveLevel);

        loadJlpt();
        setupAccessTier();

        btnSave.setOnClickListener(v -> createLevel());
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

    private void setupAccessTier() {
        List<String> tiers = new ArrayList<>();
        tiers.add("FREE");
        tiers.add("PAID");
        spAccessTier.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tiers));
        spAccessTier.setText("FREE", false);
    }

    private void createLevel() {
        int jlptIndex = spJlpt.getListSelection();
        if (jlptIndex < 0 && !jlptLevels.isEmpty()) jlptIndex = 0;
        if (jlptIndex < 0 || jlptIndex >= jlptLevels.size()) {
            toast(getString(R.string.hint_select_jlpt));
            return;
        }
        String name = textOf(edtName);
        if (name.isEmpty()) {
            edtName.setError(getString(R.string.hint_level_name));
            edtName.requestFocus();
            return;
        }
        String description = textOf(edtDescription);
        String accessTier = spAccessTier.getText() != null ? spAccessTier.getText().toString() : "FREE";
        boolean active = switchActive.isChecked();
        int jlptId = jlptLevels.get(jlptIndex).getId();

        new Thread(() -> {
            try {
                LevelDto dto = service.createLevel(name, jlptId, description, accessTier, active);
                runOnUiThread(() -> {
                    toast(getString(R.string.toast_created_level, dto.getName()));
                    finish();
                });
            } catch (Exception ex) {
                runOnUiThread(() -> toast(ex.getMessage()));
            }
        }).start();
    }

    private List<String> toJlptNames(List<JlptLevelDto> items) {
        List<String> result = new ArrayList<>();
        for (JlptLevelDto dto : items) {
            result.add(dto.getNameLevel());
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
