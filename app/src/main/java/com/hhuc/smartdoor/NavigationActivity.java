package com.hhuc.smartdoor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

import static com.hhuc.smartdoor.MainApplication.app_hid;
import static com.hhuc.smartdoor.MainApplication.app_tel;
import static com.hhuc.smartdoor.MainApplication.app_uid;


public class NavigationActivity extends AppCompatActivity {

    public final static String TAG = "NavigationActivity";
    private long mExitTime;//声明一个long类型变量：用于存放上一点击“返回键”的时刻

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_webrtc, R.id.navigation_account)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        init();
    }

    private void init() {
        try {
            Map<String, String> res;
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            Intent intent = getIntent();
            String userInfoJson = intent.getStringExtra("userInfo");
            res = new Gson().fromJson(userInfoJson, type);
            app_uid=res.get("uid");
            app_hid=res.get("hid");
            app_tel=res.get("tel");

        } catch (Exception er) {
            Log.e(TAG, "init" + er.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {

        } catch (Exception er) {
            Log.e(TAG, "onActivityResult: " + er.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            //大于2000ms则认为是误操作，使用Toast进行提示
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            //并记录下本次点击“返回键”的时刻，以便下次进行判断
            mExitTime = System.currentTimeMillis();
        } else {
            //小于2000ms则认为是用户确实希望退出程序-调用System.exit()方法进行退出
            System.exit(0);
        }
    }
}
