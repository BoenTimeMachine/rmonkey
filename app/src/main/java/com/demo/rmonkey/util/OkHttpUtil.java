package com.demo.rmonkey.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class OkHttpUtil {
    private final static String host = "http://132.232.96.142:9709/api";
    private static OkHttpClient client = new OkHttpClient.Builder().retryOnConnectionFailure(false)
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS).build();

    public static void postData(String token, String time, List<String> data) {

        JsonObject jsonContainer = new JsonObject();
        jsonContainer.addProperty("time", time);

        Gson gson = new Gson();
        String dataGson = gson.toJson(data);
        jsonContainer.addProperty("data", dataGson);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, jsonContainer.toString());
        Request request = new Request.Builder()
                .url(host + "/rc")
                .header("Authorization", token)
                .post(requestBody)
                .build();

        Call call = client.newCall(request);

        try {
            call.execute();
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    public static void postGroup(String token, String group) {

        JsonObject jsonContainer = new JsonObject();
        jsonContainer.addProperty("group", group);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, jsonContainer.toString());
        Request request = new Request.Builder()
                .url(host + "/g")
                .header("Authorization", token)
                .post(requestBody)
                .build();

        Call call = client.newCall(request);

        try {
            call.execute();
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    public static void postPass(String token, String pass) {

        JsonObject jsonContainer = new JsonObject();
        jsonContainer.addProperty("pass", pass);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, jsonContainer.toString());
        Request request = new Request.Builder()
                .url(host + "/p")
                .header("Authorization", token)
                .post(requestBody)
                .build();

        Call call = client.newCall(request);

        try {
            call.execute();
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }
}
