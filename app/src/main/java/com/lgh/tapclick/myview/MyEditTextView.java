package com.lgh.tapclick.myview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.res.ResourcesCompat;

import com.lgh.tapclick.R;

public class MyEditTextView extends AppCompatEditText {

    private Drawable cleanBtnDraw;
    private boolean cleanBtnDrawVisible;
    private boolean cleanTouchDown;
    private int curPaddingEnd;

    public MyEditTextView(@NonNull Context context) {
        super(context);
        init();
    }

    public MyEditTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyEditTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        post(new Runnable() {
            @Override
            public void run() {
                cleanBtnDraw = ResourcesCompat.getDrawable(getResources(), R.drawable.text_clean_btn, null);
                float scaleRate = (float) (getHeight() - 15) / cleanBtnDraw.getIntrinsicHeight();
                cleanBtnDraw.setBounds(0, 0, Math.round(cleanBtnDraw.getIntrinsicWidth() * scaleRate), getHeight() - 15);
                addTextChangedListener(new TextWatcher() {
                    {
                        updateCleanBtnDrawState();
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        updateCleanBtnDrawState();
                    }
                });
            }
        });
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        float right = getWidth() - curPaddingEnd;
        float left = right - (float) cleanBtnDraw.getBounds().right / 2;
        if (event.getX() >= left && event.getX() <= right) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                cleanTouchDown = true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (cleanTouchDown) {
                    cleanTouchDown = false;
                    setText(null);
                }
            }
            return true;
        }
        cleanTouchDown = false;
        return super.onTouchEvent(event);
    }

    private void updateCleanBtnDrawState() {
        if (TextUtils.isEmpty(getText())) {
            if (cleanBtnDrawVisible) {
                setCompoundDrawables(null, null, null, null);
                setPadding(getPaddingLeft(), getPaddingTop(), 0, getPaddingBottom());
                cleanBtnDrawVisible = false;
            }
        } else {
            float textWidth = getPaint().measureText(getText().toString());
            curPaddingEnd = (int) Math.max(getWidth() - textWidth - cleanBtnDraw.getBounds().right, 0);
            setPadding(getPaddingLeft(), getPaddingTop(), curPaddingEnd, getPaddingBottom());
            if (!cleanBtnDrawVisible) {
                setCompoundDrawables(null, null, cleanBtnDraw, null);
                cleanBtnDrawVisible = true;
            }
        }
    }
}
