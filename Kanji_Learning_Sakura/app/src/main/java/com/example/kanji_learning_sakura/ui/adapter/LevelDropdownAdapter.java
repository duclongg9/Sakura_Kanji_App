package com.example.kanji_learning_sakura.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.example.kanji_learning_sakura.R;
import com.example.kanji_learning_sakura.model.LevelDto;
import java.util.ArrayList;
import java.util.List;

/**
 * ArrayAdapter hiển thị danh sách level với trạng thái bị khóa cho tài khoản thường.
 */
public class LevelDropdownAdapter extends ArrayAdapter<LevelDto> {

    private boolean vipAccount;

    public LevelDropdownAdapter(@NonNull Context context) {
        super(context, android.R.layout.simple_list_item_1, new ArrayList<>());
    }

    /**
     * Cập nhật dữ liệu danh sách kèm trạng thái quyền truy cập hiện tại.
     *
     * @param items   danh sách level mới.
     * @param isVip   {@code true} nếu người dùng đang đăng nhập là VIP.
     */
    public void submit(@NonNull List<LevelDto> items, boolean isVip) {
        clear();
        addAll(items);
        this.vipAccount = isVip;
        notifyDataSetChanged();
    }

    /**
     * @param level level cần kiểm tra.
     * @return {@code true} nếu level yêu cầu VIP nhưng tài khoản hiện tại chưa phải VIP.
     */
    public boolean isLocked(LevelDto level) {
        return level != null
                && "PAID".equalsIgnoreCase(level.getAccessTier())
                && !vipAccount;
    }

    /**
     * Trả về tên hiển thị cho level, bổ sung nhãn VIP khi cần.
     */
    public String displayName(LevelDto level) {
        if (level == null) {
            return "";
        }
        if ("PAID".equalsIgnoreCase(level.getAccessTier())) {
            return getContext().getString(R.string.level_paid_label, level.getName());
        }
        return level.getName();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        return render(view, position);
    }

    @NonNull
    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        return render(view, position);
    }

    private View render(View view, int position) {
        LevelDto level = getItem(position);
        TextView tv = view.findViewById(android.R.id.text1);
        if (tv != null) {
            tv.setText(displayName(level));
            tv.setAlpha(isLocked(level) ? 0.4f : 1f);
        }
        return view;
    }
}
