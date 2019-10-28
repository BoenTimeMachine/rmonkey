package com.demo.rmonkey.runnable;

import com.demo.rmonkey.util.OkHttpUtil;

import java.util.List;

public class DataRunnable implements Runnable {
    private String token;
    private String host;
    private String time;
    private List<String> data;

    public DataRunnable(String token, String host, String time, List<String> data) {
        this.token = token;
        this.data = data;
        this.time = time;
        this.host = host;
    }

    public void run() {
        OkHttpUtil.postData(token,host, time, data);
    }
}
