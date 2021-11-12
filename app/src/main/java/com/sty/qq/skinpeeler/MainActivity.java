package com.sty.qq.skinpeeler;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.sty.qq.skinpeeler.adapter.MyFragmentPagerAdapter;
import com.sty.qq.skinpeeler.fragment.BuyFragment;
import com.sty.qq.skinpeeler.fragment.HomeFragment;
import com.sty.qq.skinpeeler.fragment.PersonalFragment;
import com.sty.qq.skinpeeler.utils.PermissionUtils;
import com.sty.qq.skinpeeler.widget.CustomTabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: ShiTianyi
 * Time: 2021/10/28 0028 19:45
 * Description:
 */
public class MainActivity extends AppCompatActivity {
    private String[] needPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private View rootView;
    private Button btnChangeSkin;
    private CustomTabLayout customTabLayout;
    private ViewPager viewPager;
    private List<Fragment> fragmentList;
    private List<String> titles;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 在这里监听的一样，ApplicationActivityLifecycle, 晚了一步
        // 就一定会 if (mFactorySet) { throw new IllegalStateException("A factory has already been set on this LayoutInflater"); }

        setContentView(R.layout.activity_main);

        if (!PermissionUtils.checkPermissions(this, needPermissions)) {
            PermissionUtils.requestPermissions(this, needPermissions);
        }

        initView();
        initListeners();
    }

    private void initView() {
        rootView = findViewById(R.id.ll_root);
        btnChangeSkin = findViewById(R.id.btn_change_skin);
        customTabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        fragmentList = new ArrayList<>();
        fragmentList.add(new HomeFragment());
        fragmentList.add(new BuyFragment());
        fragmentList.add(new PersonalFragment());

        titles = new ArrayList<>();
        titles.add("首页");
        titles.add("购买");
        titles.add("个人中心");

        MyFragmentPagerAdapter myFragmentPagerAdapter =
                new MyFragmentPagerAdapter(getSupportFragmentManager(), titles, fragmentList);
        viewPager.setAdapter(myFragmentPagerAdapter);
        customTabLayout.setupWithViewPager(viewPager);
    }

    private void initListeners() {
        btnChangeSkin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SkinActivity.class));
            }
        });
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
