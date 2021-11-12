package com.sty.qq.skinpeeler.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Author: ShiTianyi
 * Time: 2021/10/27 0027 20:09
 * Description: 自定义控件
 */
public class CustomView extends View {
    // 一般是被 Java代码  new Custom(this)
    public CustomView(Context context) {
        this(context, null);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
