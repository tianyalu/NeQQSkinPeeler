package com.sty.qq.skinpeeler;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.sty.qq.skinpeeler.utils.PermissionUtils;

import java.io.File;

public class QQActivity extends AppCompatActivity {
    private String[] needPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    protected SkinFactory skinFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //收集所有需要换肤的View【一部曲】
        skinFactory = new SkinFactory();
        skinFactory.setmDelegate(getDelegate());
        LayoutInflater.from(this).setFactory2(skinFactory);

        super.onCreate(savedInstanceState);

        //状态全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_qq);

        initListeners();

        if (!PermissionUtils.checkPermissions(this, needPermissions)) {
            PermissionUtils.requestPermissions(this, needPermissions);
        }
    }

    private void initListeners() {
        //换肤操作
        findViewById(R.id.bt_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSkin();
            }
        });
        //恢复默认皮肤操作
        findViewById(R.id.bt_reset).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                skinFactory.resetSkin(QQActivity.this);
            }
        });
    }

    public void startLogin(View view) {

    }

    /**
     * 换肤的方法
     */
    public void changeSkin() {
        //皮肤包从服务器下载到手机内存：sdcard/skin.skin
        File skinFile = new File(Environment.getExternalStorageDirectory() + "/sty", "skin.skin");
        SkinEngine.getInstance().loading(skinFile.getAbsolutePath());

        skinFactory.changeSkin();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PermissionUtils.REQUEST_PERMISSIONS_CODE) {
            if (!PermissionUtils.verifyPermissions(grantResults)) {
                PermissionUtils.showMissingPermissionDialog(this);
            } else {
                //initViews();
            }
        }
    }
}