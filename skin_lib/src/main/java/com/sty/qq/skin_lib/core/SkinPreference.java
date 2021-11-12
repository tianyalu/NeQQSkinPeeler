package com.sty.qq.skin_lib.core;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Author: ShiTianyi
 * Time: 2021/11/2 0002 20:17
 * Description: SP封装，用来保存换肤信息记录
 */
public class SkinPreference {
    private static final String SKIN_SHARED = "skins";
    private static final String KEY_SKIN_PATH = "skin-path";
    private static volatile SkinPreference instance;
    private final SharedPreferences mPref;

    private SkinPreference(Context context) {
        mPref = context.getSharedPreferences(SKIN_SHARED, Context.MODE_PRIVATE);
    }

    public static void init(Context context) {
        if(instance == null) {
            synchronized (SkinPreference.class) {
                if(instance == null) {
                    instance = new SkinPreference(context.getApplicationContext());
                }
            }
        }
    }

    public static SkinPreference getInstance() {
        return instance;
    }

    public void setSkin(String skinPath) {
        mPref.edit().putString(KEY_SKIN_PATH, skinPath).apply();
    }

    public void reset() {
        mPref.edit().remove(KEY_SKIN_PATH).apply();
    }

    public String getSkin() {
        return mPref.getString(KEY_SKIN_PATH, null);
    }
}
