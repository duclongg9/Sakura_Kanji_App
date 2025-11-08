package com.example.kanji_learning_sakura.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.core.KanjiService;
import com.example.kanji_learning_sakura.model.JlptLevelDto;
import com.example.kanji_learning_sakura.model.KanjiDto;
import com.example.kanji_learning_sakura.model.LevelDto;
import com.example.kanji_learning_sakura.ui.adapter.KanjiAdapter;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình học Kanji theo JLPT -> Level.
 */
public class KanjiBrowserActivity extends AppCompatActivity {

    private KanjiService service;
    private MaterialAutoCompleteTextView spJlpt;
    private MaterialAutoCompleteTextView spLevel;
    private final List<JlptLevelDto> jlptLevels = new ArrayList<>();
    private final List<LevelDto> levels = new ArrayList<>();
    private final KanjiAdapter adapter = new KanjiAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kanji_browser);
        service = new KanjiService(this);

        spJlpt = findViewById(R.id.spJlpt);
        spLevel = findViewById(R.id.spLevel);
        RecyclerView rv = findViewById(R.id.rvKanji);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        loadJlptLevels();
        spJlpt.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < jlptLevels.size()) {
                loadLevels(jlptLevels.get(position).getId());
            }
        });

        spLevel.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < levels.size()) {
                loadKanji(levels.get(position).getId());
            }
        });
    }

    private void loadJlptLevels() {
        new Thread(() -> {
            try {
                List<JlptLevelDto> data = service.getJlptLevels();
                runOnUiThread(() -> {
                    jlptLevels.clear();
                    jlptLevels.addAll(data);
                    ArrayAdapter<String> adapterJlpt = new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1,
                            toJlptNames(data));
                    spJlpt.setAdapter(adapterJlpt);
                    if (!data.isEmpty()) {
                        spJlpt.setText(data.get(0).getNameLevel(), false);
                        loadLevels(data.get(0).getId());
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
                    ArrayAdapter<String> adapterLevel = new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_1,
                            toLevelNames(data));
                    spLevel.setAdapter(adapterLevel);
                    spLevel.showDropDown();
                    if (!data.isEmpty()) {
                        spLevel.setText(data.get(0).getName(), false);
                        loadKanji(data.get(0).getId());
                    }
                });
            } catch (Exception ex) {
                runOnUiThread(() -> toast(ex.getMessage()));
            }
        }).start();
    }

    private void loadKanji(int levelId) {
        new Thread(() -> {
            try {
                List<KanjiDto> data = service.getKanjiByLevel(levelId);
                runOnUiThread(() -> adapter.submit(data));
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

    private List<String> toLevelNames(List<LevelDto> items) {
        List<String> result = new ArrayList<>();
        for (LevelDto dto : items) {
            result.add(dto.getName());
        }
        return result;
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
