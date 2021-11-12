package com.sty.qq.skinpeeler;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sty.qq.skin_lib.core.SkinAction;

import java.io.File;

public class SkinActivity extends AppCompatActivity {
    private Button btnChangeSkin;
    private Button btnReset;
    private Button btnChangeSelector;
    private TextView tvChangeDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin);

        initView();
        intiListener();
    }

    private void initView() {
        btnChangeSkin = findViewById(R.id.btn_change_skin);
        btnReset = findViewById(R.id.btn_reset);
        btnChangeSelector = findViewById(R.id.btn_change_selector);
        tvChangeDrawable = findViewById(R.id.tv_change_drawable);
    }

    private void intiListener() {
        btnChangeSkin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File skinFile = new File(Environment.getExternalStorageDirectory() + File.separator + "sty", "skin.skin");
                Log.e("sty", skinFile.getAbsolutePath());
                SkinAction.getInstance().loadSkinPackage(skinFile.getAbsolutePath());
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SkinAction.getInstance().loadSkinPackage(null);
            }
        });

        btnChangeSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        tvChangeDrawable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}