package com.sty.qq.skinpeeler.app;

import android.app.Application;

import com.sty.qq.skin_lib.core.SkinAction;
import com.sty.qq.skinpeeler.SkinEngine;

/**
 * Author: ShiTianyi
 * Time: 2021/10/21 0021 20:21
 * Description:
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //SkinEngine.getInstance().init(this);
        SkinAction.initAction(this);
    }
}
