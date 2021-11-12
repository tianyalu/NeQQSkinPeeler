package com.sty.qq.skinpeeler;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Author: ShiTianyi
 * Time: 2021/10/21 0021 20:18
 * Description: 专门加载外界皮肤包的资源信息
 */
public class SkinEngine {
    //单例模式
    private static final SkinEngine instance = new SkinEngine();
    private Context mContext;
    //专门服务于外界皮肤包资源的Resource
    private Resources mOutResource;
    //专门服务于外界皮肤包资源的 packageName
    private String mOutPackageName;

    public static SkinEngine getInstance() {
        return instance;
    }

    private SkinEngine() {

    }

    //给Application来初始化的
    public void init(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * 开始加载外界皮肤包资源
     * @param skinPackagePath
     */
    public void loading(final String skinPackagePath) {
        File file = new File(skinPackagePath);
        if(!file.exists()) {
            return;
        }

        mOutPackageName = getOutPackageName(skinPackagePath);
        if(mOutPackageName == null) {
            return;
        }
        //创建资源
        createResource(skinPackagePath);
    }

    /**
     * 通过皮肤包的路径得到皮肤包的完整包名，如果得不到则证明这个皮肤包是不合格的或者损坏的
     * @param skinPackagePath
     * @return
     */
    private String getOutPackageName(String skinPackagePath) {
        PackageManager packageManager = mContext.getPackageManager();
        PackageInfo mInfo = packageManager.getPackageArchiveInfo(skinPackagePath, PackageManager.GET_ACTIVITIES);
        if(mInfo == null) {
            return null;
        }
        return mInfo.packageName;
    }

    /**
     * 通过皮肤包路径去创建Resource, 此Resource专门服务于外界皮肤包资源的Resource
     * @param skinPackagePath
     */
    private void createResource(String skinPackagePath) {
        mOutResource = new Resources(createAssetManagerAction(skinPackagePath),
                mContext.getResources().getDisplayMetrics(), mContext.getResources().getConfiguration());
    }

    /**
     * 专门创建AssetManager,并把皮肤包的路径丢给AssetManager，然后返回AssetManager
     * @param skinPackagePath
     * @return
     */
    private AssetManager createAssetManagerAction(final String skinPackagePath) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            //把 skinPackagePath 皮肤包路径丢给 AssetManager.java --> JNI --> AssetManager.cpp --> 加载到skinPackagePath的皮肤包资源（颜色、图片等等）
            Method addAssetPath = assetManager.getClass().getDeclaredMethod("addAssetPath", String.class);
            addAssetPath.setAccessible(true);
            addAssetPath.invoke(assetManager, skinPackagePath);
            return assetManager;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过这个函数，拿到皮肤包外界资源的颜色
     * @param resId
     * @return
     */
    public int getColor(int resId) {
        if(mOutResource == null) {
            return resId;
        }
        try {
            String resName = mOutResource.getResourceEntryName(resId); //my_red
            int outResId = mOutResource.getIdentifier(resName, "color", mOutPackageName);
            if(outResId == 0) {
                return resId; //加载本地当前默认的 color
            }
            return mOutResource.getColor(outResId); //加载外界皮肤包资源，例如：颜色、图片等等
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resId;
    }

    /**
     * 通过这个函数，拿到皮肤包外界资源的颜色
     * @param resId
     * @return
     */
    public Drawable getDrawable(int resId) {
        if(mOutResource == null) {
            return ContextCompat.getDrawable(mContext, resId); //加载本地当前默认的Drawable
        }

        try {
            String resName = mOutResource.getResourceEntryName(resId); //qq_bg
            int outResId = mOutResource.getIdentifier(resName, "drawable", mOutPackageName);
            if(outResId == 0) {
                return ContextCompat.getDrawable(mContext, resId); //加载本地当前默认的Drawable
            }
            return mOutResource.getDrawable(outResId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ContextCompat.getDrawable(mContext, resId); //加载本地当前默认的Drawable
    }

    /**
     * 通过这个函数，拿到皮肤包外界资源的文字颜色
     * @param resId
     * @return
     */
    public int getTextColor(int resId) {
        if(mOutResource == null) {
            return resId;
        }
        try {
            String resName = mOutResource.getResourceEntryName(resId); //my_red
            int outResId = mOutResource.getIdentifier(resName, "textColor", mOutPackageName);
            if(outResId == 0) {
                return resId; //加载本地当前默认的 color
            }
            return mOutResource.getColor(outResId); //加载外界皮肤包资源，例如：颜色、图片等等
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resId;
    }
}
