package com.sty.qq.skinpeeler;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author: ShiTianyi
 * Time: 2021/10/20 0020 19:28
 * Description: 收集View，然后还需要做兼容
 */
public class SkinFactory implements LayoutInflater.Factory2 {
    //收集后的View空间，需要兼容处理
    private AppCompatDelegate mDelegate;
    //装载需要换肤的View的容器
    private List<SkinView> cacheSkinViews = new ArrayList<>();

    //缓存（用到了反射）
    private static final Class<?>[] mConstructorSignature = new Class[] {Context.class, AttributeSet.class};
    private static final HashMap<String, Constructor<? extends View>> sConstructorMap = new HashMap<>();
    private final Object[] mConstructorArgs = new Object[2];


    //只要是符合这个包名，就是系统的控件
    private static final String[] sClassPrefixList = {
            "android.widget.",
            "android.view.",
            "android.webkit.",
            "android.app."
    };

    public void setmDelegate(AppCompatDelegate mDelegate) {
        this.mDelegate = mDelegate;
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View view = mDelegate.createView(parent, name, context, attrs);
        if(view == null) {
            mConstructorArgs[0] = context;
            if(-1 == name.indexOf('.')) { //系统控件
                view = createView(context, name, sClassPrefixList, attrs);
            }else { //是我们自定义控件
                view = createView(context, name, null, attrs);
            }
        }
        if(view != null) {
            collectSkinView(context, attrs, view);
        }
        return view;
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return null;
    }

    //需要换肤控件的描述对象
    static class SkinView {
        View view;
        HashMap<String, String> attrsMap;

        //换肤能力的行为函数
        public void changeSkin() {
            if(!TextUtils.isEmpty(attrsMap.get("background"))) {
                int bgId = Integer.parseInt(attrsMap.get("background").substring(1));
                String attrType = view.getResources().getResourceTypeName(bgId);
                if(TextUtils.equals(attrType, "drawable")) {
                    view.setBackgroundDrawable(SkinEngine.getInstance().getDrawable(bgId));
                }else if(TextUtils.equals(attrType, "color")) {
                    view.setBackgroundColor(SkinEngine.getInstance().getColor(bgId));
                }
            }

            if(!TextUtils.isEmpty(attrsMap.get("textColor"))) {
                int textColorId = Integer.parseInt(attrsMap.get("textColor").substring(1));
                if(view instanceof TextView) {
                    ((TextView) view).setTextColor(SkinEngine.getInstance().getColor(textColorId));
                    Log.e("sty", "color id: " + SkinEngine.getInstance().getColor(textColorId));
                }else if(view instanceof Button) {
                    ((Button) view).setTextColor(SkinEngine.getInstance().getColor(textColorId));
                }
            }

//            if(view instanceof TextView) {
//                if(!TextUtils.isEmpty(attrsMap.get("textColor"))) {
//                    try {
//                        int textColorId = Integer.parseInt(attrsMap.get("textColor").substring(1));
//                        ((TextView) view).setTextColor(SkinEngine.getInstance().getColor(textColorId));
//                    }catch (Exception e) {
//                        e.printStackTrace();
//                        Log.e("sty", "my error1: " + e.getMessage());
//                    }
//                }
//            }
        }

        //恢复默认
        @RequiresApi(api = Build.VERSION_CODES.M)
        public void resetSkin(Context context) {
            if(!TextUtils.isEmpty(attrsMap.get("background"))) {
                int bgId = Integer.parseInt(attrsMap.get("background").substring(1));
                String attrType = view.getResources().getResourceTypeName(bgId);
                if(TextUtils.equals(attrType, "drawable")) {
                    view.setBackgroundDrawable(context.getDrawable(bgId));
                }else if(TextUtils.equals(attrType, "color")) {
                    view.setBackgroundColor(context.getColor(bgId));
                }
            }

            if(!TextUtils.isEmpty(attrsMap.get("textColor"))) {
                int textColorId = Integer.parseInt(attrsMap.get("textColor").substring(1));
                if(view instanceof TextView) {
                    ((TextView) view).setTextColor(context.getColor(textColorId));
                }else if(view instanceof Button) {
                    ((Button) view).setTextColor(context.getColor(textColorId));
                }
            }

//            if(view instanceof TextView) {
//                if(!TextUtils.isEmpty(attrsMap.get("textColor"))) {
//                    try {
//                        int textColorId = Integer.parseInt(attrsMap.get("textColor").substring(1));
//                        ((TextView) view).setTextColor(context.getColor(textColorId));
//                    }catch (Exception e) {
//                        e.printStackTrace();
//                        Log.e("sty", "my error2: " + e.getMessage());
//                    }
//                }
//            }
        }
    }

    //系统控件和自定义控件的公共方法
    //反射实例化View：使用了缓存机制，区分系统控件和自定义控件
    public final View createView(Context context, String name, String[] prefixs, AttributeSet attrs) {
        //先从缓存中获取
        Constructor<? extends View> constructor = sConstructorMap.get(name);
        Class<? extends View> clazz = null;

        if(constructor == null) {
            try {
                if(prefixs != null && prefixs.length > 0) {
                    for (String prefix : prefixs) {
                        // android.view.  TextView
                        clazz = context.getClassLoader().loadClass(prefix != null ? (prefix + name) : name).asSubclass(View.class);
                        if(clazz != null) {
                            break;
                        }
                    }
                } else {
                    clazz = context.getClassLoader().loadClass(name).asSubclass(View.class);
                }

                if(clazz == null) {
                    return null;
                }

                constructor = clazz.getConstructor(mConstructorSignature);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            constructor.setAccessible(true);
            //最终把 class丢给缓存
            sConstructorMap.put(name, constructor);
        }

        Object[] args = mConstructorArgs;
        args[1] = attrs;

        //最终就可以通过反射实例化了
        try {
            View view = constructor.newInstance(args);
            return view;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    //收集需要换肤的控件
    private void collectSkinView(Context context, AttributeSet attrs, View view) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Skinnable);
        boolean isSkin = a.getBoolean(R.styleable.Skinnable_isSkin, false);
        if(isSkin) {
            final int  len = attrs.getAttributeCount();
            HashMap<String, String> attrMap = new HashMap<>();
            for (int i = 0; i < len; i++) {
                String attributeName = attrs.getAttributeName(i);
                String attributeValue = attrs.getAttributeValue(i);
                attrMap.put(attributeName, attributeValue);
            }

            //换肤对象定义出来
            SkinView skinView = new SkinView();
            skinView.view = view;
            skinView.attrsMap = attrMap;
            //最后要存入到缓存中
            cacheSkinViews.add(skinView);
        }
        //收尾操作
        a.recycle();
    }

    /**
     * 对外暴露可以换肤
     */
    public void changeSkin() {
        for (SkinView cacheSkinView : cacheSkinViews) {
            cacheSkinView.changeSkin();
        }
    }

    /**
     * 对外暴露恢复默认皮肤
     * @param context
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void resetSkin(Context context) {
        for (SkinView cacheSkinView : cacheSkinViews) {
            cacheSkinView.resetSkin(context);
        }
    }
}
