package com.example.kanji_learning_sakura.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.config.AuthPrefs;
import com.example.kanji_learning_sakura.core.KanjiService;
import com.example.kanji_learning_sakura.model.JlptLevelDto;
import com.example.kanji_learning_sakura.model.KanjiDto;
import com.example.kanji_learning_sakura.model.LevelDto;
import com.example.kanji_learning_sakura.ui.adapter.KanjiAdapter;
import com.example.kanji_learning_sakura.ui.adapter.LevelDropdownAdapter;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình học Kanji theo JLPT -> Level.
 */
public class KanjiBrowserActivity extends AppCompatActivity {

    private KanjiService service;
    private AuthPrefs authPrefs;
    private MaterialAutoCompleteTextView spJlpt;
    private MaterialAutoCompleteTextView spLevel;
    private final List<JlptLevelDto> jlptLevels = new ArrayList<>();
    private final List<LevelDto> levels = new ArrayList<>();
    private final KanjiAdapter adapter = new KanjiAdapter();
    private LevelDropdownAdapter levelAdapter;
    private RecyclerView rvKanji;
    private LevelDto currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kanji_browser);
        service = new KanjiService(this);
        authPrefs = new AuthPrefs(this);

        spJlpt = findViewById(R.id.spJlpt);
        spLevel = findViewById(R.id.spLevel);
        levelAdapter = new LevelDropdownAdapter(this);
        spLevel.setAdapter(levelAdapter);
        rvKanji = findViewById(R.id.rvKanji);
        rvKanji.setLayoutManager(new LinearLayoutManager(this));
        rvKanji.setAdapter(adapter);

        loadJlptLevels();
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
                    levelAdapter.submit(data, authPrefs.isVipUser());
                    spLevel.showDropDown();
                    selectDefaultLevel();
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

    /**
     * Chọn level phù hợp nhất sau khi tải danh sách từ server.
     */
    private void selectDefaultLevel() {
        if (levels.isEmpty()) {
            currentLevel = null;
            adapter.submit(new ArrayList<>());
            setKanjiLocked(true);
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
     * Xử lý khi người dùng hoặc hệ thống chọn một level nhất định.
     *
     * @param level    level được chọn.
     * @param fromUser {@code true} nếu hành động xuất phát từ người dùng.
     */
    private void handleLevelSelection(LevelDto level, boolean fromUser) {
        currentLevel = level;
        boolean locked = levelAdapter.isLocked(level);
        if (locked) {
            adapter.submit(new ArrayList<>());
            setKanjiLocked(true);
            if (fromUser) {
                toast(getString(R.string.msg_paid_content_requires_vip));
            }
        } else {
            setKanjiLocked(false);
            loadKanji(level.getId());
        }
    }

    /**
     * Áp dụng trạng thái mờ/khóa cho danh sách Kanji dựa trên quyền truy cập.
     *
     * @param locked {@code true} nếu cần làm mờ và vô hiệu hóa tương tác.
     */
    private void setKanjiLocked(boolean locked) {
        if (rvKanji != null) {
            rvKanji.setAlpha(locked ? 0.35f : 1f);
            rvKanji.setEnabled(!locked);
        }
    }

    private List<String> toJlptNames(List<JlptLevelDto> items) {
        List<String> result = new ArrayList<>();
        for (JlptLevelDto dto : items) {
            result.add(dto.getNameLevel());
        }
        return result;
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
