package com.demo.rmonkey;

import android.app.Notification;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.demo.rmonkey.util.OpenAccessibilitySettingHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ycbjie.notificationlib.NotificationUtils;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        OpenAccessibilitySettingHelper.jumpToSettingPage(this.getApplicationContext());

        NotificationUtils notificationUtils = new NotificationUtils(this);
        notificationUtils
                .setOngoing(false)
                .setFlags(Notification.FLAG_NO_CLEAR)
                .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .sendNotification(1, "应用已启动", "请在系统设置页中开启BOSS服务", R.mipmap.ic_launcher);
    }
}
