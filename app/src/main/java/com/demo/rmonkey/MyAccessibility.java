package com.demo.rmonkey;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class MyAccessibility extends BaseAccessibilityService {

    private static final String TAG = "虹猴";

    private static final String PKG = "com.chengxin.talk:id/";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        // TYPE_WINDOW_CONTENT_CHANGED
        if(event.getEventType() == Integer.valueOf(0x00000800)) {
            Log.d(TAG, "onAccessibilityEvent: " + event.toString());

            try {
                checkMessage();
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }

    public void checkMessage() throws ParseException {
        AccessibilityNodeInfo alreadyGet = findViewByID(PKG+"tv_already_get");

        if(alreadyGet == null) {
            return;
        }

        String alreadyGetInfo = alreadyGet.getText().toString();
        String pattern = "([0-9]+)/\\1";

        Log.d(TAG, alreadyGetInfo);

        boolean isMatch = Pattern.matches(pattern, alreadyGetInfo);

        Log.d(TAG, "红包是否领完:" + (isMatch?"是":"否"));

        String time = getRedPacketTime();

        Log.d(TAG, "第一个领取时间"+ time);

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

            Log.d(TAG, money.getText().toString());
        }

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


    public void openRedPacket() {
            clickTextViewByID(PKG+"iv_open_redpacket ");
    }
}
