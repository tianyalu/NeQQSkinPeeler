package com.sty.qq.skin_lib.core;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

/**
 * Author: ShiTianyi
 * Time: 2021/10/29 0029 20:29
 * Description:
 */
public class SkinResources {
    private static volatile SkinResources instance; //单例
    private String mSkinPackageName; //皮肤包中的包名：null == 不合格的皮肤包，否则就是合格的皮肤包，可以换肤
    private boolean isDefaultSkin = true; //默认情况下可以恢复默认的皮肤

    //app壳本身原生的Resources
    private Resources mAppResources;
    //皮肤包外界加载专用的Resources
    private Resources mSkinResources;

    private SkinResources(Context context) {
        this.mAppResources = context.getResources();
    }

    public static void init(Context context) {
        if(instance == null) {
            synchronized (SkinResources.class) {
                if(instance == null) {
                    instance = new SkinResources(context);
                }
            }
        }
    }

    /**
     * 对外暴露
     * @return
     */
    public static SkinResources getInstance() {
        return instance;
    }

    /**
     * 换肤的准备工作
     * @param resources
     * @param packageName
     */
    public void applySkinAction(Resources resources, String packageName) {
        this.mSkinResources = resources;
        this.mSkinPackageName = packageName;

        //一切换肤都是基于标记
        isDefaultSkin = TextUtils.isEmpty(packageName) || resources == null;
    }

    /**
     * 恢复默认的准备工作
     */
    public void resetSkinAction() {
        mSkinResources = null;
        mSkinPackageName = null;
        isDefaultSkin = true;  //一旦变为true就会恢复默认
    }

    /**
     * 暴露：专门去加载皮肤包的资源信息
     * @param resID
     * @return
     */
    public int getIdentifier(int resID) {
        if(isDefaultSkin) {
            return resID; //恢复默认，全部结束
        }

        //此时可以拿到皮肤包里面的所有资源了
        String resName = mAppResources.getResourceEntryName(resID);
        String resType = mAppResources.getResourceTypeName(resID);
        int skinId = mSkinResources.getIdentifier(resName, resType, mSkinPackageName);

        return skinId; //拿到皮肤包资源的skinId,通过此skinId可以直接拿到皮肤包的资源了
    }

    /**
     * 直接拿到皮肤包的Color颜色
     * @param resID
     * @return
     */
    public int getColor(int resID) {
        if(isDefaultSkin) {
            return mAppResources.getColor(resID); //默认APP壳的颜色
        }
        int skinId = getIdentifier(resID);
        if(skinId == 0) {
            return mAppResources.getColor(resID); //默认APP壳的颜色
        }

        return mSkinResources.getColor(skinId); //真正拿到的皮肤包资源的颜色
    }

    /**
     * 另一种获取颜色的方式
     * @param resId
     * @return
     */
    public ColorStateList getColorStateList(int resId) {
        if(isDefaultSkin) {
            return mAppResources.getColorStateList(resId); //默认APP壳的颜色
        }
        int skinId = getIdentifier(resId);
        if(skinId == 0) {
            return mAppResources.getColorStateList(resId); //默认APP壳的颜色
        }

        return mSkinResources.getColorStateList(skinId); //真正拿到的皮肤包资源的颜色
    }

    public Drawable getDrawable(int resId) {
        if(isDefaultSkin) {
            return mAppResources.getDrawable(resId); //默认APP壳的图片
        }
        int skinId = getIdentifier(resId);
        if(skinId == 0) {
            return mAppResources.getDrawable(resId); //默认APP壳的图片
        }

        return mSkinResources.getDrawable(skinId); //真正拿到的皮肤包资源的图片
    }

    /**
     * 专门定制的background
     * @param resId
     * @return
     */
    public Object getBackground(int resId) {
        String resourceTypeName = mAppResources.getResourceTypeName(resId);
        if("color".equalsIgnoreCase(resourceTypeName)) { //color
            return getColor(resId);
        }else { //drawable
            return getDrawable(resId);
        }
    }
}
