package com.demo.rmonkey;

import android.app.Notification;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.demo.rmonkey.util.Debouncer;
import com.demo.rmonkey.util.OkHttpUtil;
import com.ycbjie.notificationlib.NotificationUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
import static com.demo.rmonkey.util.Const.PKGNAME;
import static com.demo.rmonkey.util.Const.SPNAME;

public class MyAccessibility extends BaseAccessibilityService {

    private static final String TAG = "\u8679\u7334";

    private static final String PKG = PKGNAME + ":id/";

    private boolean checking = false;

    private Debouncer messageDebouncer = new Debouncer(new Debouncer.Callback<Integer>(){
        @Override
        public void call(Integer type) {
            Log.d(TAG, "检查开始 ------" + type);

            if(checking) {
                return;
            };

            checking = true;

            try {
                check(type == TYPE_WINDOW_STATE_CHANGED);
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.d(TAG, ex.toString());
            }finally {
                checking = false;
                Log.d(TAG, "检查结束 ------");
            }
        }
    }, 800);

//    private Debouncer accountDebouncer = new Debouncer(new Debouncer.Callback<String>(){
//        @Override
//        public void call(String e) {
//            Log.d(TAG, "账号检查开始 ------");
//
//            if(checking || !nonAccount) {
//                return;
//            }
//
//            try {
//                checkAccount();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//                Log.d(TAG, "出错了");
//            }finally {
//                Log.d(TAG, "账号检查结束 ------");
//            }
//        }
//    }, 1000);


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if(getClosed()){
            return;
        }

        Log.d(TAG, event.toString());

//        // TYPE_WINDOW_STATE_CHANGED
//        // TYPE_WINDOW_CONTENT_CHANGED
//        if(event.getEventType() == TYPE_WINDOW_CONTENT_CHANGED) {
            messageDebouncer.call(event.getEventType());
//        }

    }

    private void check(boolean windowStageChanged) throws Exception {
        SharedPreferences preferences = getSharedPreferences(SPNAME,MODE_PRIVATE);

        if(preferences.getBoolean("geting-account", false)) {
            // 检查账号
            checkAccount();
        } else {
            // 检查所在位置
            checkPosition(windowStageChanged);
        }
    }

    private void checkPosition(boolean windowStageChanged) throws Exception {
        if(findViewByID(PKG + "iv_tab_icon") != null) {
            saveGroup(null);
            notify("已离开群聊", "监听已暂停");
        } else if(findViewByID(PKG + "action_help") != null){
            String group = getGroup();

            if(group != null) {

                if(group.contains(findViewByID(PKG +"title").getText())) {
                    if(windowStageChanged){
                        notify("已进入群聊", "正在监听红包");
                    }

                    checkMessageList();

                    return;
                } else {
                    notify("群聊已更换", "正在重新获取信息");
                }
            };

            Thread.sleep(500);

            performViewClick(findViewByID(PKG + "action_help"));
        }else if(findViewByID(PKG + "txt_team_id") != null){
            Thread.sleep(500);

            String group = getGroupFromView();
            notify("已获取群信息", "正在监听红包");

            saveGroup(group);

            performGoBack();
        } else if(findViewByID(PKG + "rl_redpacket_main") != null){
            openReadPacket();
        }else if(findViewByID(PKG+"tv_already_get") != null){
            checkAlreadyGetInfo();
        };
    }

    private void checkMessageList() throws Exception {
        AccessibilityNodeInfo list = findViewByID(PKG + "messageListView");

        if(list == null) {
            return;
        }

        List<AccessibilityNodeInfo> messages = list.findAccessibilityNodeInfosByViewId(PKG + "message_item_content");

        if(messages == null || messages.isEmpty()) {
            return;
        }

        for (AccessibilityNodeInfo message: messages) {
            if(!findNodeInfoInChild(message, PKG + "rel_background")){
                Log.d(TAG, "checkMessageList: 不是红包");

            } else {
                Log.d(TAG, "checkMessageList: 是红包");
            }
        }


//        int lastHashCode = 0;
//
//        while (true){
//            List<AccessibilityNodeInfo> messages = list.findAccessibilityNodeInfosByViewId(PKG + "message_item_content");
//
//
//            if(messages == null || messages.isEmpty()) {
//                return;
//            }
//
//            Log.d(TAG, "checkMessageList: " + messages.size());
//
//            AccessibilityNodeInfo lastMessage = messages.get(messages.size() - 1);
//
//            String lastMessageText = getNodeInfoText(lastMessage);
//
//            if(lastMessageText == null || lastMessage.hashCode() == lastHashCode) {
//                Log.d(TAG, "到底了");
//                return;
//            }
//
//            Log.d(TAG, lastMessageText);
//            Log.d(TAG, Integer.toString(lastMessage.hashCode()));
//
//            lastHashCode = lastMessage.hashCode();
//
//            performScrollForward(list);
//        }
    }

    private void checkAccount() throws Exception {
      SharedPreferences preferences = getSharedPreferences(SPNAME,MODE_PRIVATE);

        AccessibilityNodeInfo tabs = findViewByID(PKG+"ctab_layout");
        int times = 6;

        while(times > 0 && tabs == null) {
            times --;
            Thread.sleep(1000);
            tabs = findViewByID(PKG + "ctab_layout");
        }

        AccessibilityNodeInfo me = findViewByText("我");

        if(me == null) {
            notify("应用操作失败", "请重启城信客户端");
            return;
        }

        performViewClick(me);

        AccessibilityNodeInfo accountNode = findViewByID(PKG + "head_detail_label");

        if(accountNode == null) {
            notify("未检测到城信号", "请先设置城信号");
            return ;
        }

        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("account", accountNode.getText().toString());
        editor.putBoolean("geting-account", false);
        editor.commit();

        notify("账号已记录", "进入群聊即可自动记录");
    }

    private void checkAlreadyGetInfo() throws Exception {
        AccessibilityNodeInfo alreadyGet = findViewByID(PKG+"tv_already_get");

        String alreadyGetInfo = alreadyGet.getText().toString();
        Pattern pattern = Pattern.compile("\u5df2\u9886\u53d6([0-9]+)/(\\1)\u4e2a");
        Matcher matcher = pattern.matcher(alreadyGetInfo);

        boolean isMatch = matcher.find();

        if(!isMatch){
//            Log.d(TAG , "未领取完，已忽略");
            performGoBack();
            return;
        }

        // 红包个数
        int size = Integer.parseInt(matcher.group(1));

        // 第一个的时间
        String time = getRedPacketTime();

        String id = getRedPacketID(size, time);

        if(isChecked(id)) {
//            Log.d(TAG , "被记录过，已忽略");
            performGoBack();
            return;
        }

//        Log.d(TAG, "\u7b2c\u4e00\u4e2a\u9886\u53d6\u65f6\u95f4" + time);

        List<String> minfos = new ArrayList<>();

        List<AccessibilityNodeInfo> moneys = findViewsByID(PKG + "tv_red_money");

        if(moneys == null) {
            return;
        }

        for (AccessibilityNodeInfo money: moneys) {
            if(money == null) {
                continue;
            }

            AccessibilityNodeInfo parent = money.getParent();

            if(!"android.widget.RelativeLayout".equals(parent.getClassName().toString())) {
                continue;
            }

            minfos.add(money.getText().toString());
        }

        if(size > moneys.size()) {
            performScrollForward(findViewByID(PKG + "rv_redpacket"));
        }

        moneys = findViewsByID(PKG + "tv_red_money");

        int rest = size - minfos.size();

        if (rest > 0 && moneys.size() > rest) {
            for ( int ri = moneys.size() - rest; ri < moneys.size(); ri ++) {
                AccessibilityNodeInfo money = moneys.get(ri);
                if(money == null) {
                    continue;
                }

                AccessibilityNodeInfo parent = money.getParent();

                if(!"android.widget.RelativeLayout".equals(parent.getClassName().toString())) {
                    continue;
                }

                minfos.add(money.getText().toString());
            }
        }

        saveRedPacketId(id);
        postData(time, minfos);
//        Log.d(TAG , "记录成功");
        performGoBack();
    }

    private String getRedPacketTime() throws ParseException {
        List<AccessibilityNodeInfo> times = findViewsByID(PKG + "tv_get_time");

        if(times == null) {
            return null;
        }

        Date minDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");

        for (AccessibilityNodeInfo time: times) {
            if(time == null) {
                continue;
            }

            Date mDate = sdf.parse(time.getText().toString());

            if(mDate.before(minDate)) {
                minDate = mDate;
            }
        }

        return  sdf.format(minDate);
    }

    @Override
    protected  void onServiceConnected(){
        SharedPreferences preferences = getSharedPreferences(SPNAME,MODE_PRIVATE);

        String account = preferences.getString("account", null);

        if(account != null) {
            notify("辅助服务已启动", "进入群聊即可开始记录");
        } else {
            notify("辅助服务已启动", "请进入虹猴APP中获取用户信息");
        }

    }

    public boolean isChecked(String id) {
        SharedPreferences preferences = getSharedPreferences(SPNAME,MODE_PRIVATE);

        Set<String> reds = preferences.getStringSet("red_packet", new HashSet<String>());

        return reds.contains(id);
    };

    public void saveRedPacketId(String id) {
        SharedPreferences preferences = getSharedPreferences(SPNAME,MODE_PRIVATE);

        Set<String> reds = preferences.getStringSet("red_packet",  new HashSet<String>());

        reds.add(id);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet("red_packet", reds);
        editor.commit();
    };

    public void openReadPacket() {
        SharedPreferences preferences = getSharedPreferences(SPNAME,MODE_PRIVATE);

        if(preferences.getBoolean("autoOpen", false)&& findViewByID(PKG + "iv_open_redpacket") != null) {
            performViewClick(findViewByID(PKG+"iv_open_redpacket"));
        } else if(findViewByID(PKG + "tv_other_redpacket") != null) {
            performViewClick(findViewByID(PKG + "tv_other_redpacket"));
        }else{
            performGoBack();
        }
    }

    public String getGroupFromView() {
      return  findViewByID(PKG + "txt_team_id").getText().toString() + "|" + getNodeInfoText(findViewByID(PKG + "layoutName").getChild(1));
    };

    public String getGroup() {
        SharedPreferences preferences = getSharedPreferences(SPNAME,MODE_PRIVATE);
        return  preferences.getString("group", null);
    };

    public void saveGroup(String group) {
        SharedPreferences preferences = getSharedPreferences(SPNAME,MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("group", group);
        editor.commit();

        postGroup(group);
    };

    public boolean getCheckingGroup() {
        SharedPreferences preferences = getSharedPreferences(SPNAME,MODE_PRIVATE);
        return  preferences.getBoolean("checking-group", false);
    };

    public void setCheckingGroup(boolean checking) {
        SharedPreferences preferences = getSharedPreferences(SPNAME,MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("checking-group", checking);
        editor.commit();
    };

    public String getRedPacketID(int size, String time){
      String content = findViewByID(PKG + "tv_red_blessings").getText().toString();
     return time+"|"+size+"|"+content;
    }

    public void postData(String time, List<String> data) {
        SharedPreferences preferences = getSharedPreferences(SPNAME,MODE_PRIVATE);
        OkHttpUtil.postData(preferences.getString("host", "192.168.101.103:9709"), time, data);
    }

    public void postGroup(String group) {
        SharedPreferences preferences = getSharedPreferences(SPNAME,MODE_PRIVATE);
        OkHttpUtil.postGroup(preferences.getString("host", "192.168.101.103:9709"), group);
    }

    public boolean getClosed() {
        SharedPreferences preferences = getSharedPreferences(SPNAME,MODE_PRIVATE);
        return  preferences.getBoolean("close", false);
    };

    public void notify(String title) {
        NotificationUtils notificationUtils = new NotificationUtils(this.getApplicationContext());
        Notification notification = notificationUtils.getNotification(title, null, R.mipmap.ic_launcher);
        notificationUtils.getManager().notify(1,notification);
    }

    public void notify(String title, String content) {
        NotificationUtils notificationUtils = new NotificationUtils(this.getApplicationContext());
        Notification notification = notificationUtils.getNotification(title, content, R.mipmap.ic_launcher);
        notificationUtils.getManager().notify(1,notification);
    }
}
