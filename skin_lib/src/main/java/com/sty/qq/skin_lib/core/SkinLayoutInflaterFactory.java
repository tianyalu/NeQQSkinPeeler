package com.sty.qq.skin_lib.core;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sty.qq.skin_lib.util.SkinUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

/**
 * Author: ShiTianyi
 * Time: 2021/11/1 0001 20:43
 * Description: 观察者:盯着被观察者
 */
public class SkinLayoutInflaterFactory implements LayoutInflater.Factory2, Observer {
    //缓存（用到了反射）
    private static final Class<?>[] mConstructorSignature = new Class[] {Context.class, AttributeSet.class};
    private static final HashMap<String, Constructor<? extends View>> mConstructorMap = new HashMap<>();

    private SkinAttribute skinAttribute;
    private Activity activity;

    //只要是符合这个包名，就是系统的控件
    private static final String[] mClassPrefixList = {
            "android.widget.",
            "android.webkit.",
            "android.app.",
            "android.view."
    };

    public SkinLayoutInflaterFactory(Activity activity) {
        this.skinAttribute = new SkinAttribute();
        this.activity = activity;
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        //换肤就是在需要时候替换 View的属性(src、background等)
        //所以这里创建 View,从而修改View属性
        View view = createMyView(name, context, attrs);
        if (null == view) {
            view = createView(name, context, attrs);
        }
        //这就是我们加入的逻辑
        if (null != view) {
            //加载属性
            skinAttribute.lookAction(view, attrs);
        }
        return view;
    }

    private View createMyView(String name, Context context, AttributeSet attrs) {
        //如果包含 . 则不是SDK中的view 可能是自定义view包括support库中的View
        if (-1 != name.indexOf('.')) {
            return null;
        }
        //不包含就要在解析的 节点 name前，拼上： android.widget. 等尝试去反射
        for (int i = 0; i < mClassPrefixList.length; i++) {
            View view = createView(mClassPrefixList[i] + name, context, attrs);
            if(view!=null){
                return view;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return null;
    }

    private View createView( String name, Context context, AttributeSet attrs){
        Constructor<? extends View> constructor = findConstructor(context, name);
        try {
            return constructor.newInstance(context, attrs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void update(Observable o, Object arg) {
        SkinUtil.updateStatusBarColor(activity);
        skinAttribute.changeSkin();
    }

    /**
     * 先从缓存中找一找看有没有，如果有直接从缓存中取，如果没有就实例化好后放入缓存
     * @param context
     * @param name
     * @return
     */
    private Constructor<? extends View> findConstructor(Context context, String name) {
        Constructor<? extends View> constructor = mConstructorMap.get(name);
        if (constructor == null) {
            try {
                Class<? extends View> clazz = context.getClassLoader().loadClass
                        (name).asSubclass(View.class);
                constructor = clazz.getConstructor(mConstructorSignature);
                mConstructorMap.put(name, constructor);
            } catch (Exception e) {
            }
        }
        return constructor;
    }
}
