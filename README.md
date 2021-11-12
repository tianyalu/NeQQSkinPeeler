# NeQQSkinPeeler QQ换肤源码实践
[TOC]
## 一、理论
### 1.1 换肤三部曲
* 搜集需要换肤的`View`
* 加载皮肤包资源信息
* 具体换肤动作

### 1.2 与网易换肤框架的对比

网易的框架是用自定义的`View`替换原生的`View`，自定义的`View`实现了换肤接口，换肤时调用自定义的换肤接口实现换肤；而`QQ`换肤框架无需自定义`View`，只是会收集需要换肤的`View`的特定属性，并放在一个集合中，需要换肤时对这些属性做换肤操作。

## 二、实操

### 2.1 项目介绍

> 1. `app`模块为主应用 
> 2. `skin_lib`为换肤库的核心库
> 3. `qq_ui_package`为皮肤包模块

### 2.2 核心模块介绍

`ApplicationActivityLifecycle`在`Activity`的生命周期监听方法中设置换肤工厂并绑定观察者：`skinLayoutInflaterFactory`.

```java
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
    
	//...
    
    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        //释放动作...S
        SkinLayoutInflaterFactory observe = mLayoutInflaterFactories.remove(activity);
        SkinAction.getInstance().deleteObserver(observe);
    }
}
```

在换肤工厂`SkinLayoutInflaterFactory`中添通过反射生成`View`，并且收集需要换肤的元素，换肤时只需要针对这些元素做换肤动作即可：

```java
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

    private View createView( String name, Context context, AttributeSet attrs){
        Constructor<? extends View> constructor = findConstructor(context, name);
        try {
            return constructor.newInstance(context, attrs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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
```

同时本身作为观察者，当被观察者有换肤意向时做换肤动作：

```java
    public void update(Observable o, Object arg) {
        SkinUtil.updateStatusBarColor(activity);
        skinAttribute.changeSkin();
    }
```

`SkinAttribute.java`收集需要换肤控件的属性：

```java
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
```

`SkinView`执行换肤的内部类：

```java
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
```

`SkinAction`加载皮肤包资源：

```java
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
```

