package com.demo.rmonkey.ui.dashboard;

import android.content.Context;
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
import com.demo.rmonkey.util.ToastUtils;

import static com.demo.rmonkey.util.Const.SPNAME;

public class DashboardFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        SharedPreferences sp = this.getContext().getSharedPreferences(SPNAME, Context.MODE_PRIVATE);

        final EditText host = root.findViewById(R.id.host);

        host.setText(sp.getString("host", ""));

        final Button setHostButton = root.findViewById(R.id.set_host);
        setHostButton.setOnClickListener(new SetHostOnclickListener());

        final EditText pass = root.findViewById(R.id.pass);
        pass.setText(sp.getString("pass", ""));

        final Button setPassButton = root.findViewById(R.id.set_pass);
        setPassButton.setOnClickListener(new SetPassOnclickListener());

        final Switch autoOpenSwitch = root.findViewById(R.id.auot_open);
        boolean ao = sp.getBoolean("autoOpen", false);
        autoOpenSwitch.setChecked(ao);
        autoOpenSwitch.setOnCheckedChangeListener(new AutoOpenOnCheckedListener());

        final Switch closeSwitch = root.findViewById(R.id.close);
        boolean close= sp.getBoolean("close", false);
        closeSwitch.setChecked(close);
        closeSwitch.setOnCheckedChangeListener(new CloseOnCheckedListener());

        return root;
    }

    class AutoOpenOnCheckedListener implements Switch.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SharedPreferences sp = buttonView.getContext().getSharedPreferences(SPNAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("autoOpen", isChecked);
            editor.commit();

            ToastUtils.show(buttonView.getContext(), "设置成功");
        }
    }

    class CloseOnCheckedListener implements Switch.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SharedPreferences sp = buttonView.getContext().getSharedPreferences(SPNAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("close", isChecked);
            editor.commit();

            ToastUtils.show(buttonView.getContext(), "设置成功");
        }
    }

    class SetHostOnclickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final EditText host = v.getRootView().findViewById(R.id.host);
            SharedPreferences sp = v.getContext().getSharedPreferences(SPNAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("host", host.getText().toString());
            editor.commit();

            ToastUtils.show(v.getContext(), "服务器地址更新成功");
        }
    }

    class SetPassOnclickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final EditText pass = v.getRootView().findViewById(R.id.pass);
            SharedPreferences sp = v.getContext().getSharedPreferences(SPNAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("pass", pass.getText().toString());
            editor.commit();

            ToastUtils.show(v.getContext(), "密码更新成功");
        }
    }
}