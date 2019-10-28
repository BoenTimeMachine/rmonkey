package com.demo.rmonkey.ui.home;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.demo.rmonkey.R;
import com.demo.rmonkey.util.ActivityUtil;
import com.ycbjie.notificationlib.NotificationUtils;

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

            ActivityUtil.openActivity(getActivity());

            NotificationUtils notificationUtils = new NotificationUtils(v.getContext());
            Notification notification = notificationUtils.getNotification("正在获取用户信息", "请稍等...", R.mipmap.ic_launcher);
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