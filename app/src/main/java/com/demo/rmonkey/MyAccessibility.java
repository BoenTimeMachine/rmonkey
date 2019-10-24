package com.demo.rmonkey;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class MyAccessibility extends BaseAccessibilityService {

    private static final String TAG = "xys";

    private static final String PKG = "com.chengxin.talk:id/";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "onAccessibilityEvent: " + event.toString());
        checkMessage();
        openRedPacket();

    }

    public void checkMessage() {
        AccessibilityNodeInfo nodeInfo = findViewByID(PKG+"messageListView");

        List<AccessibilityNodeInfo > messages =  nodeInfo.findAccessibilityNodeInfosByText("红包已领取");

        if(messages == null || messages.size() <= 0) {
            return;
        }

        for (AccessibilityNodeInfo message: messages){
            Log.d("msg", "耶耶耶");
            performViewClick(message);
            return;
        }
    }

    public void openRedPacket() {
            clickTextViewByID(PKG+"iv_open_redpacket ");
    }
}
