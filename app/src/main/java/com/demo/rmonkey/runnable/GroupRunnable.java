package com.demo.rmonkey.runnable;

import com.demo.rmonkey.util.OkHttpUtil;

public class GroupRunnable implements Runnable {
    private String token;
    private String group;

    public GroupRunnable(String token, String group) {
        this.token = token;
        this.group = group;
    }

    public void run() {
        OkHttpUtil.postGroup(token, group);
    }
}
