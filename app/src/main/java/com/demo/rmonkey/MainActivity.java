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

//        Intent resultIntent = new Intent(this, MainActivity.class);
//        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);           //添加为栈顶Activity
//        resultIntent.putExtra("what",3);
//        PendingIntent resultPendingIntent = PendingIntent.getActivity(this,3,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);


        OpenAccessibilitySettingHelper.jumpToSettingPage(this.getApplicationContext());

        NotificationUtils notificationUtils = new NotificationUtils(this);
        notificationUtils
                .setOngoing(false)
                .setFlags(Notification.FLAG_NO_CLEAR)
//                .setContentIntent(resultPendingIntent)
                .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .sendNotification(1, "应用已启动", "请在系统设置页中开启虹猴服务", R.mipmap.ic_launcher);

//        AppStateTracker.track(getApplication(), new AppStateTracker.AppStateChangeListener() {
//            @Override
//            public void appTurnIntoForeground() {
//                // 处理app到前台的逻辑
//                final Button getAccountButton = findViewById(R.id.get_account);
//                final EditText account = findViewById(R.id.editText2);
////
//                SharedPreferences sp = getSharedPreferences(SPNAME, Context.MODE_PRIVATE);
//                String accountText = sp.getString("account", null);
////
//                getSupportFragmentManager().findFragmentById(R.layout.fragment_home);
//                if(accountText == null) {
////                    account.setText("未记录账啊号");
//                    Log.d("aa", "appTurnIntoForeground: " + account.getText());
////                    getAccountButton.setText("点击获取");
////                } else {
////                    account.setText(accountText);
////                    getAccountButton.setText("重新获取");
//                }
//            }
//
//            @Override
//            public void appTurnIntoBackGround() {
//                // app处理到到后台的逻辑
//            }
//        });

//        DaemonEnv.initialize(
//                this.getApplicationContext(),  //Application Context.
//                MyAbsWorkService.class, //刚才创建的 Service 对应的 Class 对象.
//                1);  //定时唤醒的时间间隔(ms), 默认 6 分钟.
//
//        this.getApplicationContext().startService(new Intent(this.getApplicationContext(),MyAbsWorkService.class));
    }
}
