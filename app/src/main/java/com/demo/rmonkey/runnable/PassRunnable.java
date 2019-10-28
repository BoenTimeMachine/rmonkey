package com.demo.rmonkey.runnable;

import com.demo.rmonkey.util.OkHttpUtil;

public class PassRunnable implements Runnable {
    private String token;
    private String host;
    private String pass;

    public PassRunnable(String token, String host, String pass) {
        this.token = token;
        this.host = host;
        this.pass = pass;
    }

    public void run() {
        OkHttpUtil.postPass(token, host, pass);
    }
}
