package com.sty.qq.skin_lib.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;

import com.sty.qq.skin_lib.core.SkinResources;

/**
 * Author: ShiTianyi
 * Time: 2021/11/1 0001 20:15
 * Description:
 */
public class SkinUtil {
    private static int[] APPCOMPAT_COLOR_PRIMARY_DARK_ATTRS = {
            androidx.appcompat.R.attr.colorPrimaryDark
    };

    private static int[] STATUS_BAR_COLOR_ATTRS = {
            android.R.attr.statusBarColor, android.R.attr.navigationBarColor
    };

    /**
     * 获得theme中的属性中定义的资源id
     * @param context
     * @param attrs
     * @return
     */
    public static int[] getResId(Context context, int[] attrs) {
        int[] resIds = new int[attrs.length];
        TypedArray a = context.obtainStyledAttributes(attrs);
        for (int i = 0; i < attrs.length; i++) {
            resIds[i] = a.getResourceId(i, 0);
        }
        a.recycle();
        return resIds;
    }

    public static void updateStatusBarColor(Activity activity) {
        //5.0以上才能修改
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        //获得 statusBarColor 与 navigationBarColor(状态栏颜色）
        //当与 colorPrimaryDark 不同时，以statusBarColor为准
        int[] resIds = getResId(activity, STATUS_BAR_COLOR_ATTRS);
        int statusBarColorResId = resIds[0];
        int navigationBarColor = resIds[1];

        //如果直接在style中写入固定颜色值（而不是@color/XXX)获得0
        if(statusBarColorResId != 0) {
            int color = SkinResources.getInstance().getColor(statusBarColorResId);
            activity.getWindow().setStatusBarColor(color);
        }else {
            //获得 colorPrimaryDark
            int colorPrimaryDarkResId = getResId(activity, APPCOMPAT_COLOR_PRIMARY_DARK_ATTRS)[0];
            if(colorPrimaryDarkResId != 0) {
                int color = SkinResources.getInstance().getColor(colorPrimaryDarkResId);
                activity.getWindow().setStatusBarColor(color);
            }
        }

        if(navigationBarColor != 0) {
            int color = SkinResources.getInstance().getColor(navigationBarColor);
            activity.getWindow().setNavigationBarColor(color);
        }
    }
}