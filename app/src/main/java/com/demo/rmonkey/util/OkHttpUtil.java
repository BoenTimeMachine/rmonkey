package com.demo.rmonkey.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class OkHttpUtil {
    private static OkHttpClient client = new OkHttpClient();

    public static void postData(String host, String time, List<String> data) {

        JsonObject jsonContainer = new JsonObject();
        jsonContainer.addProperty("time", time);

        Gson gson = new Gson();
        String dataGson = gson.toJson(data);
        jsonContainer.addProperty("data", dataGson);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, jsonContainer.toString());
        Request request = new Request.Builder()
                .url("http://" + host + "/api/rc")
                .post(requestBody)
                .build();

        Call call = client.newCall(request);

        try {
            call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void postGroup(String host, String group) {

        JsonObject jsonContainer = new JsonObject();
        jsonContainer.addProperty("group", group);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(JSON, jsonContainer.toString());
        Request request = new Request.Builder()
                .url("http://" + host + "/api/g")
                .post(requestBody)
                .build();

        Call call = client.newCall(request);

        try {
            call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
