package com.sty.qq.skin_lib.core;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.view.LayoutInflaterCompat;

import com.sty.qq.skin_lib.util.SkinUtil;

import java.lang.reflect.Field;
import java.util.Observable;

/**
 * Author: ShiTianyi
 * Time: 2021/10/29 0029 20:03
 * Description: 专门监听APP壳所以Activity的生命周期函数
 */
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class ApplicationActivityLifecycle implements Application.ActivityLifecycleCallbacks {
    private Observable observable;
    private ArrayMap<Activity, SkinLayoutInflaterFactory> mLayoutInflaterFactories = new ArrayMap<>();

    public ApplicationActivityLifecycle(Observable observable) {
        this.observable = observable;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        //更新状态栏
        SkinUtil.updateStatusBarColor(activity);

        LayoutInflater layoutInflater = activity.getLayoutInflater();
        //由于晚了一步，为了不发生异常，就需要通过反射修改 mFactorySet == false
        try {
            Field field = LayoutInflater.class.getDeclaredField("mFactorySet");
            field.setAccessible(true);
            field.setBoolean(layoutInflater, false);
        }catch (Exception e) {
            e.printStackTrace();
        }

        //使用我们自己的Factory2设置布局加载工厂
        SkinLayoutInflaterFactory skinLayoutInflaterFactory = new SkinLayoutInflaterFactory(activity);
        LayoutInflaterCompat.setFactory2(layoutInflater, skinLayoutInflaterFactory);
        mLayoutInflaterFactories.put(activity, skinLayoutInflaterFactory);

        //观察者和被观察者的关联
        observable.addObserver(skinLayoutInflaterFactory);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        //释放动作...S
        SkinLayoutInflaterFactory observe = mLayoutInflaterFactories.remove(activity);
        SkinAction.getInstance().deleteObserver(observe);
    }
}
