package com.example.kosuke.gesture;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Random;

public class AsyncHttp extends AsyncTask<HashMap<String, String>, Void, JSONObject> {
    private static final String TAG = AsyncHttp.class.getSimpleName();

    HttpURLConnection con = null;
    String rootUrl = "https://api-us.faceplusplus.com/humanbodypp/beta/gesture";

    private CallBackTask callBackTask;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    /**
     * @param params request, json, access_token
     */
    @Override
    protected JSONObject doInBackground(HashMap<String, String>... params) {
        JSONObject retJson = null;
        int status = -1;

        String boundaryString = getBoundary();

        try {
            Log.d(TAG, "Connect to " + rootUrl);
            URL url = new URL(rootUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setUseCaches(false);
            con.setRequestMethod("POST");
            con.setInstanceFollowRedirects(false);
            con.setRequestProperty("accept", "*/*");
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);
            con.setRequestProperty("connection", "Keep-Alive");
            con.setRequestProperty("user-agent", "Mozilla/4.0 (compatible;MSIE 6.0;Windows NT 5.1;SV1)");
            con.setDoOutput(true);

            DataOutputStream obos = new DataOutputStream(con.getOutputStream());
            for (String key : params[0].keySet()) {
                obos.writeBytes("--" + boundaryString + "\r\n");
                obos.writeBytes("Content-Disposition: form-data; name=\"" + key
                        + "\"\r\n");
                obos.writeBytes("\r\n");
                obos.writeBytes(params[0].get(key) + "\r\n");
            }
            obos.writeBytes("--" + boundaryString + "--" + "\r\n");
            obos.writeBytes("\r\n");
            obos.flush();
            obos.close();

            status = con.getResponseCode();
            Log.d(TAG, "ResponseCode: " + status);

            final InputStream stream = (status == 200) ? con.getInputStream() : con.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            String buffer = reader.readLine();
            retJson = new JSONObject(buffer);
            retJson.put("status", status);

            con.disconnect();
        } catch (ConnectException e) {
            e.printStackTrace();
            return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retJson;
    }

    @Override
    protected void onPostExecute(JSONObject json) {
        super.onPostExecute(json);
        if (callBackTask != null) {
            callBackTask.CallBack(json);
        }
    }

    public void setOnCallBack(CallBackTask _cbj) {
        callBackTask = _cbj;
    }

    public static interface CallBackTask {
        public void CallBack(JSONObject json);
    }

    private static String getBoundary() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 32; ++i) {
            sb.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-".charAt(random.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_".length())));
        }
        return sb.toString();
    }

    private static String encode(String value) throws Exception {
        return URLEncoder.encode(value, "UTF-8");
    }

}
