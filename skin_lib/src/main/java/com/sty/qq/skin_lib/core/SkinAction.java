package com.sty.qq.skin_lib.core;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import java.lang.reflect.Method;
import java.util.Observable;

/**
 * Author: ShiTianyi
 * Time: 2021/10/29 0029 19:56
 * Description:
 */
public class SkinAction extends Observable {
    private static volatile SkinAction instance;
    private Application application;
    private ApplicationActivityLifecycle applicationActivityLifecycle;  //Activity的生命周期

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private SkinAction(Application application) {
        this.application = application;
        //共享首选项，用于记录当前使用的皮肤
        SkinPreference.init(application);

        //资源管理类，用于从APP/皮肤中加载资源
        SkinResources.init(application);

        applicationActivityLifecycle = new ApplicationActivityLifecycle(this);
        application.registerActivityLifecycleCallbacks(applicationActivityLifecycle);

        //为了加载上次保存的图片
        loadSkinPackage(SkinPreference.getInstance().getSkin());
    }

    /**
     * 必须调用这个方法才能使用该类
     * @param application
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void initAction(Application application) {
        if(instance == null) {
            synchronized (SkinAction.class) {
                if(instance == null) {
                    instance = new SkinAction(application);
                }
            }
        }
    }

    /**
     * 单例模式，对外暴露
     * @return
     */
    public static SkinAction getInstance() {
        return instance;
    }

    /**
     * 加载皮肤包
     * @param skinPath
     */
    public void loadSkinPackage(String skinPath) {
        if(TextUtils.isEmpty(skinPath)) {
            //如果皮肤包的路径为null，就直接恢复默认的皮肤
            SkinPreference.getInstance().reset();
            SkinResources.getInstance().resetSkinAction();
        }else {
            try {
                //本地默认APP壳的Resource
                Resources appResources = application.getResources();

                AssetManager assetManager = AssetManager.class.newInstance();
                //资源路径设置
                Method addAssetPath = assetManager.getClass().getDeclaredMethod("addAssetPath", String.class);
                addAssetPath.setAccessible(true);
                addAssetPath.invoke(assetManager, skinPath);

                //外界皮肤包的专用Resource
                Resources skinResource = new Resources(assetManager, appResources.getDisplayMetrics(),
                        appResources.getConfiguration());
                //获取外部apk皮肤包包名
                PackageManager packageManager = application.getPackageManager();
                PackageInfo packageArchiveInfo = packageManager.getPackageArchiveInfo(skinPath, PackageManager.GET_ACTIVITIES);
                String packageName = packageArchiveInfo.packageName;

                SkinResources.getInstance().applySkinAction(skinResource, packageName);

                //最后要记录
                SkinPreference.getInstance().setSkin(skinPath);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }

        //通知采集好的View执行换肤操作
        setChanged();
        notifyObservers();
    }
}
