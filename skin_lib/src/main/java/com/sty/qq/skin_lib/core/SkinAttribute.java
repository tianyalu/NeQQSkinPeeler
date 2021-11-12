package com.sty.qq.skin_lib.core;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.view.ViewCompat;

import com.sty.qq.skin_lib.iinterface.SkinViewSupport;
import com.sty.qq.skin_lib.util.SkinUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: ShiTianyi
 * Time: 2021/10/29 0029 20:09
 * Description: 具体换肤的详情
 */
public class SkinAttribute {
    private static final List<String> mAttributes = new ArrayList<>();
    //把所有需要换肤的View和属性信息集合
    private List<SkinView> mSkinViews = new ArrayList<>();

    static {
        mAttributes.add("background");
        mAttributes.add("src");
        mAttributes.add("textColor");
        mAttributes.add("drawableLeft");
        mAttributes.add("drawableTop");
        mAttributes.add("drawableRight");
        mAttributes.add("drawableBottom");
    }

    public void lookAction(View view, AttributeSet attrs) {
        List<SkinPair> mSkinPair = new ArrayList<>();

        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            //获取名称 textColor
            String attributeName = attrs.getAttributeName(i);
            if(mAttributes.contains(attributeName)) {
                String attributeValue = attrs.getAttributeValue(i);
                //# 开头的，#f00 就没有资格换肤，也没法换肤
                if(attributeValue.startsWith("#")) {
                    continue;
                }

                int resId;
                //如果是？开头的，我们就要去使用该属性
                if(attributeValue.startsWith("?")) {
                    int attrId = Integer.parseInt(attributeValue.substring(1));
                    resId = SkinUtil.getResId(view.getContext(), new int[]{attrId})[0];
                }else { //正常情况下以@开头
                    resId = Integer.parseInt(attributeValue.substring(1));
                }

                SkinPair skinPair = new SkinPair(attributeName, resId);
                mSkinPair.add(skinPair);
            }
        }

        if(!mSkinPair.isEmpty() || view instanceof SkinViewSupport) {
            SkinView skinView = new SkinView(view, mSkinPair);

            skinView.changeSkin();
            mSkinViews.add(skinView);
        }
    }

    // TextView Button, ... 此View的描述对象
    static class SkinView{
        View view;
        List<SkinPair> skinPairs;

        public SkinView(View view, List<SkinPair> skinPairs) {
            this.view = view;
            this.skinPairs = skinPairs;
        }

        private void changeSkinAction() {
            if(view instanceof SkinViewSupport) {
                ((SkinViewSupport) view).runSkin();
            }
        }
        //换肤
        public void changeSkin() {
            changeSkinAction();
            for (SkinPair skinPair : skinPairs) {
                Drawable left = null;
                Drawable right = null;
                Drawable top = null;
                Drawable bottom = null;
                switch (skinPair.attributeName) {
                    case "background":
                        Object background = SkinResources.getInstance().getBackground(skinPair.resId);
                        //背景可能是color 也可能是drawable
                        if(background instanceof Integer) {
                            view.setBackgroundColor((int) background);
                        }else {
                            ViewCompat.setBackground(view, (Drawable) background);
                        }
                        break;
                    case "src":
                        background = SkinResources.getInstance().getBackground(skinPair.resId);
                        //背景可能是color 也可能是drawable
                        if(background instanceof Integer) {
                            ((ImageView) view).setImageDrawable(new ColorDrawable((Integer)background));
                        }else {
                            ((ImageView) view).setImageDrawable((Drawable) background);
                        }
                        break;
                    case "textColor":
                        ((TextView) view).setTextColor(SkinResources.getInstance().getColorStateList(skinPair.resId));
                        break;
                    case "drawableLeft":
                        left = SkinResources.getInstance().getDrawable(skinPair.resId);
                        break;
                    case "drawableRight":
                        right = SkinResources.getInstance().getDrawable(skinPair.resId);
                        break;
                    case "drawableTop":
                        top = SkinResources.getInstance().getDrawable(skinPair.resId);
                        break;
                    case "drawableBottom":
                        bottom = SkinResources.getInstance().getDrawable(skinPair.resId);
                        break;
                    default:
                        break;
                }
                if(null != left || null != right || null != top || null != bottom) {
                    ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
                }
                    
            }
        }
    }

    //属性名和属性ID的对应关系
    static class SkinPair {
        //属性名
        String attributeName;
        //属性ID
        int resId;

        public SkinPair(String attributeName, int resId) {
            this.attributeName = attributeName;
            this.resId = resId;
        }
    }

    /**
     * 对所有的View中的所有属性进行换肤操作
     */
    public void changeSkin() {
        for (SkinView mSkinView : mSkinViews) {
            mSkinView.changeSkin();
        }
    }
}
