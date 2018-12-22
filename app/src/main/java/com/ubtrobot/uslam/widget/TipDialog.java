package com.ubtrobot.uslam.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ubtrobot.uslam.R;
import com.ubtrobot.uslam.utils.ViewUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author leo
 * @date 2018/12/10
 * @email ao.liu@ubtrobot.com
 */
public class TipDialog extends Dialog {

    public TipDialog(@NonNull Context context) {
        super(context, R.style.TipDialog);
    }

    public TipDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        setCancelable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDialogWidth();
    }

    private void initDialogWidth() {
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams attributes =
                    window.getAttributes();
            attributes.width = ViewGroup.LayoutParams.MATCH_PARENT;
            window.setAttributes(attributes);
        }
    }

    public static class Builder {

        public static final int ICON_TYPE_NOTHING = 0;

        public static final int ICON_TYPE_LOADING = 1;

        public static final int ICON_TYPE_SUCCESS = 2;

        public static final int ICON_TYPE_FAIL = 3;

        public static final int ICON_TYPE_INFO = 4;

        @IntDef({ICON_TYPE_NOTHING, ICON_TYPE_LOADING, ICON_TYPE_SUCCESS, ICON_TYPE_FAIL, ICON_TYPE_INFO})
        @Retention(RetentionPolicy.SOURCE)
        public @interface IconType {

        }

        private @IconType
        int mCurrentIconType = ICON_TYPE_NOTHING;

        private Context mContext;

        private CharSequence mTipWord;

        private View.OnClickListener mListener;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder setIconType(@IconType int iconType) {
            mCurrentIconType = iconType;
            return this;
        }

        public Builder setTipWord(CharSequence tipWord) {
            mTipWord = tipWord;
            return this;
        }

        public TipDialog create() {
            return create(true);
        }

        public TipDialog create(boolean cancelable) {
            TipDialog dialog = new TipDialog(mContext);
            dialog.setCancelable(cancelable);
            dialog.setContentView(R.layout.dialog_tip);
            ViewGroup contentWrap = dialog.findViewById(R.id.contentWrap);

            if (mCurrentIconType == ICON_TYPE_LOADING) {
                LoadingView loadingView = new LoadingView(mContext);
                loadingView.setColor(Color.WHITE);
                loadingView.setSize(ViewUtils.dp2px(mContext, 32));
                LinearLayout.LayoutParams loadingViewLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                loadingView.setLayoutParams(loadingViewLP);
                contentWrap.addView(loadingView);
            } else if (mCurrentIconType == ICON_TYPE_SUCCESS || mCurrentIconType == ICON_TYPE_FAIL || mCurrentIconType == ICON_TYPE_INFO) {
                ImageView imageView = new ImageView(mContext);
                LinearLayout.LayoutParams imageViewLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                imageView.setLayoutParams(imageViewLP);

                if (mCurrentIconType == ICON_TYPE_SUCCESS) {
                    imageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.icon_notify_done));
                } else if (mCurrentIconType == ICON_TYPE_FAIL) {
                    imageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.icon_notify_error));
                } else {
                    imageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.mipmap.icon_notify_info));
                }

                contentWrap.addView(imageView);
            }


            if (mTipWord != null && mTipWord.length() > 0) {
                TextView tipView = new TextView(mContext);
                LinearLayout.LayoutParams tipViewLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                if (mCurrentIconType != ICON_TYPE_NOTHING) {
                    tipViewLP.topMargin = ViewUtils.dp2px(mContext, 12);
                }
                tipView.setLayoutParams(tipViewLP);

                tipView.setEllipsize(TextUtils.TruncateAt.END);
                tipView.setGravity(Gravity.CENTER);
                tipView.setMaxLines(2);
                tipView.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                tipView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                tipView.setText(mTipWord);

                contentWrap.addView(tipView);
            }
            return dialog;
        }
    }

    /**
     * 传入自定义的布局并使用这个布局生成 TipDialog
     */
    public static class CustomBuilder {
        private Context mContext;
        private int mContentLayoutId;

        public CustomBuilder(Context context) {
            mContext = context;
        }

        public CustomBuilder setContent(@LayoutRes int layoutId) {
            mContentLayoutId = layoutId;
            return this;
        }


        public TipDialog create() {
            TipDialog dialog = new TipDialog(mContext);
            dialog.setContentView(R.layout.dialog_tip);
            ViewGroup contentWrap = (ViewGroup) dialog.findViewById(R.id.contentWrap);
            LayoutInflater.from(mContext).inflate(mContentLayoutId, contentWrap, true);
            return dialog;
        }
    }
}
