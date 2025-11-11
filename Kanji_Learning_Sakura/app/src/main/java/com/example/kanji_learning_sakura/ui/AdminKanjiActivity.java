package com.example.kanji_learning_sakura.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
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
import com.example.kanji_learning_sakura.core.KanjiService;
import com.example.kanji_learning_sakura.model.BulkImportReportDto;
import com.example.kanji_learning_sakura.model.JlptLevelDto;
import com.example.kanji_learning_sakura.model.AdminMemberDto;
import com.example.kanji_learning_sakura.model.KanjiDto;
import com.example.kanji_learning_sakura.model.LevelDto;
import com.example.kanji_learning_sakura.model.VipPlanDto;
import com.example.kanji_learning_sakura.ui.adapter.AdminKanjiAdapter;
import com.example.kanji_learning_sakura.ui.adapter.AdminMemberAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

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
    private RecyclerView rvKanji;
    private AdminKanjiAdapter adapter;
    private CircularProgressIndicator progressKanji;
    private TextView tvEmpty;
    private CharSequence emptyMessage;
    private LevelDto currentLevel;
    private ChipGroup chipVipPlans;
    private ChipGroup chipMemberFilter;
    private RecyclerView rvMembers;
    private AdminMemberAdapter memberAdapter;
    private CircularProgressIndicator progressMembers;
    private TextView tvMembersEmpty;
    private final List<VipPlanDto> vipPlans = new ArrayList<>();
    private String currentMemberFilter = "";

    private final ActivityResultLauncher<Intent> refreshLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> reloadLevels());

    private final ActivityResultLauncher<String> csvPicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onCsvPicked);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_kanji);
        service = new KanjiService(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbarAdmin);
        toolbar.setNavigationOnClickListener(v -> finish());

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
        rvKanji = findViewById(R.id.rvKanji);
        progressKanji = findViewById(R.id.progressKanji);
        tvEmpty = findViewById(R.id.tvEmpty);
        emptyMessage = tvEmpty.getText();
        chipVipPlans = findViewById(R.id.chipVipPlans);
        chipMemberFilter = findViewById(R.id.chipMemberFilter);
        rvMembers = findViewById(R.id.rvAdminMembers);
        progressMembers = findViewById(R.id.progressMembers);
        tvMembersEmpty = findViewById(R.id.tvMembersEmpty);

        rvKanji.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminKanjiAdapter(new AdminKanjiAdapter.Listener() {
            @Override
            public void onEdit(@NonNull KanjiDto item) {
                showEditDialog(item);
            }

            @Override
            public void onDelete(@NonNull KanjiDto item) {
                confirmDelete(item);
            }
        });
        rvKanji.setAdapter(adapter);

        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        memberAdapter = new AdminMemberAdapter(this::confirmApproveRequest);
        rvMembers.setAdapter(memberAdapter);

        loadVipPlans();
        loadJlpt();
        loadMembers(currentMemberFilter);

        spJlpt.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < jlptLevels.size()) {
                loadLevels(jlptLevels.get(position).getId());
            }
        });

        spLevel.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < levels.size()) {
                currentLevel = levels.get(position);
                loadKanji(currentLevel);
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

        chipMemberFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int checkedId = checkedIds.isEmpty() ? View.NO_ID : checkedIds.get(0);
            currentMemberFilter = filterForChip(checkedId);
            loadMembers(currentMemberFilter);
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
                        JlptLevelDto first = data.get(0);
                        loadLevels(first.getId());
                        spJlpt.setText(first.getNameLevel(), false);
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
                    if (!data.isEmpty()) {
                        currentLevel = data.get(0);
                        spLevel.setText(currentLevel.getName(), false);
                        loadKanji(currentLevel);
                    } else {
                        currentLevel = null;
                        adapter.submitList(new ArrayList<>());
                        showEmptyState(true, emptyMessage.toString());
                    }
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
                    loadKanji(level);
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

    private void loadKanji(LevelDto level) {
        setKanjiLoading(true);
        new Thread(() -> {
            try {
                List<KanjiDto> data = service.getKanjiByLevel(level.getId());
                runOnUiThread(() -> {
                    setKanjiLoading(false);
                    adapter.submitList(data);
                    showEmptyState(data.isEmpty(), emptyMessage.toString());
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    setKanjiLoading(false);
                    showEmptyState(true, getString(R.string.toast_load_kanji_failed, ex.getMessage()));
                    toast(getString(R.string.toast_load_kanji_failed, ex.getMessage()));
                });
            }
        }).start();
    }

    private void setKanjiLoading(boolean loading) {
        progressKanji.setVisibility(loading ? View.VISIBLE : View.GONE);
        rvKanji.setAlpha(loading ? 0.3f : 1f);
    }

    private void showEmptyState(boolean show, String message) {
        tvEmpty.setText(message);
        tvEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private String filterForChip(int chipId) {
        if (chipId == R.id.chipFilterPending) {
            return "pending";
        }
        if (chipId == R.id.chipFilterVip) {
            return "vip";
        }
        if (chipId == R.id.chipFilterFree) {
            return "free";
        }
        return "";
    }

    private void loadVipPlans() {
        new Thread(() -> {
            try {
                List<VipPlanDto> data = service.getVipPlans();
                runOnUiThread(() -> renderVipPlans(data));
            } catch (Exception ex) {
                runOnUiThread(() -> toast(getString(R.string.toast_load_plans_failed, ex.getMessage())));
            }
        }).start();
    }

    private void renderVipPlans(List<VipPlanDto> plans) {
        vipPlans.clear();
        vipPlans.addAll(plans);
        chipVipPlans.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        if (plans.isEmpty()) {
            Chip chip = (Chip) inflater.inflate(R.layout.chip_vip_plan, chipVipPlans, false);
            chip.setText(R.string.member_plan_empty);
            chipVipPlans.addView(chip);
            return;
        }
        for (VipPlanDto plan : plans) {
            Chip chip = (Chip) inflater.inflate(R.layout.chip_vip_plan, chipVipPlans, false);
            String description = plan.getDescription() != null && !plan.getDescription().isEmpty()
                    ? plan.getDescription()
                    : plan.getCode();
            chip.setText(getString(R.string.member_plan_format, description, formatCurrency(plan.getAmount())));
            chipVipPlans.addView(chip);
        }
    }

    private void loadMembers(String filter) {
        setMembersLoading(true);
        new Thread(() -> {
            try {
                List<AdminMemberDto> data = service.getAdminMembers(filter);
                runOnUiThread(() -> {
                    setMembersLoading(false);
                    memberAdapter.submitList(data);
                    showMembersEmptyState(data.isEmpty(), getString(R.string.empty_members));
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    setMembersLoading(false);
                    showMembersEmptyState(true, getString(R.string.toast_load_members_failed, ex.getMessage()));
                    toast(getString(R.string.toast_load_members_failed, ex.getMessage()));
                });
            }
        }).start();
    }

    private void setMembersLoading(boolean loading) {
        progressMembers.setVisibility(loading ? View.VISIBLE : View.GONE);
        rvMembers.setAlpha(loading ? 0.3f : 1f);
    }

    private void showMembersEmptyState(boolean show, String message) {
        tvMembersEmpty.setText(message);
        tvMembersEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        rvMembers.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
    }

    private void confirmApproveRequest(@NonNull AdminMemberDto member) {
        if (member.getRequestId() == null) {
            toast(getString(R.string.toast_member_approve_failed, getString(R.string.member_request_status_pending)));
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.member_confirm_approve_title)
                .setMessage(getString(R.string.member_confirm_approve_message, member.getUserName()))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.member_action_approve, (dialog, which) -> approveVipRequest(member))
                .show();
    }

    private void approveVipRequest(@NonNull AdminMemberDto member) {
        setMembersLoading(true);
        new Thread(() -> {
            try {
                service.approveVipRequest(member.getRequestId());
                runOnUiThread(() -> {
                    toast(getString(R.string.toast_member_approved, member.getUserName()));
                    loadMembers(currentMemberFilter);
                });
            } catch (IllegalStateException ex) {
                runOnUiThread(() -> {
                    setMembersLoading(false);
                    toast(getString(R.string.toast_member_approve_failed, ex.getMessage()));
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    setMembersLoading(false);
                    toast(getString(R.string.toast_member_approve_failed, getString(R.string.msg_network_error)));
                });
            }
        }).start();
    }

    private String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        format.setMaximumFractionDigits(0);
        format.setMinimumFractionDigits(0);
        return format.format(amount);
    }

    private void showEditDialog(KanjiDto item) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_kanji, null, false);
        TextInputEditText edtDialogKanji = view.findViewById(R.id.edtDialogKanji);
        TextInputEditText edtDialogHanViet = view.findViewById(R.id.edtDialogHanViet);
        TextInputEditText edtDialogOn = view.findViewById(R.id.edtDialogOn);
        TextInputEditText edtDialogKun = view.findViewById(R.id.edtDialogKun);
        TextInputEditText edtDialogDescription = view.findViewById(R.id.edtDialogDescription);
        MaterialAutoCompleteTextView spDialogLevel = view.findViewById(R.id.spDialogLevel);

        edtDialogKanji.setText(item.getCharacter());
        edtDialogHanViet.setText(item.getHanViet());
        edtDialogOn.setText(item.getOnReading());
        edtDialogKun.setText(item.getKunReading());
        edtDialogDescription.setText(item.getDescription());

        List<LevelDto> selectableLevels = new ArrayList<>();
        selectableLevels.add(null);
        selectableLevels.addAll(levels);
        List<String> levelNames = new ArrayList<>();
        levelNames.add(getString(R.string.label_no_level));
        levelNames.addAll(toLevelNames(levels));
        spDialogLevel.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, levelNames));
        LevelDto initialLevel = findLevelById(item.getLevelId());
        if (initialLevel == null) {
            initialLevel = currentLevel != null ? currentLevel : getSelectedLevel();
        }
        int initialIndex = 0;
        if (initialLevel != null) {
            for (int i = 1; i < selectableLevels.size(); i++) {
                LevelDto level = selectableLevels.get(i);
                if (level != null && level.getId() == initialLevel.getId()) {
                    initialIndex = i;
                    break;
                }
            }
        }
        spDialogLevel.setText(levelNames.get(initialIndex), false);
        final LevelDto[] selectedLevelHolder = new LevelDto[]{selectableLevels.get(initialIndex)};
        spDialogLevel.setOnItemClickListener((parent, v1, position, id) -> selectedLevelHolder[0] = selectableLevels.get(position));

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_edit_kanji_title)
                .setView(view)
                .setPositiveButton(R.string.dialog_edit_kanji_positive, (dialog, which) -> {
                    String newChar = textOf(edtDialogKanji);
                    if (newChar.isEmpty()) {
                        toast(getString(R.string.hint_kanji_char));
                        return;
                    }
                    String newHan = textOf(edtDialogHanViet);
                    String newOn = textOf(edtDialogOn);
                    String newKun = textOf(edtDialogKun);
                    String newDesc = textOf(edtDialogDescription);
                    LevelDto level = selectedLevelHolder[0];
                    Integer levelId = level != null ? level.getId() : null;
                    updateKanji(item.getId(), newChar, newHan, newOn, newKun, newDesc, levelId);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void updateKanji(long id, String character, String hanViet, String on, String kun,
                              String description, Integer levelId) {
        setKanjiLoading(true);
        new Thread(() -> {
            try {
                KanjiDto dto = service.updateKanji(id, character, hanViet, on, kun, description, levelId);
                runOnUiThread(() -> {
                    setKanjiLoading(false);
                    toast(getString(R.string.toast_update_kanji, dto.getCharacter()));
                    if (currentLevel != null) {
                        loadKanji(currentLevel);
                    }
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    setKanjiLoading(false);
                    toast(ex.getMessage());
                });
            }
        }).start();
    }

    private void confirmDelete(KanjiDto item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_kanji_title)
                .setMessage(getString(R.string.dialog_delete_kanji_message, item.getCharacter()))
                .setPositiveButton(android.R.string.ok, (dialog, which) -> deleteKanji(item.getId()))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deleteKanji(long id) {
        setKanjiLoading(true);
        new Thread(() -> {
            try {
                service.deleteKanji(id);
                runOnUiThread(() -> {
                    setKanjiLoading(false);
                    toast(getString(R.string.toast_delete_kanji));
                    if (currentLevel != null) {
                        loadKanji(currentLevel);
                    }
                });
            } catch (Exception ex) {
                runOnUiThread(() -> {
                    setKanjiLoading(false);
                    toast(ex.getMessage());
                });
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

    private LevelDto findLevelById(Integer id) {
        if (id == null) {
            return null;
        }
        for (LevelDto level : levels) {
            if (level.getId() == id) {
                return level;
            }
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
