package com.demo.rmonkey.ui.dashboard;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.demo.rmonkey.R;
import com.demo.rmonkey.runnable.PassRunnable;
import com.demo.rmonkey.util.ToastUtils;

import static android.content.Context.MODE_PRIVATE;
import static com.demo.rmonkey.util.Const.SPNAME;

public class DashboardFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        SharedPreferences sp = this.getContext().getSharedPreferences(SPNAME, MODE_PRIVATE);


        final EditText pass = root.findViewById(R.id.pass);
        pass.setText(sp.getString("pass", "123456"));

        final Button setPassButton = root.findViewById(R.id.set_pass);
        setPassButton.setOnClickListener(new SetPassOnclickListener());

        final Switch autoOpenSwitch = root.findViewById(R.id.auot_open);
        boolean ao = sp.getBoolean("autoOpen", false);
        autoOpenSwitch.setChecked(ao);
        autoOpenSwitch.setOnCheckedChangeListener(new AutoOpenOnCheckedListener());

        final Switch closeSwitch = root.findViewById(R.id.close);
        boolean close = sp.getBoolean("close", false);
        closeSwitch.setChecked(close);
        closeSwitch.setOnCheckedChangeListener(new CloseOnCheckedListener());

        return root;
    }

    class AutoOpenOnCheckedListener implements Switch.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SharedPreferences sp = buttonView.getContext().getSharedPreferences(SPNAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("autoOpen", isChecked);
            editor.apply();

            ToastUtils.show(buttonView.getContext(), "设置成功");
        }
    }

    class CloseOnCheckedListener implements Switch.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SharedPreferences sp = buttonView.getContext().getSharedPreferences(SPNAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("close", isChecked);
            editor.apply();

            ToastUtils.show(buttonView.getContext(), "设置成功");
        }
    }


    class SetPassOnclickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final EditText pass = v.getRootView().findViewById(R.id.pass);
            SharedPreferences sp = v.getContext().getSharedPreferences(SPNAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();

            String newPass =  pass.getText().toString();

            new Thread(new PassRunnable(getToken(), newPass)).start();

            editor.putString("pass", pass.getText().toString());
            editor.apply();

            ToastUtils.show(v.getContext(), "密码更新完成");
        }
    }

    private String getSettingString(String key, String defValue) {
        return this.getContext().getSharedPreferences(SPNAME, MODE_PRIVATE).getString(key, defValue);
    }

    private String getToken() {
        String account = getSettingString("account", "").substring(4);
        return account + "::::" + getSettingString("pass", "123456");
    }
}