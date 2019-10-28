package com.demo.rmonkey;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.demo.rmonkey.runnable.DataRunnable;
import com.demo.rmonkey.runnable.GroupRunnable;
import com.ycbjie.notificationlib.NotificationUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.demo.rmonkey.util.Const.PKGNAME;
import static com.demo.rmonkey.util.Const.SPNAME;

public class MyAccessibility extends BaseAccessibilityService {

    private static final String TAG = "\u8679\u7334";

    private static final String PKG = PKGNAME + ":id/";

    private int state = 0;

    private Handler mainHandler = new Handler();

    private Runnable mainRunnable = new Runnable() {
        @SuppressLint("Assert")
        @Override
        public void run() {
            try {
                assert getSettingBoolean("close") && getRootInWindow() != null;

                // 检查账号
                checkAccount();

                // 检查页面
                checkPage();

            } catch (Exception e) {
                Log.d(TAG, "error: " + e.getMessage());
            } finally {
                mainHandler.postDelayed(this, 500);
            }
        }
    };

    @SuppressLint("Assert")
    private void checkPage() throws Exception {
        try {

            AccessibilityNodeInfo myToolbar = findViewByID(getId("myToolbar"));

            if (findViewByID(getId("rl_redpacket_main")) != null) {
                // 红包弹窗
                doReadPacketModal();
            } else if (findNodeInfoInChild(myToolbar, getId("number"))) {
                // 群聊
                doGroup();
            } else if (myToolbar.getChild(1).getText() != null && myToolbar.getChild(1).getText().equals("红包详情")) {
                // 红包详情
                doReadPacket();
            } else if (myToolbar.getChild(0).getText() != null) {
                // 首页
                doHome();

            } else if (myToolbar.getChild(1).getText() != null && myToolbar.getChild(1).getText().toString().startsWith("聊天成员")) {
                // 群聊设置
                doGroupInfo();
            } else {
                assert false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("未找到页面");
        }

    }

    private void doHome() {
        Log.d(TAG, "doHome: ");
        state = 0;

        if(getGroup() != null) {
            postGroup(null);
            notify("已离开群聊", "监听已暂停");
        }
    }

    private void doGroup() throws Exception {
        Log.d(TAG, "doGroup: ");

        String group = getGroup();
        boolean show = state == 0;

        AccessibilityNodeInfo myToolbar = findViewByID(getId("myToolbar"));

        if (group == null) {
            notify("已进入群聊", "正在获取信息", show);
            goGroupInfo();
        } else if (!group.endsWith(myToolbar.getChild(2).getChild(0).getText().toString())) {
            postGroup(null);
            notify("已更换群聊", "正在重新获取信息", show);
            goGroupInfo();
        } else {
            notify("已进入群聊", "正在监听中-" + group, show);
            listenGroup();
        }

    }

    private void listenGroup() throws Exception {
        state = 1;

        AccessibilityNodeInfo list = findViewByID(getId("messageListView"));

        assert list != null;

        list.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);

        List<AccessibilityNodeInfo> abs = findViewsByID(getId("rel_background"));

        assert abs != null;

        AccessibilityNodeInfo last = abs.get(abs.size() - 1);

        performViewClick(last);
    }

    private void goGroupInfo() throws Exception {
        Thread.sleep(500);
        performViewClick(findViewByID(getId("action_help")));
    }

    private void doGroupInfo() throws Exception {
        Log.d(TAG, "doGroupInfo: ");
        String group = findViewByID(getId("txt_team_id")).getText().toString() + "|" + getNodeInfoText(findViewByID(getId("layoutName")).getChild(1));
        postGroup(group);
        performGoBack();
        notify("已获取群信息", "正在监听中-" + group);
    }

    private void doReadPacket() throws Exception {
        Log.d(TAG, "doReadPacket: ");
        checkAlreadyGetInfo();
    }

    private void doReadPacketModal() {
        Log.d(TAG, "doReadPacketModal: ");
        openReadPacket();
    }

    private void checkAccount() throws Exception {
        if (getSettingString("account") != null) {
            return;
        }

        AccessibilityNodeInfo tabs = findViewByID(getId("ctab_layout"));

        if (tabs == null) {
            Thread.sleep(300);
            performGoBack();
            throw new Exception("未在首页");
        }

        AccessibilityNodeInfo me;

        try {
            me = tabs.getChild(0).getChild(3);
            assert me != null && "我".equals(getNodeInfoText(me));
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("未找到我按钮");
        }

        Thread.sleep(300);
        performViewClick(me);

        AccessibilityNodeInfo accountNode = findViewByID(getId("head_detail_label"));

        try {
            assert accountNode != null && accountNode.getText() != null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("未检测到城信号");
        }

        setSettingString("account", accountNode.getText().toString());
        notify("账号已获取", "选择群聊进入即可开始记录");
    }

    private boolean checkAlreadyGetInfo() throws Exception {

        AccessibilityNodeInfo alreadyGet = findViewByID(getId("tv_already_get"));

        if (alreadyGet == null) {
            return false;
        }

        String alreadyGetInfo = alreadyGet.getText().toString();
        Pattern pattern = Pattern.compile("\u5df2\u9886\u53d6([0-9]+)/(\\1)\u4e2a");
        Matcher matcher = pattern.matcher(alreadyGetInfo);

        boolean isMatch = matcher.find();

        if (!isMatch) {
//            Log.d(TAG , "未领取完，已忽略");
            performGoBack();
            return false;
        }

        // 红包个数
        int size = Integer.parseInt(matcher.group(1));

        // 第一个的时间
        String time = getRedPacketTime();

        String id = getRedPacketID(size, time);

        if (isChecked(id)) {
            performGoBack();
            return true;
        }

        List<String> minfos = new ArrayList<>();

        List<AccessibilityNodeInfo> moneys = findViewsByID(getId("tv_red_money"));

        Log.d(TAG, "checkAlreadyGetInfo: moneys" + moneys.toString());

        if (moneys == null) {
            return false;
        }

        for (AccessibilityNodeInfo money : moneys) {
            if (money == null) {
                continue;
            }

            AccessibilityNodeInfo parent = money.getParent();

            if (!"android.widget.LinearLayout".equals(parent.getClassName().toString())) {
                continue;
            }

            minfos.add(money.getText().toString());
        }

        if (size > moneys.size()) {
            performScrollForward(findViewByID(getId("rv_redpacket")));
        }

        moneys = findViewsByID(getId("tv_red_money"));

        int rest = size - minfos.size();

        if (rest > 0 && moneys.size() > rest) {
            for (int ri = moneys.size() - rest; ri < moneys.size(); ri++) {
                AccessibilityNodeInfo money = moneys.get(ri);
                if (money == null) {
                    continue;
                }

                AccessibilityNodeInfo parent = money.getParent();

                if (!"android.widget.LinearLayout".equals(parent.getClassName().toString())) {
                    continue;
                }

                minfos.add(money.getText().toString());
            }
        }

        saveRedPacketId(id);
        postData(time, minfos);
        performGoBack();
        return true;
    }

    private String getRedPacketTime() throws ParseException {
        List<AccessibilityNodeInfo> times = findViewsByID(PKG + "tv_get_time");

        if (times == null) {
            return null;
        }

        Date minDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        for (AccessibilityNodeInfo time : times) {
            if (time == null) {
                continue;
            }

            Date mDate = sdf.parse(time.getText().toString());

            if (mDate.before(minDate)) {
                minDate = mDate;
            }
        }

        return sdf.format(minDate);
    }

    public boolean isChecked(String id) {
        SharedPreferences preferences = getSharedPreferences(SPNAME, MODE_PRIVATE);

        Set<String> reds = preferences.getStringSet("red_packet", new LinkedHashSet<String>());

        assert reds != null;
        return reds.contains(id);
    }

    public void saveRedPacketId(String id) {
        SharedPreferences preferences = getSharedPreferences(SPNAME, MODE_PRIVATE);

        Set<String> reds = preferences.getStringSet("red_packet", new LinkedHashSet<String>());

        assert reds != null;
        reds.add(id);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet("red_packet", reds);
        editor.apply();
    }

    public void openReadPacket() {
        if (getSettingBoolean("autoOpen", false) && findViewByID(PKG + "iv_open_redpacket") != null) {
            performViewClick(findViewByID(PKG + "iv_open_redpacket"));
        } else if (findViewByID(PKG + "tv_other_redpacket") != null) {
            performViewClick(findViewByID(PKG + "tv_other_redpacket"));
        } else if (findViewByID(PKG + "rel_parent") != null) {
            findViewByID(PKG + "rel_parent").performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    public String getGroup() {
        SharedPreferences preferences = getSharedPreferences(SPNAME, MODE_PRIVATE);
        return preferences.getString("group", null);
    }

    public String getRedPacketID(int size, String time) {
        String content = findViewByID(PKG + "tv_red_blessings").getText().toString();
        return time + "|" + size + "|" + content;
    }

    // 数据发送
    public void postData(String time, List<String> data) {
        new Thread(new DataRunnable(getToken(),getSettingString("host", "192.168.101.103:9709"), time, data)).start();
    }

    public void postGroup(String group) {
        setSettingString("group", group);
        new Thread(new GroupRunnable(getToken(), getSettingString("host", "192.168.101.103:9709"), group)).start();
    }


    // 辅助函数
    private String getId(String id) {
        return PKG + id;
    }

    private String getToken() {
        String account = getSettingString("account", "").substring(4);
        return account + "::::" + getSettingString("pass", "");
    }

    // 发通知
    private void notify(String title) {
        NotificationUtils notificationUtils = new NotificationUtils(this.getApplicationContext());
        Notification notification = notificationUtils.getNotification(title, null, R.mipmap.ic_launcher);
        notificationUtils.getManager().notify(1, notification);
    }

    private void notify(String title, String content) {
        NotificationUtils notificationUtils = new NotificationUtils(this.getApplicationContext());
        Notification notification = notificationUtils.getNotification(title, content, R.mipmap.ic_launcher);
        notificationUtils.getManager().notify(1, notification);
    }

    private void notify(String title, String content, boolean show) {
        if (!show) {
            return;
        }

        NotificationUtils notificationUtils = new NotificationUtils(this.getApplicationContext());
        Notification notification = notificationUtils.getNotification(title, content, R.mipmap.ic_launcher);
        notificationUtils.getManager().notify(1, notification);
    }

    // 本地存储
    private void setSettingString(String key, String value) {
        getSharedPreferences(SPNAME, MODE_PRIVATE).edit().putString(key, value).apply();
    }

    private String getSettingString(String key, String defValue) {
        return getSharedPreferences(SPNAME, MODE_PRIVATE).getString(key, defValue);
    }

    private String getSettingString(String key) {
        return getSharedPreferences(SPNAME, MODE_PRIVATE).getString(key, null);
    }

    private void setSettingBoolean(String key, Boolean value) {
        getSharedPreferences(SPNAME, MODE_PRIVATE).edit().putBoolean(key, value).apply();
    }

    private boolean getSettingBoolean(String key, boolean defValue) {
        return getSharedPreferences(SPNAME, MODE_PRIVATE).getBoolean(key, defValue);
    }

    private boolean getSettingBoolean(String key) {
        return getSharedPreferences(SPNAME, MODE_PRIVATE).getBoolean(key, false);
    }

    @Override
    protected void onServiceConnected() {
        notify("辅助服务已启动", getSettingString("account", null) != null ? "进入群聊即可开始记录" : "即将自动获取账号信息，请勿操作");

        // 启动轮询
        mainHandler.postDelayed(mainRunnable, 600);
    }
}
