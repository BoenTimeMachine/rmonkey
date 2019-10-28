package com.demo.rmonkey.runnable;

import com.demo.rmonkey.util.OkHttpUtil;

public class PassRunnable implements Runnable {
    private String token;
    private String pass;

    public PassRunnable(String token, String pass) {
        this.token = token;
        this.pass = pass;
    }

    public void run() {
        OkHttpUtil.postPass(token, pass);
    }
}
