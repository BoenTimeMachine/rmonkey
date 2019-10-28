package com.demo.rmonkey.runnable;

import com.demo.rmonkey.util.OkHttpUtil;

public class GroupRunnable implements Runnable {
    private String token;
    private String host;
    private String group;

    public GroupRunnable(String token, String host, String group) {
        this.token = token;
        this.host = host;
        this.group = group;
    }

    public void run() {
        OkHttpUtil.postGroup(token, host, group);
    }
}
