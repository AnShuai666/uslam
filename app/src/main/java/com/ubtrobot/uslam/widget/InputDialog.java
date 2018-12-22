package com.ubtrobot.uslam.widget;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.EditText;
import android.widget.TextView;
import com.ubtrobot.uslam.R;

/**
 * @author leo
 * @date 2018/12/14
 * @email ao.liu@ubtrobot.com
 */
public class InputDialog extends Dialog {

    public InputDialog(@NonNull Context context) {
        this(context, 0);
    }

    public InputDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public enum Type {
        START,
        MIDDLE,
        END
    }

    public interface OnClickListener {
        void onClick(Type type, InputDialog dialog, String inputStr);
    }

    public static class Builder {
        private Context mContext;
        private String mTitle;
        private OnClickListener mListener;
        private TextView mTvTitle;
        private TextView mTvSave;
        private TextView mTvNoSave;
        private TextView mTvCancel;
        private EditText mEtInput;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setTitle(int title) {
            mTitle = mContext.getResources().getString(title);
            return this;
        }

        public Builder addListener(OnClickListener listener) {
            mListener = listener;
            return this;
        }

        public InputDialog create() {
            InputDialog dialog = new InputDialog(mContext);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_input);
            mTvTitle = dialog.findViewById(R.id.title);
            mTvSave = dialog.findViewById(R.id.save);
            mTvNoSave = dialog.findViewById(R.id.no_save);
            mTvCancel = dialog.findViewById(R.id.cancel);
            mEtInput = dialog.findViewById(R.id.et_input);

            mTvTitle.setText(mTitle);

            mTvSave.setOnClickListener(v -> {
                mListener.onClick(Type.START, dialog, getInputText());
            });
            mTvNoSave.setOnClickListener(v -> {
                mListener.onClick(Type.MIDDLE, dialog, getInputText());
            });
            mTvCancel.setOnClickListener(v -> {
                mListener.onClick(Type.END, dialog, getInputText());
            });
            return dialog;
        }

        private String getInputText() {
            return mEtInput.getText().toString().trim();
        }

    }

}
