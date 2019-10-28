package com.demo.rmonkey.util;

import android.app.Activity;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.Toast;

import com.demo.rmonkey.R;
import com.ycbjie.notificationlib.NotificationUtils;

import java.util.List;

import static com.demo.rmonkey.util.Const.PKGNAME;

public class ActivityUtil {
    public static void openActivity(Activity activity) {
        // 获取包管理器
        PackageManager manager = activity.getPackageManager();
        // 指定入口,启动类型,包名
        Intent intent = getIntent();
        //查询要启动的Activity
        List<ResolveInfo> apps = manager.queryIntentActivities(intent, 0);
        if (apps.size() > 0) {//如果包名存在
            ResolveInfo ri = apps.get(0);
            // //获取包名
            String packageName = ri.activityInfo.packageName;
            //获取app启动类型
            String className = ri.activityInfo.name;
            //组装包名和类名
            ComponentName cn = new ComponentName(packageName, className);
            //设置给Intent
            intent.setComponent(cn);
            //根据包名类型打开Activity
            activity.startActivity(intent);
        } else {
            Toast.makeText(activity.getApplicationContext(), "未安装对应APP", Toast.LENGTH_SHORT).show();

            NotificationUtils notificationUtils = new NotificationUtils(activity.getApplicationContext());
            Notification notification = notificationUtils.getNotification("打开应用失败", "请安装对应APP", R.mipmap.ic_launcher);
            notificationUtils.getManager().notify(1, notification);
        }
    }

    public static Intent getIntent() {
        Intent intent = new Intent(Intent.ACTION_MAIN);//入口Main
        intent.addCategory(Intent.CATEGORY_LAUNCHER);// 启动LAUNCHER,跟MainActivity里面的配置类似
        intent.setPackage(PKGNAME);//包名

        return intent;
    }
}
