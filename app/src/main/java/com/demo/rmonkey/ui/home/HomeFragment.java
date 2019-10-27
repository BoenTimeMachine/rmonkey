package com.demo.rmonkey.ui.home;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.demo.rmonkey.R;
import com.ycbjie.notificationlib.NotificationUtils;

import java.util.List;

import static com.demo.rmonkey.util.Const.PKGNAME;
import static com.demo.rmonkey.util.Const.SPNAME;


public class HomeFragment extends Fragment {


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        setView(root);

        return root;
    }

    class GetAccountOnclickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            SharedPreferences sp = v.getContext().getSharedPreferences(SPNAME, Context.MODE_PRIVATE);
            String accountText = sp.getString("account", null);

            SharedPreferences.Editor editor = sp.edit();

            if (accountText != null) {
                editor.putStringSet("account", null);
            }

            editor.putBoolean("geting-account", true);
            editor.apply();

            openActivity(v);

            NotificationUtils notificationUtils = new NotificationUtils(v.getContext());
            Notification notification = notificationUtils.getNotification("正在获取用户信息", "请稍等...", R.mipmap.ic_launcher);
            notificationUtils.getManager().notify(1, notification);
        }
    }

    public void openActivity(View view) {
        // 获取包管理器
        PackageManager manager = getActivity().getPackageManager();
        // 指定入口,启动类型,包名
        Intent intent = new Intent(Intent.ACTION_MAIN);//入口Main
        intent.addCategory(Intent.CATEGORY_LAUNCHER);// 启动LAUNCHER,跟MainActivity里面的配置类似
        intent.setPackage(PKGNAME);//包名
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
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "未安装对应APP", Toast.LENGTH_SHORT).show();

            NotificationUtils notificationUtils = new NotificationUtils(view.getContext());
            Notification notification = notificationUtils.getNotification("打开应用失败", "请安装对应APP", R.mipmap.ic_launcher);
            notificationUtils.getManager().notify(1, notification);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        setView(this.getView());
    }


    private void setView(View root) {
        final EditText account = root.findViewById(R.id.editText2);
        final Button getAccountButton = root.findViewById(R.id.get_account);

        account.setFocusable(false);
        account.setClickable(false);

        SharedPreferences sp = this.getActivity().getSharedPreferences(SPNAME, Context.MODE_PRIVATE);
        String accountText = sp.getString("account", null);

        if (accountText == null) {
            account.setText("未记录账号");
            getAccountButton.setText("点击获取");
        } else {
            account.setText(accountText);
            getAccountButton.setText("重新获取");
        }

        getAccountButton.setOnClickListener(new GetAccountOnclickListener());
    }
}