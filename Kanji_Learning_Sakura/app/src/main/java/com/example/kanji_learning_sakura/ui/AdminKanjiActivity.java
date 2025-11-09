package com.example.kanji_learning_sakura.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.core.KanjiService;
import com.example.kanji_learning_sakura.model.BulkImportReportDto;
import com.example.kanji_learning_sakura.model.JlptLevelDto;
import com.example.kanji_learning_sakura.model.KanjiDto;
import com.example.kanji_learning_sakura.model.LevelDto;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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

    private final ActivityResultLauncher<String> csvPicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onCsvPicked);

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
        MaterialButton btnImportCsv = findViewById(R.id.btnImportCsv);

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
        btnImportCsv.setOnClickListener(v -> csvPicker.launch("text/*"));
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

    private void onCsvPicked(Uri uri) {
        if (uri == null) {
            return;
        }
        new Thread(() -> {
            try {
                byte[] data = readAllBytes(uri);
                String fileName = resolveDisplayName(uri);
                BulkImportReportDto report = service.importKanjiCsv(data, fileName);
                runOnUiThread(() -> showImportResult(report, fileName));
            } catch (Exception ex) {
                runOnUiThread(() -> toast(getString(R.string.toast_import_failed, ex.getMessage())));
            }
        }).start();
    }

    private byte[] readAllBytes(Uri uri) throws Exception {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new IllegalStateException(getString(R.string.toast_import_failed, getString(R.string.dialog_import_unknown_file)));
            }
            byte[] chunk = new byte[4096];
            int read;
            while ((read = inputStream.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            return buffer.toByteArray();
        }
    }

    private String resolveDisplayName(Uri uri) {
        String name = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        name = cursor.getString(index);
                    }
                }
            } finally {
                cursor.close();
            }
        }
        if (name == null) {
            name = uri.getLastPathSegment();
        }
        return name;
    }

    private void showImportResult(BulkImportReportDto report, String fileName) {
        String title = getString(R.string.dialog_import_title,
                fileName != null && !fileName.isEmpty() ? fileName : getString(R.string.dialog_import_unknown_file));
        StringBuilder message = new StringBuilder();
        message.append(getString(R.string.dialog_import_summary_line,
                report.getTotalRows(), report.getKanjiInserted(), report.getKanjiUpdated(),
                report.getQuestionsCreated(), report.getChoicesCreated()));
        if (!report.getErrors().isEmpty()) {
            message.append("\n\n").append(getString(R.string.dialog_import_errors_heading));
            int limit = Math.min(report.getErrors().size(), 5);
            for (int i = 0; i < limit; i++) {
                BulkImportReportDto.RowError error = report.getErrors().get(i);
                message.append("\n• ")
                        .append(getString(R.string.dialog_import_error_item, error.getRowNumber(), error.getMessage()));
            }
            if (report.getErrors().size() > limit) {
                message.append("\n").append(getString(R.string.dialog_import_more_errors,
                        report.getErrors().size() - limit));
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(message.toString())
                .setPositiveButton(android.R.string.ok, null)
                .show();

        if (report.getErrors().isEmpty()) {
            toast(getString(R.string.toast_import_success));
        } else {
            toast(getString(R.string.toast_import_completed_with_errors, report.getErrors().size()));
        }
        reloadLevels();
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
